package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.ScreenWithTitle
import org.jetbrains.kotlinconf.event.ActiveEvent
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.about_app_description_sessionize
import org.jetbrains.kotlinconf.generated.resources.about_app_feature_notes
import org.jetbrains.kotlinconf.generated.resources.about_app_feature_plan
import org.jetbrains.kotlinconf.generated.resources.about_app_feature_private
import org.jetbrains.kotlinconf.generated.resources.about_app_link_github
import org.jetbrains.kotlinconf.generated.resources.about_app_link_licenses
import org.jetbrains.kotlinconf.generated.resources.about_app_link_rate
import org.jetbrains.kotlinconf.generated.resources.about_app_name
import org.jetbrains.kotlinconf.generated.resources.about_app_tagline
import org.jetbrains.kotlinconf.generated.resources.about_app_title
import org.jetbrains.kotlinconf.generated.resources.about_app_unofficial
import org.jetbrains.kotlinconf.generated.resources.app_version
import org.jetbrains.kotlinconf.generated.resources.arrow_up_right_24
import org.jetbrains.kotlinconf.ui.components.PageMenuItem
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.utils.getStoreUrl

@Composable
fun AboutAppScreen(
    onBack: () -> Unit,
    onGitHubRepo: () -> Unit,
    onRateApp: () -> Unit,
    onLicenses: () -> Unit,
) {
    ScreenWithTitle(
        title = stringResource(Res.string.about_app_title),
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(Res.string.about_app_name), style = AppTheme.typography.h1)
                Text(stringResource(Res.string.about_app_tagline), style = AppTheme.typography.text1, color = AppTheme.colors.longText)
            }

            Column(
                modifier = Modifier
                    .clip(AppTheme.shapes.roundedCornerMd)
                    .background(AppTheme.colors.tileBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(stringResource(Res.string.about_app_feature_plan), color = AppTheme.colors.longText)
                Text(stringResource(Res.string.about_app_feature_notes), color = AppTheme.colors.longText)
                Text(stringResource(Res.string.about_app_feature_private), color = AppTheme.colors.longText)
            }

            Text(
                stringResource(Res.string.about_app_description_sessionize, ActiveEvent.name),
                style = AppTheme.typography.text2,
                color = AppTheme.colors.noteText,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )

            PageMenuItem(
                stringResource(Res.string.about_app_link_github),
                drawableEnd = Res.drawable.arrow_up_right_24,
                onClick = onGitHubRepo,
            )

            val storeUrlAvailable = remember { getStoreUrl() != null }
            if (storeUrlAvailable) {
                PageMenuItem(
                    stringResource(Res.string.about_app_link_rate),
                    drawableEnd = Res.drawable.arrow_up_right_24,
                    onClick = onRateApp,
                )
            }

            PageMenuItem(stringResource(Res.string.about_app_link_licenses), onClick = onLicenses)

            Text(
                stringResource(Res.string.about_app_unofficial),
                style = AppTheme.typography.text2,
                color = AppTheme.colors.noteText,
                modifier = Modifier.padding(top = 16.dp),
            )

            Spacer(Modifier.height(8.dp))

            val clipboardManager = LocalClipboardManager.current
            val appVersion = stringResource(resource = Res.string.app_version)
            Text(
                text = appVersion,
                style = AppTheme.typography.text2,
                color = AppTheme.colors.primaryText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(AppTheme.shapes.roundedCornerMd)
                    .clickable { clipboardManager.setText(AnnotatedString(appVersion)) }
                    .padding(16.dp)
            )
        }
    }
}
