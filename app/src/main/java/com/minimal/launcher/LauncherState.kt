package com.minimal.launcher

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

enum class Screen { Home, Drawer, Settings }

class LauncherState(private val activity: ComponentActivity) {
    private val prefs = LauncherPrefs(activity)

    var screen by mutableStateOf(Screen.Home)
    var query by mutableStateOf("")
    var pendingLaunch by mutableStateOf<AppInfo?>(null)

    /** Bumped on every resume so time-based UI can resync after the device sleeps. */
    var resumeCount by mutableIntStateOf(0)
        private set
    var apps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var favorites by mutableStateOf(prefs.favorites)
        private set
    var hidden by mutableStateOf(prefs.hidden)
        private set
    var mindful by mutableStateOf(prefs.mindful)
        private set
    var mindfulSeconds by mutableIntStateOf(prefs.mindfulSeconds)
        private set
    var autoLaunch by mutableStateOf(prefs.autoLaunch)
        private set
    var showScreenTime by mutableStateOf(prefs.showScreenTime)
        private set
    var screenTimeMs by mutableStateOf<Long?>(null)
        private set
    var lockUntil by mutableStateOf(prefs.lockUntil)
        private set
    var lockMinutes by mutableIntStateOf(prefs.lockMinutes)
        private set
    var lockServiceEnabled by mutableStateOf(FocusLockService.isEnabled(activity))
        private set

    /** Re-evaluated on every resume (via [refresh]), so an expired lock clears itself. */
    val isLocked: Boolean get() = lockUntil > System.currentTimeMillis()

    val favoriteApps: List<AppInfo>
        get() = favorites.mapNotNull { key -> apps.firstOrNull { it.key == key } }

    val hiddenApps: List<AppInfo> get() = apps.filter { it.key in hidden }
    val mindfulApps: List<AppInfo> get() = apps.filter { it.key in mindful }

    val searchResults: List<AppInfo>
        get() {
            val visible = apps.filter { it.key !in hidden }
            val q = query.trim().normalized()
            if (q.isEmpty()) return visible
            return visible
                .filter { it.label.normalized().contains(q) }
                .sortedBy { if (it.label.normalized().startsWith(q)) 0 else 1 }
        }

    fun refresh() {
        resumeCount++
        lockServiceEnabled = FocusLockService.isEnabled(activity)
        if (lockUntil != 0L && !isLocked) {
            lockUntil = 0L
            prefs.lockUntil = 0L
        }
        activity.lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) { AppRepository.loadApps(activity) }
            apps = loaded
            pruneMissing(loaded)
            screenTimeMs = if (showScreenTime && ScreenTime.hasPermission(activity)) {
                withContext(Dispatchers.IO) { ScreenTime.today(activity) }
            } else {
                null
            }
        }
    }

    fun goHome() {
        screen = Screen.Home
        query = ""
    }

    fun openDrawer() {
        query = ""
        screen = Screen.Drawer
    }

    fun launch(app: AppInfo) {
        if (app.key in mindful) pendingLaunch = app else openNow(app)
    }

    fun openNow(app: AppInfo) {
        pendingLaunch = null
        if (!AppRepository.launch(activity, app)) toast("Couldn't open ${app.label}")
        goHome()
    }

    fun toggleFavorite(app: AppInfo) {
        favorites = when {
            app.key in favorites -> favorites - app.key
            favorites.size >= MAX_FAVORITES -> {
                toast("Home holds $MAX_FAVORITES apps. Keep it simple.")
                return
            }
            else -> favorites + app.key
        }
        prefs.favorites = favorites
    }

    fun moveFavorite(app: AppInfo, delta: Int) {
        val list = favorites.toMutableList()
        val from = list.indexOf(app.key)
        val to = from + delta
        if (from < 0 || to !in list.indices) return
        list.add(to, list.removeAt(from))
        favorites = list
        prefs.favorites = list
    }

    fun toggleHidden(app: AppInfo) {
        hidden = if (app.key in hidden) hidden - app.key else hidden + app.key
        prefs.hidden = hidden
    }

    fun toggleMindful(app: AppInfo) {
        mindful = if (app.key in mindful) mindful - app.key else mindful + app.key
        prefs.mindful = mindful
    }

    fun cycleMindfulSeconds() {
        val options = listOf(3, 5, 10, 15, 30)
        mindfulSeconds = options[(options.indexOf(mindfulSeconds) + 1) % options.size]
        prefs.mindfulSeconds = mindfulSeconds
    }

    fun toggleAutoLaunch() {
        autoLaunch = !autoLaunch
        prefs.autoLaunch = autoLaunch
    }

    fun toggleScreenTime() {
        showScreenTime = !showScreenTime
        prefs.showScreenTime = showScreenTime
        if (showScreenTime && !ScreenTime.hasPermission(activity)) {
            toast("Allow usage access for Minimal")
            ScreenTime.requestPermission(activity)
        }
        refresh()
    }

    fun cycleLockMinutes() {
        lockMinutes = LOCK_OPTIONS[(LOCK_OPTIONS.indexOf(lockMinutes) + 1) % LOCK_OPTIONS.size]
        prefs.lockMinutes = lockMinutes
    }

    /** Starts the focus lock. Returns false (and opens accessibility settings) if the service is off. */
    fun startLock(): Boolean {
        if (!FocusLockService.isEnabled(activity)) {
            toast("Turn on Minimal in accessibility first")
            SystemActions.openAccessibilitySettings(activity)
            return false
        }
        lockUntil = System.currentTimeMillis() + lockMinutes * 60_000L
        prefs.lockUntil = lockUntil
        goHome()
        return true
    }

    /** Forget uninstalled apps so they don't take up home slots. */
    private fun pruneMissing(loaded: List<AppInfo>) {
        if (loaded.isEmpty()) return
        val keys = loaded.mapTo(HashSet()) { it.key }
        if (favorites.any { it !in keys }) {
            favorites = favorites.filter { it in keys }
            prefs.favorites = favorites
        }
        if (hidden.any { it !in keys }) {
            hidden = hidden.filterTo(HashSet()) { it in keys }
            prefs.hidden = hidden
        }
        if (mindful.any { it !in keys }) {
            mindful = mindful.filterTo(HashSet()) { it in keys }
            prefs.mindful = mindful
        }
    }

    private fun toast(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    private fun String.normalized(): String =
        Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(DIACRITICS, "")

    private companion object {
        const val MAX_FAVORITES = 8
        val LOCK_OPTIONS = listOf(30, 60, 120, 240, 480, 1440)
        val DIACRITICS = Regex("\\p{Mn}+")
    }
}
