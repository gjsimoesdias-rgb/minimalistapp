# Minimal

A text-only, black & white Android launcher in the spirit of *minimalist phone*. It makes your phone boring on purpose.

## Features

- **Calm home screen**: a big clock, the date, battery (and optionally today's screen time), then up to 8 apps as plain text. No icons, badges or wallpaper.
- **Swipe up** for an alphabetical list of all apps with instant search. It opens the keyboard, ignores accents, and can optionally open the app itself when only one matches.
- **Long-press any app** to add it to or remove it from home, give it a *mindful delay*, hide it, open its app info, or uninstall it.
- **Mindful delay**: chosen apps show a "breathe." screen and a countdown (3–30 s) before you can open them.
- **Screen time today**: an optional total on the home screen, using Android usage access.
- **Hidden apps** stay out of the drawer and search.
- **Gestures**: swipe down for notifications, long-press empty space for settings, tap the time for alarms, tap the date for calendar, and use the bottom corners for phone and camera.
- **Grayscale**: a shortcut plus instructions for turning the screen black & white.

Kotlin + Jetpack Compose, minSdk 26 (Android 8.0), no internet permission, no tracking.

## Build the APK

### Option A: GitHub (no local setup)
1. Push this folder to a GitHub repository.
2. The **Build APK** workflow runs automatically. It also has a manual *Run workflow* button in the Actions tab.
3. Download the `minimal-launcher` artifact. It contains `app-release.apk` and `app-debug.apk`.

### Option B: Android Studio
1. Install [Android Studio](https://developer.android.com/studio).
2. Use **File → Open** to open this folder and let Gradle sync.
3. Plug in your phone with USB debugging on, then press **Run**, or use **Build → Build APK(s)**.

## Install on your phone
1. Copy the APK to the phone and open it. Allow "install unknown apps" when asked.
2. Open **Minimal**, long-press the screen, and choose **set as default launcher**.
3. Swipe up and long-press your essential apps to add them to home.

To go back to your old launcher, use Settings → Apps → Default apps → Home app.

### Grayscale without the menus (optional, via adb)
```
adb shell settings put secure accessibility_display_daltonizer_enabled 1
adb shell settings put secure accessibility_display_daltonizer 0
```
