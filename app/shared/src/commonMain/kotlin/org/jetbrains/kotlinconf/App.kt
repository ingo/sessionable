package org.jetbrains.kotlinconf

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import org.jetbrains.kotlinconf.di.AppGraph
import org.jetbrains.kotlinconf.flags.LocalFlags
import org.jetbrains.kotlinconf.navigation.NavHost
import org.jetbrains.kotlinconf.screens.ConnectScreen
import org.jetbrains.kotlinconf.ui.theme.DarkColors
import org.jetbrains.kotlinconf.ui.theme.LightColors
import org.jetbrains.kotlinconf.ui.theme.AppTheme
import org.jetbrains.kotlinconf.utils.LocalNotificationBar
import org.jetbrains.kotlinconf.utils.LocalWindowSize
import org.jetbrains.kotlinconf.utils.rememberNotificationBarState
import org.jetbrains.kotlinconf.utils.windowSize

@Composable
fun App(
    appGraph: AppGraph,
    onThemeChange: ((isDarkTheme: Boolean) -> Unit)? = null,
) {
    val service = appGraph.conferenceService
    val currentTheme by service.getTheme().collectAsStateWithLifecycle(initialValue = Theme.SYSTEM)
    val isDarkTheme = when (currentTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

    val isOnboardingComplete = service.isOnboardingComplete()
        .collectAsStateWithLifecycle(initialValue = null)
        .value

    val activeEvent by service.activeEvent.collectAsStateWithLifecycle()

    val flags by appGraph.flagsManager.flags.collectAsStateWithLifecycle()
    CompositionLocalProvider(
        LocalFlags provides flags,
        LocalAppGraph provides appGraph,
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
        LocalWindowSize provides windowSize(),
        LocalMapHandler provides rememberMapHandler(),
        LocalShareHandler provides rememberShareHandler(),
        LocalNotificationBar provides rememberNotificationBarState(),
    ) {
        if (isOnboardingComplete != null) {
            val event = activeEvent
            if (event == null) {
                val colors = if (isDarkTheme) DarkColors else LightColors
                if (onThemeChange != null) LaunchedEffect(colors) { onThemeChange(colors.isDark) }
                AppTheme(rippleEnabled = flags.rippleEnabled, colors = colors) { ConnectScreen() }
            } else {
                // A fresh navigation state for each conference
                key(event.id) { NavHost(isOnboardingComplete, isDarkTheme, onThemeChange) }
            }
        }
    }
}

public val LocalAppGraph: ProvidableCompositionLocal<AppGraph> =
    staticCompositionLocalOf {
        error("No AppGraph registered")
    }
