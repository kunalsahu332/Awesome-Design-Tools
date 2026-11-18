# Design Tool of the Day — Android widget

A home-screen widget built with Kotlin and [Jetpack Glance](https://developer.android.com/develop/ui/compose/glance).
Each day it shows a different tool from this repo's [README](../README.md) (572 tools in 36 categories).

- **Tap the card** to open the tool's website.
- **Tap the shuffle button** to get another random tool.
- **Resize it**: small shows just the name, medium adds the category and a short description, large shows more text.
- Uses Material You dynamic colors and follows light/dark mode.

## Install

**Prebuilt APK:** open the repo's **Actions** tab, go to the latest *Android widget* run, and download
`design-tool-widget-apk`. Unzip it, copy `app-debug.apk` to your phone and install it (allow "install unknown apps").

**From source:** open the `android-widget/` folder in Android Studio, then click Run. Or from the command line:

```sh
cd android-widget
./gradlew assembleDebug   # APK: app/build/outputs/apk/debug/app-debug.apk
```

Then long-press your home screen → **Widgets** → **Design Tool Widget**, or open the app and tap
**Add widget to home screen**.

## Updating the tool list

The list is bundled in `app/src/main/assets/tools.json`, generated from the root README:

```sh
node android-widget/scripts/extract-tools.js
```

The CI workflow runs this before every build, so the APK always matches the README.

## Requirements

Android 8.0 (API 26) or newer. Building it needs JDK 17 and the Android SDK (API 35).
