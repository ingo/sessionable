package org.jetbrains.kotlinconf.export

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.jetbrains.kotlinconf.Session
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.asSessionCard
import org.jetbrains.kotlinconf.personal.Attachment
import org.jetbrains.kotlinconf.personal.MediaKind
import org.jetbrains.kotlinconf.personal.PersonalNotes
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Builds a real report zip from a session with a photo, and reads it back. */
class ReportBundlerTest {
    @Test
    fun theZipHoldsTheReportAndThePhotosAttachedToSessions() = runBlocking {
        val dir = File.createTempFile("report-test", "").apply { delete(); mkdirs() }
        val photo = File(dir, "p1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val start = LocalDateTime.parse("2026-10-22T09:00")
        val session = Session(
            id = SessionId("1"), title = "Opening", description = "", speakerIds = emptyList(), location = "A",
            startsAt = start, endsAt = LocalDateTime.parse("2026-10-22T09:50"),
        ).asSessionCard(emptyMap(), LocalDateTime(2026, 10, 1, 0, 0), setOf(SessionId("1")))
        val snapshot = ExportSnapshot(
            sessions = listOf(session), speakers = emptyMap(), notes = PersonalNotes(), ratings = emptyMap(), met = emptySet(),
            attachments = mapOf("1" to listOf(Attachment("a", MediaKind.PHOTO, "p1.jpg", addedAt = 1))),
        )
        val event = ExportEvent("test-conf", "Test Conf", TimeZone.UTC)
        val bundler = OkioReportBundler(FileSystem.SYSTEM, (dir.path + "/out").toPath(), download = { _, _ -> false })

        val result = bundler.build(ReportInput(snapshot, event, ExportLabels()) { photo.path }) {}

        assertIs<ReportResult.Done>(result)
        assertEquals(1, result.photos)
        ZipFile(result.path).use { zip ->
            val names = zip.entries().asSequence().map { it.name }.toSet()
            assertTrue("report.md" in names && "report.html" in names, names.toString())
            assertTrue("media/2026-10-22-0900-opening/01.jpg" in names, names.toString())
        }
        dir.deleteRecursively()
        Unit
    }
}
