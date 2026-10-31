package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.connect_check
import org.jetbrains.kotlinconf.generated.resources.connect_checking
import org.jetbrains.kotlinconf.generated.resources.connect_cancel
import org.jetbrains.kotlinconf.generated.resources.connect_forget
import org.jetbrains.kotlinconf.generated.resources.connect_forget_confirm
import org.jetbrains.kotlinconf.generated.resources.connect_forget_named
import org.jetbrains.kotlinconf.generated.resources.connect_found
import org.jetbrains.kotlinconf.generated.resources.connect_intro
import org.jetbrains.kotlinconf.generated.resources.connect_link_hint
import org.jetbrains.kotlinconf.generated.resources.connect_name_hint
import org.jetbrains.kotlinconf.generated.resources.connect_organiser_button
import org.jetbrains.kotlinconf.generated.resources.connect_organiser_text
import org.jetbrains.kotlinconf.generated.resources.connect_organiser_title
import org.jetbrains.kotlinconf.generated.resources.connect_name_label
import org.jetbrains.kotlinconf.generated.resources.connect_not_a_link
import org.jetbrains.kotlinconf.generated.resources.connect_not_found
import org.jetbrains.kotlinconf.generated.resources.connect_open
import org.jetbrains.kotlinconf.generated.resources.connect_privacy
import org.jetbrains.kotlinconf.generated.resources.connect_saved
import org.jetbrains.kotlinconf.generated.resources.connect_title
import org.jetbrains.kotlinconf.generated.resources.connect_zone_help
import org.jetbrains.kotlinconf.generated.resources.connect_zone_label
import org.jetbrains.kotlinconf.generated.resources.connect_zone_unknown
import org.jetbrains.kotlinconf.ui.components.Button
import org.jetbrains.kotlinconf.ui.components.HorizontalDivider
import org.jetbrains.kotlinconf.ui.components.MainHeaderTitleBar
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.utils.bottomInsetPadding
import org.jetbrains.kotlinconf.utils.topInsetPadding

/**
 * The first screen of the generic build: paste a Sessionize link, check it, say what the conference is called and
 * where it takes place, and open it.
 */
@Composable
fun ConnectScreen(viewModel: ConnectViewModel = metroViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saved by viewModel.savedEvents.collectAsStateWithLifecycle()
    var link by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var forgetting by rememberSaveable { mutableStateOf<String?>(null) }
    var zoneId by rememberSaveable { mutableStateOf(TimeZone.currentSystemDefault().id) }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppTheme.colors.mainBackground)
            .padding(topInsetPadding())
    ) {
        MainHeaderTitleBar(stringResource(Res.string.connect_title))
        HorizontalDivider(1.dp, AppTheme.colors.strokePale)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(bottomInsetPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(Res.string.connect_intro),
                color = AppTheme.colors.longText,
                modifier = Modifier.padding(top = 24.dp),
            )

            TextInput(
                value = link,
                onChange = {
                    link = it
                    viewModel.reset()
                },
                hint = stringResource(Res.string.connect_link_hint),
                keyboardType = KeyboardType.Uri,
            )
            Button(
                stringResource(Res.string.connect_check),
                onClick = { viewModel.check(link) },
                primary = uiState !is ConnectUiState.Found,
                enabled = link.isNotBlank() && uiState != ConnectUiState.Checking,
            )

            when (val state = uiState) {
                ConnectUiState.Idle -> Unit
                ConnectUiState.Checking ->
                    Text(
                        stringResource(Res.string.connect_checking),
                        color = AppTheme.colors.noteText,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                ConnectUiState.NotALink ->
                    Text(
                        stringResource(Res.string.connect_not_a_link),
                        color = AppTheme.colors.orangeText,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                ConnectUiState.NotFound ->
                    Text(
                        stringResource(Res.string.connect_not_found),
                        color = AppTheme.colors.orangeText,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                is ConnectUiState.Found -> {
                    Text(
                        stringResource(Res.string.connect_found, state.summary.sessions, state.summary.speakers),
                        color = AppTheme.colors.noteText,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    Heading(stringResource(Res.string.connect_name_label))
                    TextInput(name, onChange = { name = it }, hint = stringResource(Res.string.connect_name_hint))

                    Heading(stringResource(Res.string.connect_zone_label))
                    TimeZoneField(zoneId, onChange = { zoneId = it })

                    val zoneValid = remember(zoneId) { zoneId in TimeZone.availableZoneIds }
                    Button(
                        stringResource(Res.string.connect_open),
                        onClick = { viewModel.connect(state.sessionizeId, name, zoneId) },
                        primary = true,
                        enabled = name.isNotBlank() && zoneValid,
                    )
                }
            }

            val uriHandler = LocalUriHandler.current
            Heading(stringResource(Res.string.connect_organiser_title))
            Text(stringResource(Res.string.connect_organiser_text), color = AppTheme.colors.longText)
            Button(
                stringResource(Res.string.connect_organiser_button),
                onClick = { uriHandler.openUri("https://sessionize.com/tryit") },
            )

            if (saved.isNotEmpty()) {
                Heading(stringResource(Res.string.connect_saved))
                for (event in saved) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(event.name, onClick = { viewModel.open(event.sessionizeId) }, modifier = Modifier.weight(1f))
                        val forgetLabel = stringResource(Res.string.connect_forget_named, event.name)
                        Button(
                            stringResource(Res.string.connect_forget),
                            onClick = { forgetting = event.sessionizeId },
                            modifier = Modifier.semantics { contentDescription = forgetLabel },
                        )
                    }
                    if (forgetting == event.sessionizeId) {
                        Text(
                            stringResource(Res.string.connect_forget_confirm, event.name),
                            color = AppTheme.colors.orangeText,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                stringResource(Res.string.connect_forget),
                                onClick = {
                                    forgetting = null
                                    viewModel.forget(event.sessionizeId)
                                },
                                primary = true,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                stringResource(Res.string.connect_cancel),
                                onClick = { forgetting = null },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Text(
                stringResource(Res.string.connect_privacy),
                color = AppTheme.colors.noteText,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
    }
}

/** A time zone id, with the matching zones listed below while it is being typed. */
@Composable
private fun TimeZoneField(zoneId: String, onChange: (String) -> Unit) {
    TextInput(zoneId, onChange = onChange, hint = stringResource(Res.string.connect_zone_label))
    val valid = zoneId in TimeZone.availableZoneIds
    if (valid) {
        Text(stringResource(Res.string.connect_zone_help), color = AppTheme.colors.noteText)
        return
    }
    val matches = remember(zoneId) {
        val query = zoneId.trim().replace(' ', '_')
        if (query.isEmpty()) emptyList()
        else TimeZone.availableZoneIds.filter { it.contains(query, ignoreCase = true) }.sorted().take(6)
    }
    if (matches.isEmpty()) {
        Text(stringResource(Res.string.connect_zone_unknown), color = AppTheme.colors.orangeText)
    }
    for (match in matches) Button(match, onClick = { onChange(match) })
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = AppTheme.typography.h3,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
    )
}

@Composable
private fun TextInput(
    value: String,
    onChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = AppTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = AppTheme.typography.text1.copy(color = colors.primaryText),
        cursorBrush = SolidColor(colors.primaryText),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.strokeHalf, AppTheme.shapes.roundedCornerSm)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { if (value.isEmpty()) contentDescription = hint },
        decorationBox = { field ->
            if (value.isEmpty()) {
                Text(hint, style = AppTheme.typography.text1, color = colors.placeholderText)
            }
            field()
        },
    )
}
