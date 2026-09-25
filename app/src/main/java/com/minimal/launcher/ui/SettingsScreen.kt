package com.minimal.launcher.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun SettingsScreen(state: LauncherState) {
    val context = LocalContext.current

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
