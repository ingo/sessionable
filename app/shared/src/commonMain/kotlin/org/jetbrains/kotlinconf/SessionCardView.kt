package org.jetbrains.kotlinconf

import kotlinx.datetime.LocalDateTime
import org.jetbrains.kotlinconf.utils.DateTimeFormatting
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class SessionCardView(
    val id: SessionId,
    val title: String,
    val speakerLine: String,
    val locationLine: String,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val state: SessionState,
    val speakerIds: List<SpeakerId>,
    val isFavorite: Boolean,
    val description: String,
    val tags: Set<String>,
    val startsInMinutes: Int?,
    val videoUrl: String?,
    /** Where to watch the session live, when there is a link. */
    val liveUrl: String? = null,
    val slidesUrl: String? = null,
    /** The speakers' companies, when the event collects them. */
    val companies: List<String> = emptyList(),
    /** Sessionize's mark for a break, lunch or registration. */
    val isServiceSession: Boolean = false,
    /** A session for everyone, such as a keynote. */
    val isPlenary: Boolean = false,
) {
    val fullTimeline: String = DateTimeFormatting.dateAndTime(startsAt, endsAt)
    val shortTimeline: String = DateTimeFormatting.timeToTime(startsAt, endsAt)
    val isLightning: Boolean = endsAt - startsAt <= LIGHTNING_TALK_LIMIT

    /** The time, and the room when there is one: sessions without a room yet show only the time. */
    val timelineWithRoom: String = if (locationLine.isBlank()) shortTimeline else "$shortTimeline · $locationLine"
}

val SessionCardView.isLive get() = state == SessionState.Live

/** Not a talk: marked by Sessionize, or (for older data and events that do not mark them) without speakers or tags. */
val SessionCardView.isServiceEvent: Boolean
    get() = isServiceSession || (speakerIds.isEmpty() && tags.isEmpty())

val Session.isLightning: Boolean
    get() = endsAt - startsAt <= LIGHTNING_TALK_LIMIT

// Maximum duration of a talk for it to be considered a lighting talk
private val LIGHTNING_TALK_LIMIT: Duration = 15.minutes

