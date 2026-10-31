@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@file:Suppress("DEPRECATION")

package org.jetbrains.kotlinconf.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.useContents
import org.jetbrains.kotlinconf.personal.MediaKind
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCompatible
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.QuickLook.QLPreviewController
import platform.QuickLook.QLPreviewControllerDataSourceProtocol
import platform.QuickLook.QLPreviewItemProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraCaptureMode
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerQualityTypeHigh
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSInteger
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

@Composable
actual fun rememberMediaPicker(): MediaPicker? = remember { IosMediaPicker() }

private const val TYPE_IMAGE = "public.image"
private const val TYPE_JPEG = "public.jpeg"
private const val TYPE_PNG = "public.png"
private const val TYPE_MOVIE = "public.movie"

private class IosMediaPicker : MediaPicker {
    // UIKit keeps delegates weakly: hold on to them while their screen is up
    private var cameraDelegate: CameraDelegate? = null
    private var libraryDelegate: LibraryDelegate? = null
    private var previewSource: PreviewSource? = null

    override val hasCamera: Boolean =
        UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)

    override fun takePhoto(onPicked: (List<PickedMedia>) -> Unit) = openCamera(video = false, onPicked)

    override fun recordVideo(onPicked: (List<PickedMedia>) -> Unit) = openCamera(video = true, onPicked)

    private fun openCamera(video: Boolean, onPicked: (List<PickedMedia>) -> Unit) {
        if (!hasCamera) return onPicked(emptyList())
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.mediaTypes = listOf(if (video) TYPE_MOVIE else TYPE_IMAGE)
        if (video) {
            picker.cameraCaptureMode = UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModeVideo
            picker.videoQuality = UIImagePickerControllerQualityTypeHigh
            picker.videoMaximumDuration = 60.0 * 60
        }
        val delegate = CameraDelegate { result ->
            cameraDelegate = null
            onPicked(result)
        }
        cameraDelegate = delegate
        picker.delegate = delegate
        present(picker)
    }

    override fun pickFromLibrary(onPicked: (List<PickedMedia>, failed: Int) -> Unit) {
        // The system picker needs no access to the photo library: the app only gets what the attendee chooses
        val config = PHPickerConfiguration(photoLibrary = PHPhotoLibrary.sharedPhotoLibrary())
        config.selectionLimit = 0
        config.filter = PHPickerFilter.anyFilterMatchingSubfilters(
            listOf(PHPickerFilter.imagesFilter, PHPickerFilter.videosFilter)
        )
        // HEIC photos arrive as JPEG and HEVC videos as H.264, which every recipient can open
        config.preferredAssetRepresentationMode = PHPickerConfigurationAssetRepresentationModeCompatible
        val picker = PHPickerViewController(configuration = config)
        val delegate = LibraryDelegate { result, failed ->
            libraryDelegate = null
            onPicked(result, failed)
        }
        libraryDelegate = delegate
        picker.delegate = delegate
        present(picker)
    }

    override fun open(path: String, kind: MediaKind) {
        val controller = QLPreviewController()
        val source = PreviewSource(listOf(PreviewItem(NSURL.fileURLWithPath(path))))
        previewSource = source
        controller.dataSource = source
        present(controller)
    }
}

private class CameraDelegate(
    private val done: (List<PickedMedia>) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val videoUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val picked = when {
            videoUrl != null -> copyVideo(videoUrl)
            image != null -> saveJpeg(image)
            else -> null
        }
        done(listOfNotNull(picked))
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        done(emptyList())
    }
}

private const val CONCURRENT_LOADS = 2
private const val MAX_ATTEMPTS = 3

