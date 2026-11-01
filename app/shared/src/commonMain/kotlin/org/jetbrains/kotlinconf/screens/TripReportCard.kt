package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.export.TripReportSummary
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_empty
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_share
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_text
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_title
import org.jetbrains.kotlinconf.generated.resources.trip_stat_met
import org.jetbrains.kotlinconf.generated.resources.trip_stat_notes
import org.jetbrains.kotlinconf.generated.resources.trip_stat_rated
import org.jetbrains.kotlinconf.generated.resources.trip_stat_sessions
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme

/** The trip report on the Now tab once the event is over: a summary of it, and the way to send it to yourself. */
@Composable
internal fun TripReportCard(summary: TripReportSummary, onShare: () -> Unit, modifier: Modifier = Modifier) {
    val white = AppTheme.colors.primaryTextWhiteFixed
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.roundedCornerMd)
            .background(AppTheme.colors.primaryBackground)
            .padding(20.dp),
    ) {
        Text(
            stringResource(Res.string.now_trip_report_title),
            style = AppTheme.typography.h2,
            color = white,
            modifier = Modifier.semantics { heading() },
        )
        if (summary.sessions == 0) {
            Text(stringResource(Res.string.now_trip_report_empty), style = AppTheme.typography.text1, color = white.copy(alpha = 0.9f))
            return@Column
        }
        Text(stringResource(Res.string.now_trip_report_text), style = AppTheme.typography.text2, color = white.copy(alpha = 0.9f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(summary.sessions, Res.string.trip_stat_sessions, Modifier.weight(1f))
            Stat(summary.notes, Res.string.trip_stat_notes, Modifier.weight(1f))
            Stat(summary.rated, Res.string.trip_stat_rated, Modifier.weight(1f))
            Stat(summary.met, Res.string.trip_stat_met, Modifier.weight(1f))
        }
        Text(
            stringResource(Res.string.now_trip_report_share),
            style = AppTheme.typography.h4.copy(textAlign = TextAlign.Center),
            color = AppTheme.colors.primaryBackground,
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.roundedCornerMd)
                .background(white)
                .clickable(role = Role.Button, onClick = onShare)
                .padding(vertical = 14.dp),
        )
    }
}

@Composable
private fun Stat(value: Int, label: StringResource, modifier: Modifier = Modifier) {
    val white = AppTheme.colors.primaryTextWhiteFixed
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = AppTheme.typography.h1, color = white)
        Text(stringResource(label), style = AppTheme.typography.text2.copy(textAlign = TextAlign.Center), color = white.copy(alpha = 0.85f))
    }
}
