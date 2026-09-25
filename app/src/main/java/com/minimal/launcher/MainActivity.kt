package com.minimal.launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.minimal.launcher.ui.LauncherApp

class MainActivity : ComponentActivity() {
    private lateinit var state: LauncherState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        state = LauncherState(this)
        setContent { LauncherApp(state) }
    }

    override fun onResume() {
        super.onResume()
        state.refresh()
    }

    // Pressing the home button while we're already in front returns to the home screen.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        state.pendingLaunch = null
        state.goHome()
    }
}
