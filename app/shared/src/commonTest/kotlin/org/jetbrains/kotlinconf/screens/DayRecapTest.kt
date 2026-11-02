package org.jetbrains.kotlinconf.screens

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.kotlinconf.Conference
import org.jetbrains.kotlinconf.Session
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.buildAgenda
import org.jetbrains.kotlinconf.export.ExportSnapshot
import org.jetbrains.kotlinconf.personal.Attachment
import org.jetbrains.kotlinconf.personal.MediaKind
import org.jetbrains.kotlinconf.personal.PersonalNotes
import org.jetbrains.kotlinconf.personal.SessionRating
import org.jetbrains.kotlinconf.personal.Thumb
import kotlin.test.Test
import kotlin.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DayRecapTest {
    private fun session(id: String, day: String, start: String, end: String) = Session(
        id = SessionId(id),
        title = "Talk $id",
        description = "",
        speakerIds = emptyList(),
        location = "Room",
        startsAt = LocalDateTime.parse("2026-10-${day}T$start"),
        endsAt = LocalDateTime.parse("2026-10-${day}T$end"),
    )

    private val conference = Conference(
        listOf(
            session("a", "22", "10:00", "10:45"),
            session("b", "22", "14:00", "14:30"),
            session("c", "23", "09:30", "10:00"),
            session("d", "23", "11:00", "11:30"),
        )
    )

    private fun recap(
        now: String,
        favorites: Set<String>,
        ratings: Map<String, SessionRating> = emptyMap(),
        notes: PersonalNotes = PersonalNotes(),
        attachments: Map<String, List<Attachment>> = emptyMap(),
    ): DayRecap? {
        val time = LocalDateTime.parse(now)
        val agenda = conference.buildAgenda(favorites.map(::SessionId).toSet(), time)
        val days = buildMySchedule(agenda)
        val snapshot = ExportSnapshot(days.flatMap { it.sessions }, emptyMap(), notes, ratings, emptySet(), attachments)
        return upNext(days, time)?.let { dayRecap(it, days, snapshot, TimeZone.UTC) }
    }

    @Test
    fun noRecapWhileABookmarkedSessionIsStillToCome() {
        assertNull(recap("2026-10-22T12:00", setOf("a", "b")))
    }

    @Test
    fun noRecapDuringASession() {
        assertNull(recap("2026-10-22T14:10", setOf("a", "b")))
    }

    @Test
    fun noRecapBeforeTheConference() {
        assertNull(recap("2026-10-21T20:00", setOf("a", "b")))
    }

    @Test
    fun afterTheLastBookmarkedSessionItLooksBackAndAheadToTomorrow() {
        val recap = assertNotNull(
            recap(
                "2026-10-22T15:00", setOf("a", "b", "c"),
                ratings = mapOf("a" to SessionRating(Thumb.UP, "Use the registry")),
                notes = PersonalNotes().withSessionNote(SessionId("b"), "ask about auth"),
            )
        )

        assertEquals(listOf("a", "b"), recap.attended.map { it.session.id.id })
        assertEquals(Thumb.UP, recap.attended[0].thumb)
        assertEquals("Use the registry", recap.attended[0].takeaway)
        assertEquals(1, recap.notes)
        assertTrue(recap.hasTomorrow)
        assertEquals("c", recap.tomorrow?.id?.id)
    }

    @Test
    fun photosAndVideosAddedTodayAreCounted() {
        val today = Instant.parse("2026-10-22T11:00:00Z").toEpochMilliseconds()
        val yesterday = today - 24 * 60 * 60 * 1000
        val recap = assertNotNull(
            recap(
                "2026-10-22T15:00", setOf("a", "b"),
                attachments = mapOf(
                    "a" to listOf(
                        Attachment("p1", MediaKind.PHOTO, "p1.jpg", addedAt = today),
                        Attachment("v1", MediaKind.VIDEO, "v1.mov", addedAt = today),
                        Attachment("p0", MediaKind.PHOTO, "p0.jpg", addedAt = yesterday),
                    ),
                ),
            )
        )

        assertEquals(1, recap.photosToday)
        assertEquals(1, recap.videosToday)
    }

    @Test
    fun onTheLastDayThereIsNoTomorrow() {
        val recap = assertNotNull(recap("2026-10-23T12:00", setOf("c", "d")))

        assertEquals(false, recap.hasTomorrow)
        assertNull(recap.tomorrow)
    }

    @Test
    fun aDayWithNothingBookmarkedGivesAnEmptyRecapOnceTheProgrammeIsOver() {
        val recap = assertNotNull(recap("2026-10-22T18:00", emptySet()))

        assertEquals(emptyList(), recap.attended)
    }
}
