# Batty

Battery notifier — an Android app written in Kotlin that shows notifications and alerts when battery level reaches configurable thresholds or when charging state changes.

## Description

Lightweight Kotlin Android app to notify users about battery level and charging state. Configure low/high thresholds (for example, notify at 20% or 90%), notification behavior and check frequency. Designed to run in the background with minimal battery impact.

## Features

- Configurable notifications when battery reaches user-defined levels.
- Detects charging state changes (charging / discharging).
- Optional: restore schedules at device boot.
- Notification channel(s) for controlling priority, sound and vibration.
- Simple UI to configure thresholds and options.

## Screenshots

Add screenshots to docs/ or assets/, e.g.:
- docs/screenshot-1.png
- docs/screenshot-2.png

## Requirements

- Android Studio (recommended) or an environment with Android SDK and Gradle.
- JDK 11+ (or as required by the project Gradle settings).
- Kotlin (included in the project).
- Recommended minSdkVersion >= 21 (adjust to your needs).

## Development / Run (local)

1. Clone:
   git clone https://github.com/andres2002002/Batty.git
2. Open in Android Studio (File → Open → Batty) and let Gradle sync.
3. Connect a device or start an emulator.
4. Run from Android Studio or:
   ./gradlew assembleDebug
   ./gradlew installDebug

## Permissions & Notes

- Android 13+ requires runtime POST_NOTIFICATIONS to show notifications:
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
- If you re-schedule work at boot:
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
- For continuous monitoring via a foreground service:
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
- Battery level is typically read via ACTION_BATTERY_CHANGED or BatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).

## Example AndroidManifest snippet

<manifest ...>
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

  <application ...>
    <!-- Declare services, receivers and activities here -->
  </application>
</manifest>

## How it works (technical)

- The app listens to ACTION_BATTERY_CHANGED or queries BatteryManager for capacity.
- When the level or state matches user settings, it builds a Notification via NotificationManager and NotificationChannel (Android O+).
- For continuous operation consider WorkManager, a foreground Service, or AlarmManager depending on desired tradeoffs between reliability and battery use.

## Suggested default settings

- Low battery threshold: 20%
- High battery threshold: 90%
- Repeat notifications: on/off
- Check interval (if polling): 15 / 30 / 60 minutes
- Sound & vibration per notification channel

## Testing

- Use Android Emulator to simulate battery levels.
- Test charging states (USB, AC) and permission flows (runtime permissions on Android 6+ and POST_NOTIFICATIONS on Android 13+).

## Contributing

1. Fork the repo and create a branch: git checkout -b feature/your-feature
2. Commit with clear messages and open a pull request describing changes.
3. Add tests where appropriate and update README/docs for new features.

## Repository layout (example)

- app/ — Android module
- docs/ — screenshots and guides
- LICENSE — project license
- README.md — this file

## License

This project is licensed under the MIT License — see the LICENSE file for details.

## Contact

- Maintainer: andres2002002
