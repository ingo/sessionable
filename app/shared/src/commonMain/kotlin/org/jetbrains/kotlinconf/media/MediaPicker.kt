package org.jetbrains.kotlinconf.media

import androidx.compose.runtime.Composable
import org.jetbrains.kotlinconf.personal.MediaKind

/**
 * A photo or video the camera or the library handed over, as a temporary file that the app then moves
 * into [MediaFiles]. [thumbnailPath] is a still for a video, also temporary.
 */
class PickedMedia(
    val path: String,
    val kind: MediaKind,
    val extension: String,
    val thumbnailPath: String? = null,
)

/**
 * The platform's camera, photo library and full-screen viewer. Results arrive on the main thread;
 * an empty list means the attendee cancelled or nothing could be read.
 */
interface MediaPicker {
    /** Whether this device has a camera the app can use. */
    val hasCamera: Boolean

    fun takePhoto(onPicked: (List<PickedMedia>) -> Unit)

    fun recordVideo(onPicked: (List<PickedMedia>) -> Unit)

    /** Photos and videos from the library, several at a time. [onPicked] gets the copies and how many could not be read. */
    fun pickFromLibrary(onPicked: (List<PickedMedia>, failed: Int) -> Unit)

    /** Shows a stored photo or plays a stored video. */
    fun open(path: String, kind: MediaKind)
}

/** The picker for this platform, or null where the app keeps no photos or videos (the web build). */
@Composable
expect fun rememberMediaPicker(): MediaPicker?
