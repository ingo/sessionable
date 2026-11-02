package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.ScreenWithTitle
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.SpeakerId
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.my_event_day
import org.jetbrains.kotlinconf.generated.resources.my_event_days
import org.jetbrains.kotlinconf.generated.resources.my_event_media
import org.jetbrains.kotlinconf.generated.resources.my_event_media_empty
import org.jetbrains.kotlinconf.generated.resources.my_event_met
import org.jetbrains.kotlinconf.generated.resources.my_event_met_empty
import org.jetbrains.kotlinconf.generated.resources.my_event_notes
import org.jetbrains.kotlinconf.generated.resources.my_event_notes_empty
import org.jetbrains.kotlinconf.generated.resources.my_event_notes_none_found
import org.jetbrains.kotlinconf.generated.resources.my_event_search
import org.jetbrains.kotlinconf.generated.resources.my_event_title
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_open
import org.jetbrains.kotlinconf.personal.MediaKind
import org.jetbrains.kotlinconf.ui.components.Button
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.utils.DateTimeFormatting

/**
 * Everything the attendee kept for the event in one place: what they did each day, every note and takeaway (searchable),
 * every photo and video, and the people they met. Each item opens the session or speaker it belongs to.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyEventScreen(
    onBack: () -> Unit,
    onSession: (SessionId) -> Unit,
    onSpeaker: (SpeakerId) -> Unit,
    onTripReport: () -> Unit,
    viewModel: MyEventViewModel = metroViewModel(),
) {
    val contents by viewModel.contents.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    ScreenWithTitle(title = stringResource(Res.string.my_event_title), onBack = onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 24.dp)) {
            if (!contents.loaded) return@Column

            Button(stringResource(Res.string.now_trip_report_open), onClick = onTripReport, primary = true, modifier = Modifier.fillMaxWidth())

            Heading(stringResource(Res.string.my_event_days))
            contents.days.forEach { day ->
                Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(DateTimeFormatting.weekdayDate(day.date), style = AppTheme.typography.text1)
                    Text(
                        stringResource(Res.string.my_event_day, day.bookmarked, day.attended, day.rated),
                        style = AppTheme.typography.text2,
                        color = AppTheme.colors.noteText,
                    )
                }
            }

            Heading(stringResource(Res.string.my_event_notes))
            if (contents.notes.isEmpty()) {
                Text(stringResource(Res.string.my_event_notes_empty), color = AppTheme.colors.noteText)
            } else {
                SearchBox(query, onChange = { query = it }, hint = stringResource(Res.string.my_event_search))
                val shown = contents.notes.filter { it.matches(query) }
                if (shown.isEmpty()) {
                    Text(stringResource(Res.string.my_event_notes_none_found), color = AppTheme.colors.noteText)
                }
                shown.forEach { item ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) {
                                item.sessionId?.let(onSession)
                                item.speakerId?.let(onSpeaker)
                            }
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(item.title, style = AppTheme.typography.text1)
                        item.takeaway?.let { Text("“$it”", style = AppTheme.typography.text2, color = AppTheme.colors.longText) }
                        if (item.note.isNotBlank()) Text(item.note, style = AppTheme.typography.text2, color = AppTheme.colors.noteText, maxLines = 4)
                    }
                }
            }

            Heading(stringResource(Res.string.my_event_media))
            if (contents.media.isEmpty()) {
                Text(stringResource(Res.string.my_event_media_empty), color = AppTheme.colors.noteText)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    contents.media.forEach { item ->
                        Box(
                            Modifier
                                .size(84.dp)
                                .clip(AppTheme.shapes.roundedCornerSm)
                                .background(AppTheme.colors.tileBackground)
                                .semantics { contentDescription = item.title }
                                .clickable(role = Role.Image) { onSession(item.sessionId) },
                            contentAlignment = Alignment.Center,
                        ) {
                            item.thumbnail?.let {
                                AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                            if (item.attachment.kind == MediaKind.VIDEO) {
                                Box(
                                    Modifier.size(26.dp).clip(CircleShape).background(AppTheme.colors.primaryBackground),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("▶", style = AppTheme.typography.text2, color = AppTheme.colors.primaryTextWhiteFixed)
                                }
                            }
                        }
                    }
                }
            }

            Heading(stringResource(Res.string.my_event_met))
            if (contents.met.isEmpty()) {
                Text(stringResource(Res.string.my_event_met_empty), color = AppTheme.colors.noteText)
            } else {
                contents.met.forEach { speaker ->
                    Column(
                        Modifier.fillMaxWidth().clickable(role = Role.Button) { onSpeaker(speaker.id) }.padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(speaker.name, style = AppTheme.typography.text1)
                        speaker.position.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.text2, color = AppTheme.colors.noteText) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Heading(text: String) {
    Text(text, style = AppTheme.typography.h2, modifier = Modifier.padding(top = 16.dp).semantics { heading() })
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit, hint: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.roundedCornerMd)
            .background(AppTheme.colors.tileBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = AppTheme.typography.text1.copy(color = AppTheme.colors.primaryText),
            cursorBrush = SolidColor(AppTheme.colors.primaryText),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = hint },
        )
        if (value.isEmpty()) Text(hint, style = AppTheme.typography.text1, color = AppTheme.colors.placeholderText)
    }
}
