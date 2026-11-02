package org.jetbrains.kotlinconf.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import org.jetbrains.kotlinconf.ConferenceService
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.SessionState
import org.jetbrains.kotlinconf.Speaker
import org.jetbrains.kotlinconf.SpeakerId
import org.jetbrains.kotlinconf.export.ExportSnapshot
import org.jetbrains.kotlinconf.isServiceEvent
import org.jetbrains.kotlinconf.personal.Attachment

/** What the attendee did on one conference day. */
data class DayStats(val date: LocalDate, val bookmarked: Int, val attended: Int, val rated: Int)

/** A note or takeaway, with the session or the speaker it is about. */
data class NoteItem(
    val sessionId: SessionId?,
    val speakerId: SpeakerId?,
    val title: String,
    val note: String,
    val takeaway: String?,
) {
    fun matches(query: String): Boolean =
        query.isBlank() || listOfNotNull(title, note, takeaway).any { it.contains(query.trim(), ignoreCase = true) }
}

/** A photo or video, with the session it is attached to. */
data class MediaItem(val sessionId: SessionId, val title: String, val attachment: Attachment, val thumbnail: String?)

/** Everything personal kept for the event, read from what is already stored. */
data class MyEventContents(
    val days: List<DayStats> = emptyList(),
    val notes: List<NoteItem> = emptyList(),
    val media: List<MediaItem> = emptyList(),
    val met: List<Speaker> = emptyList(),
    val loaded: Boolean = false,
)

@ContributesIntoMap(AppScope::class)
@ViewModelKey
class MyEventViewModel(private val service: ConferenceService) : ViewModel() {
    val contents: StateFlow<MyEventContents> = service.exportSnapshots
        .map(::build)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyEventContents())

    fun pathOf(file: String): String? = service.mediaFiles?.path(file)

    private fun build(snap: ExportSnapshot): MyEventContents {
        val talks = snap.sessions.filterNot { it.isServiceEvent }
        val days = talks.groupBy { it.startsAt.date }.entries.sortedBy { it.key }.map { (date, sessions) ->
            DayStats(
                date = date,
                bookmarked = sessions.count { it.isFavorite },
                attended = sessions.count { it.isFavorite && it.state == SessionState.Past },
                rated = sessions.count { snap.ratings[it.id.id]?.isEmpty == false },
            )
        }
        val byStart = talks.sortedWith(compareBy({ it.startsAt }, { it.title }))
        val sessionNotes = byStart.mapNotNull { s ->
            val note = snap.notes.sessionNote(s.id)
            val takeaway = snap.ratings[s.id.id]?.takeaway?.takeIf { it.isNotBlank() }
            if (note.isBlank() && takeaway == null) null else NoteItem(s.id, null, s.title, note, takeaway)
        }
        val speakerNotes = snap.speakers.values.sortedBy { it.name.lowercase() }.mapNotNull { sp ->
            snap.notes.speakerNote(sp.id).takeIf { it.isNotBlank() }?.let { NoteItem(null, sp.id, sp.name, it, null) }
        }
        val media = byStart.flatMap { s ->
            snap.attachments[s.id.id].orEmpty().sortedBy { it.addedAt }.map { MediaItem(s.id, s.title, it, pathOf(it.preview)) }
        }
        return MyEventContents(
            days = days,
            notes = sessionNotes + speakerNotes,
            media = media,
            met = snap.met.mapNotNull { snap.speakers[it] }.sortedBy { it.name.lowercase() },
            loaded = true,
        )
    }
}
