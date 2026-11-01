package org.jetbrains.kotlinconf.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.kotlinconf.generated.resources.Res
import org.jetbrains.kotlinconf.generated.resources.award_28
import org.jetbrains.kotlinconf.generated.resources.bookmark_28_fill
import org.jetbrains.kotlinconf.generated.resources.camera_24
import org.jetbrains.kotlinconf.generated.resources.clock_28
import org.jetbrains.kotlinconf.generated.resources.tour_1_text
import org.jetbrains.kotlinconf.generated.resources.tour_1_title
import org.jetbrains.kotlinconf.generated.resources.tour_2_text
import org.jetbrains.kotlinconf.generated.resources.tour_2_title
import org.jetbrains.kotlinconf.generated.resources.tour_3_text
import org.jetbrains.kotlinconf.generated.resources.tour_3_title
import org.jetbrains.kotlinconf.generated.resources.tour_4_text
import org.jetbrains.kotlinconf.generated.resources.tour_4_title
import org.jetbrains.kotlinconf.generated.resources.tour_done
import org.jetbrains.kotlinconf.generated.resources.tour_next
import org.jetbrains.kotlinconf.generated.resources.tour_skip
import org.jetbrains.kotlinconf.ui.components.Button
import org.jetbrains.kotlinconf.ui.components.Icon
import org.jetbrains.kotlinconf.ui.components.Text
import org.jetbrains.kotlinconf.ui.theme.AppTheme

private class TourPage(val icon: DrawableResource, val title: StringResource, val text: StringResource)

private val pages = listOf(
    TourPage(Res.drawable.bookmark_28_fill, Res.string.tour_1_title, Res.string.tour_1_text),
    TourPage(Res.drawable.clock_28, Res.string.tour_2_title, Res.string.tour_2_text),
    TourPage(Res.drawable.camera_24, Res.string.tour_3_title, Res.string.tour_3_text),
    TourPage(Res.drawable.award_28, Res.string.tour_4_title, Res.string.tour_4_text),
)

/** A four-card tour of what the app is for, shown once over the first screen. */
@Composable
internal fun TourScreen(onDone: () -> Unit) {
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(AppTheme.colors.mainBackground)
            // Swallows taps, so the screen behind cannot be used until the tour is done
            .clickable(enabled = false) {}
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (!last) {
                Text(
                    stringResource(Res.string.tour_skip),
                    style = AppTheme.typography.text1,
                    color = AppTheme.colors.noteText,
                    modifier = Modifier
                        .clip(AppTheme.shapes.roundedCornerMd)
                        .clickable(role = Role.Button, onClick = onDone)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth()) { index ->
            val page = pages[index]
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    Modifier.size(104.dp).clip(CircleShape).background(AppTheme.colors.primaryBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(page.icon),
                        contentDescription = null,
                        tint = AppTheme.colors.primaryTextWhiteFixed,
                        modifier = Modifier.size(48.dp),
                    )
                }
                Text(
                    stringResource(page.title),
                    style = AppTheme.typography.h1.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(page.text),
                    style = AppTheme.typography.text1.copy(textAlign = TextAlign.Center),
                    color = AppTheme.colors.longText,
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            repeat(pages.size) { index ->
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (index == pager.currentPage) AppTheme.colors.primaryBackground else AppTheme.colors.strokeHalf),
                )
            }
        }

        Button(
            label = stringResource(if (last) Res.string.tour_done else Res.string.tour_next),
            primary = true,
            onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
