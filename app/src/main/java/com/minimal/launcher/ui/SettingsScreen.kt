package com.minimal.launcher.ui

import android.app.Activity
import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.launcher.AppInfo
import com.minimal.launcher.LauncherState
import com.minimal.launcher.SystemActions
import java.util.Date

@Composable
fun SettingsScreen(state: LauncherState) {
    val context = LocalContext.current
    var confirmLock by remember { mutableStateOf(false) }

    if (state.isLocked) {
        LockedSettings(state)
        return
    }

    if (confirmLock) {
        val until = formatLockEnd(context, System.currentTimeMillis() + state.lockMinutes * 60_000L)
        AlertDialog(
            onDismissRequest = { confirmLock = false },
            title = { Text("lock until $until?") },
            text = {
                Text(
                    "until then you can't uninstall Minimal, switch launcher, turn off the lock, " +
                        "or change these settings. there's no way to end it early.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmLock = false
                    state.startLock()
                }) { Text("lock") }
            },
            dismissButton = { TextButton(onClick = { confirmLock = false }) { Text("cancel") } },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
        contentPadding = PaddingValues(vertical = 28.dp),
    ) {
        item { Text("settings", fontSize = 34.sp, fontWeight = FontWeight.Thin) }

        item {
            Action("set as default launcher") {
                (context as? Activity)?.let(SystemActions::requestDefaultLauncher)
            }
        }

        section("focus lock")
        item {
            Hint(
                "blocks the escape routes for a while: uninstalling Minimal, switching to another " +
                    "launcher, and changing these settings. can't be ended early.",
            )
        }
        item {
            Value("lock permission", if (state.lockServiceEnabled) "on" else "off") {
                SystemActions.openAccessibilitySettings(context)
            }
        }
        if (!state.lockServiceEnabled) {
            item {
                Hint(
                    "turn on \"Minimal\" under accessibility → installed apps. if android says " +
                        "\"restricted setting\", open app info → ⋮ → allow restricted settings, then try again.",
                )
            }
            item { Action("open Minimal app info") { SystemActions.openOwnAppInfo(context) } }
        }
        item { Value("lock for", formatDuration(state.lockMinutes), state::cycleLockMinutes) }
        item {
            Action("start focus lock") {
                if (state.lockServiceEnabled) confirmLock = true else state.startLock()
            }
        }

        section("home")
        if (state.favoriteApps.isEmpty()) {
            item { Hint("long-press any app to add it to home (up to 8)") }
        }
        items(state.favoriteApps, key = { "fav-" + it.key }) { app ->
            ManagedApp(app) {
                Control("↑") { state.moveFavorite(app, -1) }
                Control("↓") { state.moveFavorite(app, +1) }
                Control("×") { state.toggleFavorite(app) }
            }
        }

        section("behaviour")
        item { Toggle("open app when search has one match", state.autoLaunch, state::toggleAutoLaunch) }
        item { Toggle("show screen time on home", state.showScreenTime, state::toggleScreenTime) }
        item { Value("mindful delay", "${state.mindfulSeconds}s", state::cycleMindfulSeconds) }

        section("mindful apps")
        item { Hint("these apps make you wait before they open. long-press an app to add it.") }
        items(state.mindfulApps, key = { "mindful-" + it.key }) { app ->
            ManagedApp(app) { Control("remove") { state.toggleMindful(app) } }
        }

        section("hidden apps")
        if (state.hiddenApps.isEmpty()) item { Hint("nothing hidden") }
        items(state.hiddenApps, key = { "hidden-" + it.key }) { app ->
            ManagedApp(app) { Control("unhide") { state.toggleHidden(app) } }
        }

        section("grayscale")
        item {
            Hint(
                "a black & white screen makes your phone far less tempting. " +
                    "turn it on in accessibility → color correction → grayscale.",
            )
        }
        item { Action("open accessibility settings") { SystemActions.openAccessibilitySettings(context) } }

        section("gestures")
        item {
            Hint(
                "swipe up — all apps\n" +
                    "swipe down — notifications\n" +
                    "long-press — settings\n" +
                    "tap the time — alarms\n" +
                    "tap the date — calendar",
            )
        }
    }
}

@Composable
private fun LockedSettings(state: LauncherState) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp, vertical = 28.dp),
    ) {
        Text("settings", fontSize = 34.sp, fontWeight = FontWeight.Thin)
        Spacer(Modifier.height(36.dp))
        Text("focus lock is on", fontSize = 19.sp)
        Text(
            "until ${formatLockEnd(context, state.lockUntil)}",
            color = Muted,
            fontSize = 19.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(16.dp))
        Hint("settings open again when it ends. your home apps still work as normal.")
    }
}

fun formatLockEnd(context: Context, millis: Long): String {
    val time = DateFormat.getTimeFormat(context).format(Date(millis))
    val sameDay = DateUtils.isToday(millis)
    return if (sameDay) time else "tomorrow $time"
}

private fun formatDuration(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
}

private fun LazyListScope.section(title: String) {
    item {
        Text(
            title,
            color = Muted,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 36.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, color = Faint, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun Action(label: String, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 19.sp,
        modifier = Modifier
            .fillMaxWidth()
            .plainClickable(onClick)
            .padding(vertical = 12.dp),
    )
}

@Composable
private fun Value(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .plainClickable(onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 19.sp, modifier = Modifier.weight(1f))
        Text(value, color = Muted, fontSize = 19.sp)
    }
}

@Composable
private fun Toggle(label: String, on: Boolean, onToggle: () -> Unit) =
    Value(label, if (on) "on" else "off", onToggle)

@Composable
private fun ManagedApp(app: AppInfo, controls: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            app.label.lowercase(),
            fontSize = 19.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        controls()
    }
}

@Composable
private fun Control(label: String, onClick: () -> Unit) {
    Text(
        label,
        color = Muted,
        fontSize = 19.sp,
        modifier = Modifier
            .plainClickable(onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}
