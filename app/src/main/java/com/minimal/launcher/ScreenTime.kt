package com.minimal.launcher

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

object ScreenTime {
    // UsageEvents.Event.ACTIVITY_RESUMED / ACTIVITY_PAUSED (same values as the pre-Q constants).
    private const val RESUMED = 1
    private const val PAUSED = 2

    fun hasPermission(context: Context): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestPermission(context: Context) =
        AppRepository.startSafely(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

    /** Total foreground time today across all apps except this launcher, in milliseconds. */
    fun today(context: Context): Long {
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return 0L
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val events = usm.queryEvents(start, System.currentTimeMillis())
        val event = UsageEvents.Event()
        val resumedAt = HashMap<String, Long>()
        var total = 0L
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == context.packageName) continue
            val key = "${event.packageName}/${event.className}"
            when (event.eventType) {
                RESUMED -> resumedAt[key] = event.timeStamp
                PAUSED -> resumedAt.remove(key)?.let { total += event.timeStamp - it }
            }
        }
        return total
    }

    fun format(ms: Long): String {
        val minutes = ms / 60_000
        val hours = minutes / 60
        return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
    }
}