private class LibraryDelegate(
    private val done: (List<PickedMedia>, failed: Int) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    // Main-thread bookkeeping for the queue of items to copy
    private var results: List<PHPickerResult> = emptyList()
    private var picked = arrayOfNulls<PickedMedia>(0)
    private var next = 0
    private var inFlight = 0
    private var failed = 0

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) return done(emptyList(), 0)
        picked = arrayOfNulls(results.size)
        repeat(CONCURRENT_LOADS) { startNext() }
    }

    /**
     * A few at a time: the Photos services that hand over the files have small memory limits, and asking for
     * hundreds at once gets them killed, after which every remaining load fails.
     */
    private fun startNext() {
        if (next >= results.size) {
            if (inFlight == 0) done(picked.filterNotNull(), failed)
            return
        }
        val index = next++
        inFlight++
        load(index, attempt = 1)
    }

    private fun load(index: Int, attempt: Int) {
        val provider = results[index].itemProvider
        val type = when {
            provider.hasItemConformingToTypeIdentifier(TYPE_MOVIE) -> TYPE_MOVIE
            provider.hasItemConformingToTypeIdentifier(TYPE_JPEG) -> TYPE_JPEG
            provider.hasItemConformingToTypeIdentifier(TYPE_PNG) -> TYPE_PNG
            provider.hasItemConformingToTypeIdentifier(TYPE_IMAGE) -> TYPE_IMAGE
            else -> return finish(index, null)
        }
        // The file is only there while the handler runs: copy it right away
        provider.loadFileRepresentationForTypeIdentifier(type) { url, _ ->
            val copy = url?.let { autoreleasepool { copyPicked(it, type) } }
            dispatch_async(dispatch_get_main_queue()) {
                if (copy == null && attempt < MAX_ATTEMPTS) {
                    // Most likely a Photos service restarting: wait a moment and ask again
                    dispatch_after(
                        dispatch_time(DISPATCH_TIME_NOW, attempt * 1_500_000_000L),
                        dispatch_get_main_queue(),
                    ) { load(index, attempt + 1) }
                } else {
                    finish(index, copy)
                }
            }
        }
    }

    private fun finish(index: Int, copy: PickedMedia?) {
        picked[index] = copy
        if (copy == null) failed++
        inFlight--
        startNext()
    }

    private fun copyPicked(url: NSURL, type: String): PickedMedia? = when (type) {
        TYPE_MOVIE -> copyVideo(url)
        TYPE_JPEG, TYPE_PNG -> copyFile(url, url.pathExtension?.ifEmpty { null } ?: if (type == TYPE_PNG) "png" else "jpg")
            ?.let { PickedMedia(it, MediaKind.PHOTO, it.substringAfterLast('.')) }
        // Another image format (rare once HEIC is converted): re-encode it as JPEG
        else -> url.path?.let { path -> UIImage.imageWithContentsOfFile(path)?.let { saveJpeg(it) } }
    }
}

private class PreviewItem(private val url: NSURL) : NSObject(), QLPreviewItemProtocol {
    override fun previewItemURL(): NSURL = url
    override fun previewItemTitle(): String = ""
}

private class PreviewSource(private val items: List<PreviewItem>) : NSObject(), QLPreviewControllerDataSourceProtocol {
    override fun numberOfPreviewItemsInPreviewController(controller: QLPreviewController): NSInteger = items.size.toLong()
    override fun previewController(controller: QLPreviewController, previewItemAtIndex: NSInteger): QLPreviewItemProtocol =
        items[previewItemAtIndex.toInt().coerceIn(0, items.lastIndex)]
}

private fun tempPath(extension: String): String =
    NSTemporaryDirectory().trimEnd('/') + "/" + NSUUID().UUIDString + "." + extension.lowercase()

private fun copyFile(url: NSURL, extension: String): String? {
    val target = tempPath(extension)
    val copied = NSFileManager.defaultManager.copyItemAtURL(url, toURL = NSURL.fileURLWithPath(target), error = null)
    return if (copied) target else null
}

private fun copyVideo(url: NSURL): PickedMedia? {
    val extension = url.pathExtension?.ifEmpty { null } ?: "mov"
    val path = copyFile(url, extension) ?: return null
    return PickedMedia(path, MediaKind.VIDEO, extension.lowercase(), thumbnailPath = videoStill(path))
}

/** A camera photo drawn upright, so viewers that ignore the orientation flag show it the right way up. */
private fun saveJpeg(image: UIImage): PickedMedia? {
    val format = UIGraphicsImageRendererFormat.defaultFormat()
    format.scale = image.scale
    val upright = image.size.useContents {
        val size = CGSizeMake(width, height)
        UIGraphicsImageRenderer(size = size, format = format).imageWithActions {
            image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        }
    }
    val path = tempPath("jpg")
    val data = UIImageJPEGRepresentation(upright, 0.85) ?: return null
    return if (data.writeToFile(path, atomically = true)) PickedMedia(path, MediaKind.PHOTO, "jpg") else null
}

/** A small still from the first second of a video, for the list on the session page. */
private fun videoStill(path: String): String? {
    val generator = AVAssetImageGenerator(asset = AVURLAsset(uRL = NSURL.fileURLWithPath(path), options = null))
    generator.appliesPreferredTrackTransform = true
    generator.maximumSize = CGSizeMake(480.0, 480.0)
    val still = generator.copyCGImageAtTime(CMTimeMakeWithSeconds(1.0, 600), actualTime = null, error = null)
        ?: generator.copyCGImageAtTime(CMTimeMakeWithSeconds(0.0, 600), actualTime = null, error = null)
        ?: return null
    val data = UIImageJPEGRepresentation(UIImage.imageWithCGImage(still), 0.8)
    CGImageRelease(still)
    data ?: return null
    val target = tempPath("jpg")
    return if (data.writeToFile(target, atomically = true)) target else null
}

private fun present(controller: UIViewController) {
    dispatch_async(dispatch_get_main_queue()) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@dispatch_async
        var top: UIViewController = root
        while (true) top = top.presentedViewController ?: break
        top.presentViewController(controller, animated = true, completion = null)
    }
}
