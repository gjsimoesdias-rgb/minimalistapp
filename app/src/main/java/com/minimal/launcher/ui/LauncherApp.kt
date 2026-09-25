package com.minimal.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.minimal.launcher.LauncherState
import com.minimal.launcher.Screen

val Muted = Color(0xFF8A8A8A)
val Faint = Color(0xFF4A4A4A)

private val Colors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF141414),
    onSurface = Color.White,
    surfaceContainer = Color(0xFF141414),
)

@Composable
fun LauncherApp(state: LauncherState) {
    MaterialTheme(colorScheme = Colors) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                when (state.screen) {
                    Screen.Home -> HomeScreen(state)
                    Screen.Drawer -> AppDrawer(state)
                    Screen.Settings -> SettingsScreen(state)
                }
                state.pendingLaunch?.let { app ->
                    MindfulOverlay(
                        app = app,
                        seconds = state.mindfulSeconds,
                        onOpen = { state.openNow(app) },
                        onCancel = { state.pendingLaunch = null },
                    )
                }
            }
        }
    }

    // Always enabled: back on the home screen does nothing, like any launcher.
    BackHandler {
        when {
            state.pendingLaunch != null -> state.pendingLaunch = null
            state.screen != Screen.Home -> state.goHome()
        }
    }
}

/** Click without ripple, for a calm text-only UI. */
fun Modifier.plainClickable(onClick: () -> Unit): Modifier =
    clickable(interactionSource = null, indication = null, onClick = onClick)
