package com.minimal.launcher

import android.content.Context

class LauncherPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    /** Ordered list of app keys shown on the home screen. */
    var favorites: List<String>
        get() = prefs.getString(KEY_FAVORITES, "").orEmpty().split('\n').filter { it.isNotBlank() }
        set(value) = prefs.edit().putString(KEY_FAVORITES, value.joinToString("\n")).apply()

    var hidden: Set<String>
        get() = prefs.getStringSet(KEY_HIDDEN, emptySet()).orEmpty().toSet()
        set(value) = prefs.edit().putStringSet(KEY_HIDDEN, value).apply()

    /** Apps that make you wait a few seconds before they open. */
    var mindful: Set<String>
        get() = prefs.getStringSet(KEY_MINDFUL, emptySet()).orEmpty().toSet()
        set(value) = prefs.edit().putStringSet(KEY_MINDFUL, value).apply()

    var mindfulSeconds: Int
        get() = prefs.getInt(KEY_MINDFUL_SECONDS, 5)
        set(value) = prefs.edit().putInt(KEY_MINDFUL_SECONDS, value).apply()

    var autoLaunch: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LAUNCH, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LAUNCH, value).apply()

    var showScreenTime: Boolean
        get() = prefs.getBoolean(KEY_SCREEN_TIME, false)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN_TIME, value).apply()

    /** Epoch millis until which the focus lock is active; 0 when off. */
    var lockUntil: Long
        get() = prefs.getLong(KEY_LOCK_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_LOCK_UNTIL, value).apply()

    var lockMinutes: Int
        get() = prefs.getInt(KEY_LOCK_MINUTES, 60)
        set(value) = prefs.edit().putInt(KEY_LOCK_MINUTES, value).apply()

    fun isLocked(): Boolean = lockUntil > System.currentTimeMillis()

    private companion object {
        const val KEY_LOCK_UNTIL = "lock_until"
        const val KEY_LOCK_MINUTES = "lock_minutes"
        const val KEY_FAVORITES = "favorites"
        const val KEY_HIDDEN = "hidden"
        const val KEY_MINDFUL = "mindful"
        const val KEY_MINDFUL_SECONDS = "mindful_seconds"
        const val KEY_AUTO_LAUNCH = "auto_launch"
        const val KEY_SCREEN_TIME = "screen_time"
    }
}
