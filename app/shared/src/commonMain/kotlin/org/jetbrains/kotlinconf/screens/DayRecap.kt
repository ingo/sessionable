package org.jetbrains.kotlinconf.screens

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.kotlinconf.SessionCardView
import org.jetbrains.kotlinconf.Speaker
import org.jetbrains.kotlinconf.export.ExportSnapshot
import org.jetbrains.kotlinconf.personal.MediaKind
import org.jetbrains.kotlinconf.personal.Thumb
import kotlin.time.Instant

/** One session of the day with what the attendee kept for it. */
data class RecapSession(
    val session: SessionCardView,
    val thumb: Thumb?,
    val takeaway: String?,
    val hasNote: Boolean,
    val photos: Int,
    val videos: Int,
)

/** The look back at a conference day, shown on the Now tab once its last bookmarked session is over. */
data class DayRecap(
    val date: LocalDate,
    val attended: List<RecapSession>,
    val notes: Int,
    /** Photos and videos added today, anywhere in the event. */
    val photosToday: Int,
    val videosToday: Int,
    /** The speakers of today's sessions that were marked as met. */
    val met: List<Speaker>,
    /** There is another conference day after this one. */
    val hasTomorrow: Boolean,
    /** The first bookmarked session of the next conference day, if there is one. */
    val tomorrow: SessionCardView?,
)

/**
 * The recap for the day shown in [upNext], or null while the day is still going (a session is on or still to come
 * among the bookmarked ones) or when the day is not today. It appears after the last bookmarked session; the next
 * morning the day has changed, so it is gone. A day on which nothing was bookmarked and nothing is left on the
 * programme gives an empty recap, for a friendly empty state.
 */
fun dayRecap(upNext: UpNext, days: List<MyScheduleDay>, snapshot: ExportSnapshot, zone: TimeZone): DayRecap? {
    if (upNext.moment != ConferenceMoment.Today) return null
    if (upNext.current.isNotEmpty() || upNext.next.isNotEmpty() || upNext.later.isNotEmpty()) return null
    val over = upNext.earlier.isNotEmpty() || upNext.day.sessions.all { it.endsAt <= upNext.at }
    if (!over) return null

    val date = upNext.day.date
    val attended = upNext.earlier.map { session ->
        val rating = snapshot.ratings[session.id.id]
        val media = snapshot.attachments[session.id.id].orEmpty()
        RecapSession(
            session = session,
            thumb = rating?.thumb,
            takeaway = rating?.takeaway?.takeIf { it.isNotBlank() },
            hasNote = snapshot.notes.sessionNote(session.id).isNotBlank(),
            photos = media.count { it.kind == MediaKind.PHOTO },
            videos = media.count { it.kind == MediaKind.VIDEO },
        )
    }
    val addedToday = snapshot.attachments.values.flatten().filter {
        Instant.fromEpochMilliseconds(it.addedAt).toLocalDateTime(zone).date == date
    }
    val speakerIds = attended.flatMap { it.session.speakerIds }.toSet()
    val tomorrowDay = days.filter { it.date > date }.minByOrNull { it.date }
    return DayRecap(
        date = date,
        attended = attended,
        notes = attended.count { it.hasNote },
        photosToday = addedToday.count { it.kind == MediaKind.PHOTO },
        videosToday = addedToday.count { it.kind == MediaKind.VIDEO },
        met = speakerIds.filter { it in snapshot.met }.mapNotNull { snapshot.speakers[it] }.sortedBy { it.name.lowercase() },
        hasTomorrow = tomorrowDay != null,
        tomorrow = tomorrowDay?.booked?.map { it.session }?.minWithOrNull(compareBy({ it.startsAt }, { it.endsAt })),
    )
}
