package com.minimal.launcher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

object SystemActions {

    fun openDialer(context: Context) = AppRepository.startSafely(context, Intent(Intent.ACTION_DIAL))

    fun openCamera(context: Context) =
        AppRepository.startSafely(context, Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))

    fun openAlarms(context: Context) =
        AppRepository.startSafely(context, Intent(AlarmClock.ACTION_SHOW_ALARMS))

    fun openCalendar(context: Context) = AppRepository.startSafely(
        context,
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR),
    )

    fun openAccessibilitySettings(context: Context) =
        AppRepository.startSafely(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

    fun openOwnAppInfo(context: Context) = AppRepository.startSafely(
        context,
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
    )

    @SuppressLint("WrongConstant")
    fun expandNotifications(context: Context) {
        try {
            val statusBar = context.getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager")
                .getMethod("expandNotificationsPanel")
                .invoke(statusBar)
        } catch (_: Exception) {
            // Not supported on this device; ignore.
        }
    }

    fun requestDefaultLauncher(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                @Suppress("DEPRECATION")
                activity.startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), 1)
                return
            }
        }
        AppRepository.startSafely(activity, Intent(Settings.ACTION_HOME_SETTINGS))
    }
}
