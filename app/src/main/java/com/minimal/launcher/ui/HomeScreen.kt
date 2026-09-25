package com.minimal.launcher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.minimal.launcher.LauncherState
import com.minimal.launcher.Screen
import com.minimal.launcher.ScreenTime
import com.minimal.launcher.SystemActions
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(state: LauncherState) {
    val context = LocalContext.current
    val battery = rememberBatteryLevel()

    Column(
        Modifier
            .fillMaxSize()
            .homeGestures(
                onSwipeUp = { state.openDrawer() },
                onSwipeDown = { SystemActions.expandNotifications(context) },
                onLongPress = { state.screen = Screen.Settings },
            )
            .systemBarsPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Clock(
            resumeCount = state.resumeCount,
            onTimeClick = { SystemActions.openAlarms(context) },
            onDateClick = { SystemActions.openCalendar(context) },
        )
        val status = listOfNotNull(
            battery?.let { "$it%" },
            state.screenTimeMs?.let { "${ScreenTime.format(it)} today" },
            if (state.isLocked) "locked until ${formatLockEnd(context, state.lockUntil)}" else null,
        ).joinToString("   ·   ")
        if (status.isNotEmpty()) {
            Text(status, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        }

        Spacer(Modifier.weight(1f))

        if (state.favorites.isEmpty()) {
            Text(
                "swipe up for all apps\nlong-press an app to put it here\nlong-press empty space for settings",
                color = Muted,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            )
        } else {
            state.favoriteApps.forEach { app ->
                AppEntry(app = app, state = state, fontSize = 28.sp, verticalPadding = 10.dp)
            }
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "phone",
                color = Muted,
                fontSize = 16.sp,
                modifier = Modifier
                    .plainClickable { SystemActions.openDialer(context) }
                    .padding(vertical = 8.dp),
            )
            Text(
                "camera",
                color = Muted,
                fontSize = 16.sp,
                modifier = Modifier
                    .plainClickable { SystemActions.openCamera(context) }
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Clock(resumeCount: Int, onTimeClick: () -> Unit, onDateClick: () -> Unit) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // Restarted on each resume: coroutine delays pause while the device sleeps.
    LaunchedEffect(resumeCount) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000L - now % 60_000L)
        }
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val time = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(now))
    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(now)).lowercase()

    Text(
        time,
        fontSize = 72.sp,
        fontWeight = FontWeight.Thin,
        modifier = Modifier.plainClickable(onTimeClick),
    )
    Text(date, color = Muted, fontSize = 16.sp, modifier = Modifier.plainClickable(onDateClick))
}

@Composable
private fun rememberBatteryLevel(): Int? {
    val context = LocalContext.current
    var level by remember { mutableStateOf<Int?>(null) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val raw = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                if (raw >= 0 && scale > 0) level = raw * 100 / scale
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return level
}

private fun Modifier.homeGestures(
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onLongPress: () -> Unit,
): Modifier = this
    .pointerInput(Unit) {
        var total = 0f
        detectVerticalDragGestures(
            onDragStart = { total = 0f },
            onDragEnd = {
                val threshold = 60.dp.toPx()
                when {
                    total < -threshold -> onSwipeUp()
                    total > threshold -> onSwipeDown()
                }
            },
            onVerticalDrag = { change, amount ->
                change.consume()
                total += amount
            },
        )
    }
    .pointerInput(Unit) {
        detectTapGestures(onLongPress = { onLongPress() })
    }
