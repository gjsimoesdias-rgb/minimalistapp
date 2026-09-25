package com.minimal.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimal.launcher.AppInfo
import kotlinx.coroutines.delay

/** A pause before a distracting app opens. The user still has to choose to continue. */
@Composable
fun MindfulOverlay(app: AppInfo, seconds: Int, onOpen: () -> Unit, onCancel: () -> Unit) {
    var remaining by remember(app) { mutableIntStateOf(seconds) }
    LaunchedEffect(app) {
        while (remaining > 0) {
            delay(1_000)
            remaining--
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .plainClickable {} // swallow taps meant for the screen underneath
            .systemBarsPadding()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("breathe.", fontSize = 40.sp, fontWeight = FontWeight.Thin)
        Spacer(Modifier.height(16.dp))
        Text(
            "do you really need ${app.label.lowercase()} right now?",
            color = Muted,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(56.dp))
        if (remaining > 0) {
            Text("$remaining", color = Faint, fontSize = 28.sp, fontWeight = FontWeight.Light)
        } else {
            Text(
                "open ${app.label.lowercase()}",
                fontSize = 20.sp,
                modifier = Modifier
                    .plainClickable(onOpen)
                    .padding(12.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "never mind",
            color = Muted,
            fontSize = 17.sp,
            modifier = Modifier
                .plainClickable(onCancel)
                .padding(12.dp),
        )
    }
}
