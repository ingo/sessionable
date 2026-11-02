package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.LocalShareHandler
import org.jetbrains.kotlinconf.ScreenWithTitle
import org.jetbrains.kotlinconf.SessionId
import org.jetbrains.kotlinconf.event.ActiveEvent
import org.jetbrains.kotlinconf.export.ExportEvent
import org.jetbrains.kotlinconf.export.ReportProgress
import org.jetbrains.kotlinconf.export.ReportResult
import org.jetbrains.kotlinconf.export.rememberExportLabels
import org.jetbrains.kotlinconf.export.rememberExporter
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.export_slides
import org.jetbrains.kotlinconf.generated.resources.now_trip_report_title
import org.jetbrains.kotlinconf.generated.resources.trip_report_download
import org.jetbrains.kotlinconf.generated.resources.trip_report_download_hint
import org.jetbrains.kotlinconf.generated.resources.trip_report_download_size
import org.jetbrains.kotlinconf.generated.resources.trip_report_downloading_slides
import org.jetbrains.kotlinconf.generated.resources.trip_report_empty
import org.jetbrains.kotlinconf.generated.resources.trip_report_failed
import org.jetbrains.kotlinconf.generated.resources.trip_report_in_report
import org.jetbrains.kotlinconf.generated.resources.trip_report_intro
import org.jetbrains.kotlinconf.generated.resources.trip_report_note
import org.jetbrains.kotlinconf.generated.resources.trip_report_packing
import org.jetbrains.kotlinconf.generated.resources.trip_report_photos
import org.jetbrains.kotlinconf.generated.resources.trip_report_preview
import org.jetbrains.kotlinconf.generated.resources.trip_report_rating
import org.jetbrains.kotlinconf.generated.resources.trip_report_ready
import org.jetbrains.kotlinconf.generated.resources.trip_report_sessions
import org.jetbrains.kotlinconf.generated.resources.trip_report_share_again
import org.jetbrains.kotlinconf.generated.resources.trip_report_share_text
import org.jetbrains.kotlinconf.generated.resources.trip_report_share_text_hint
import org.jetbrains.kotlinconf.generated.resources.trip_report_slides_linked
import org.jetbrains.kotlinconf.generated.resources.trip_report_too_large
import org.jetbrains.kotlinconf.generated.resources.trip_report_videos
import org.jetbrains.kotlinconf.generated.resources.trip_report_with_slides
import org.jetbrains.kotlinconf.ui.components.Button
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.utils.DateTimeFormatting

private const val ZIP_MIME_TYPE = "application/zip"

/**
 * The attendee's trip report: what is in it, a zip to download (report, slides and the photos and videos attached to
 * sessions) and the report as text.
 */
@Composable
fun TripReportScreen(
    onBack: () -> Unit,
    onSession: (SessionId) -> Unit,
    onPreview: () -> Unit,
    viewModel: TripReportViewModel = metroViewModel(),
) {
    val contents by viewModel.contents.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val share = LocalShareHandler.current
    val exporter = rememberExporter()
    val labels = rememberExportLabels()
    val event = ExportEvent(ActiveEvent.id, ActiveEvent.name, ActiveEvent.timeZone, ActiveEvent.venueAddress)
    val shareZip: (ReportResult.Done) -> Unit = { share.shareFileAt(it.path, ZIP_MIME_TYPE) }

    ScreenWithTitle(title = stringResource(Res.string.now_trip_report_title), onBack = onBack) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 24.dp),
        ) {
            Text(stringResource(Res.string.trip_report_intro), color = AppTheme.colors.longText)
            if (!contents.loaded) return@Column
            if (contents.items.isEmpty()) {
                Text(stringResource(Res.string.trip_report_empty), color = AppTheme.colors.noteText)
                return@Column
            }
            Text(
                listOf(
                    pluralStringResource(Res.plurals.trip_report_sessions, contents.items.size, contents.items.size),
                    pluralStringResource(Res.plurals.trip_report_with_slides, contents.withSlides, contents.withSlides),
                    pluralStringResource(Res.plurals.trip_report_photos, contents.photos, contents.photos),
                    pluralStringResource(Res.plurals.trip_report_videos, contents.videos, contents.videos),
                ).joinToString(" · "),
                style = AppTheme.typography.h3,
            )

            Button(
                stringResource(Res.string.trip_report_preview),
                onClick = onPreview,
                modifier = Modifier.fillMaxWidth(),
            )
            if (viewModel.canDownload) {
                val working = state is TripReportState.Working
                Button(
                    stringResource(Res.string.trip_report_download),
                    onClick = { viewModel.download(event, labels, shareZip) },
                    primary = true,
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(Res.string.trip_report_download_hint), color = AppTheme.colors.noteText)
                if (contents.mediaBytes > 0) {
                    Text(
                        stringResource(Res.string.trip_report_download_size, formatBytes(contents.mediaBytes)),
                        color = AppTheme.colors.noteText,
                    )
                }
                ReportStatus(state, onShareAgain = shareZip)
            }

            Button(
                stringResource(Res.string.trip_report_share_text),
                onClick = exporter::shareTripReport,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(Res.string.trip_report_share_text_hint), color = AppTheme.colors.noteText)

            Text(
                stringResource(Res.string.trip_report_in_report),
                style = AppTheme.typography.h3,
                modifier = Modifier.padding(top = 16.dp).semantics { heading() },
            )
            contents.items.forEach { item -> ReportRow(item, onClick = { onSession(item.session.id) }) }
        }
    }
}

@Composable
private fun ReportStatus(state: TripReportState, onShareAgain: (ReportResult.Done) -> Unit) {
    when (state) {
        TripReportState.Idle -> Unit
        is TripReportState.Working -> {
            val progress = state.progress
            Text(
                when (progress) {
                    is ReportProgress.Slides -> stringResource(Res.string.trip_report_downloading_slides, progress.done + 1, progress.total)
                    else -> stringResource(Res.string.trip_report_packing)
                },
                color = AppTheme.colors.longText,
            )
        }
        is TripReportState.Finished -> when (val result = state.result) {
            is ReportResult.Done -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.trip_report_ready, formatBytes(result.bytes)), color = AppTheme.colors.longText)
                if (result.slidesLinked > 0) {
                    Text(
                        pluralStringResource(Res.plurals.trip_report_slides_linked, result.slidesLinked, result.slidesLinked),
                        color = AppTheme.colors.noteText,
                    )
                }
                Button(
                    stringResource(Res.string.trip_report_share_again),
                    onClick = { onShareAgain(result) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ReportResult.TooLarge -> Text(stringResource(Res.string.trip_report_too_large), color = AppTheme.colors.orangeText)
            ReportResult.Failed -> Text(stringResource(Res.string.trip_report_failed), color = AppTheme.colors.orangeText)
        }
    }
}

@Composable
private fun ReportRow(item: TripReportItem, onClick: () -> Unit) {
    val details = listOfNotNull(
        DateTimeFormatting.weekdayDate(item.session.startsAt.date) + ", " +
            DateTimeFormatting.timeToTime(item.session.startsAt, item.session.endsAt),
        item.session.slidesUrl?.let { stringResource(Res.string.export_slides) },
        item.photos.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.trip_report_photos, it, it) },
        item.videos.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.trip_report_videos, it, it) },
        if (item.hasNote) stringResource(Res.string.trip_report_note) else null,
        if (item.hasRating) stringResource(Res.string.trip_report_rating) else null,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(item.session.title, style = AppTheme.typography.text1)
        Text(details.joinToString(" · "), style = AppTheme.typography.text2, color = AppTheme.colors.noteText)
    }
}
