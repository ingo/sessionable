package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_open
import org.jetbrains.kotlinconf.generated.resources.recap_added
import org.jetbrains.kotlinconf.generated.resources.recap_attended
import org.jetbrains.kotlinconf.generated.resources.recap_empty
import org.jetbrains.kotlinconf.generated.resources.recap_last_day
import org.jetbrains.kotlinconf.generated.resources.recap_met
import org.jetbrains.kotlinconf.generated.resources.recap_notes
import org.jetbrains.kotlinconf.generated.resources.recap_title
import org.jetbrains.kotlinconf.generated.resources.recap_tomorrow
import org.jetbrains.kotlinconf.generated.resources.recap_tomorrow_none
import org.jetbrains.kotlinconf.generated.resources.trip_report_photos
import org.jetbrains.kotlinconf.generated.resources.trip_report_videos
import org.jetbrains.kotlinconf.personal.Thumb
import org.jetbrains.kotlinconf.ui.components.Button
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.utils.DateTimeFormatting

/** The day looked back on once its last bookmarked session is over, and a look ahead to tomorrow. */
@Composable
internal fun DayRecapCard(
    recap: DayRecap,
    onSession: (SessionId) -> Unit,
    onTripReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.roundedCornerMd)
            .background(AppTheme.colors.tileBackground)
            .padding(16.dp),
    ) {
        Text(stringResource(Res.string.recap_title), style = AppTheme.typography.h2, modifier = Modifier.semantics { heading() })
        Text(DateTimeFormatting.weekdayDate(recap.date), style = AppTheme.typography.text2, color = AppTheme.colors.noteText)

        if (recap.attended.isEmpty()) {
            Text(stringResource(Res.string.recap_empty), color = AppTheme.colors.longText)
        } else {
            Text(stringResource(Res.string.recap_attended), style = AppTheme.typography.h3, modifier = Modifier.padding(top = 6.dp).semantics { heading() })
            recap.attended.forEach { RecapRow(it, onClick = { onSession(it.session.id) }) }

            val added = listOfNotNull(
                recap.notes.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.recap_notes, it, it) },
                recap.photosToday.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.trip_report_photos, it, it) },
                recap.videosToday.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.trip_report_videos, it, it) },
            )
            if (added.isNotEmpty()) {
                Text(stringResource(Res.string.recap_added, added.joinToString(" · ")), style = AppTheme.typography.text2, color = AppTheme.colors.longText)
            }
            if (recap.met.isNotEmpty()) {
                Text(stringResource(Res.string.recap_met, recap.met.joinToString(", ") { it.name }), style = AppTheme.typography.text2, color = AppTheme.colors.longText)
            }
        }

        if (recap.hasTomorrow) {
            Text(stringResource(Res.string.recap_tomorrow), style = AppTheme.typography.h3, modifier = Modifier.padding(top = 6.dp).semantics { heading() })
            val first = recap.tomorrow
            if (first == null) {
                Text(stringResource(Res.string.recap_tomorrow_none), style = AppTheme.typography.text2, color = AppTheme.colors.noteText)
            } else {
                Column(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { onSession(first.id) }.padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(first.title, style = AppTheme.typography.text1)
                    Text(first.timelineWithRoom, style = AppTheme.typography.text2, color = AppTheme.colors.noteText)
                }
            }
        } else {
            Text(stringResource(Res.string.recap_last_day), color = AppTheme.colors.longText, modifier = Modifier.padding(top = 6.dp))
            Button(
                stringResource(Res.string.now_trip_report_open),
                onClick = onTripReport,
                primary = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RecapRow(item: RecapSession, onClick: () -> Unit) {
    val thumb = item.thumb?.let { if (it == Thumb.UP) "👍" else "👎" }
    Column(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(listOfNotNull(thumb, item.session.title).joinToString(" "), style = AppTheme.typography.text1)
        Text(item.session.shortTimeline, style = AppTheme.typography.text2, color = AppTheme.colors.noteText)
        item.takeaway?.let { Text("“$it”", style = AppTheme.typography.text2, color = AppTheme.colors.longText) }
    }
}
