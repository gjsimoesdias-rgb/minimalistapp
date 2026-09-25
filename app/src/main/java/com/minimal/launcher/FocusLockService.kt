package com.minimal.launcher

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

/**
 * Enforces the focus lock: while it's on, any system screen that could remove or replace
 * this launcher (its app info, uninstall dialog, default-home picker, accessibility toggle)
 * is closed and you're sent home.
 *
 * Detection is language-independent: those screens all display this app's own label.
 */
class FocusLockService : AccessibilityService() {
    private lateinit var prefs: LauncherPrefs
    private lateinit var label: String
    private var lastKick = 0L

    override fun onServiceConnected() {
        prefs = LauncherPrefs(this)
        label = getString(R.string.app_name)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!::prefs.isInitialized || !prefs.isLocked()) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || WATCHED.none { pkg.contains(it) }) return

        val isRoleRequest = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            pkg.contains("permissioncontroller") &&
            event.className?.toString()?.contains("Role") == true
        if (isRoleRequest || showsThisApp()) kick()
    }

    private fun showsThisApp(): Boolean {
        val root = rootInActiveWindow ?: return false
        return root.findAccessibilityNodeInfosByText(label).any { node ->
            node.text?.toString()?.trim().equals(label, ignoreCase = true) ||
                node.contentDescription?.toString()?.trim().equals(label, ignoreCase = true)
        }
    }

    private fun kick() {
        val now = SystemClock.uptimeMillis()
        if (now - lastKick < KICK_COOLDOWN_MS) return
        lastKick = now
        performGlobalAction(GLOBAL_ACTION_HOME)
        Toast.makeText(this, "Focus lock is on", Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val KICK_COOLDOWN_MS = 800L

        /** Apps that host the screens used to uninstall, disable or replace a launcher. */
        private val WATCHED = listOf("settings", "packageinstaller", "permissioncontroller", "com.android.vending")

        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            val me = ComponentName(context, FocusLockService::class.java)
            return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
        }
    }
}
