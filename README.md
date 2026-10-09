# Notification Killer

Android-native notification filtering app built with Kotlin, Jetpack Compose, Room, DataStore and `NotificationListenerService`.

## Build

Requirements: JDK 17 and Android SDK Platform 34 (or a compatible newer SDK).

```powershell
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

## First run

1. Open **Notification Access** from the dashboard. Android will show its system access screen; the user must enable the listener there.
2. Choose app filters and protected apps. Protected apps take priority over app and keyword rules.
3. Add keyword rules. Rules can dismiss a clearable notification or record a metadata-only review entry.
4. Turn on **Enable Auto-Clean** when ready. It is off by default.

## What Android allows

`NotificationListenerService` receives a notification after Android publishes it, so filtering cannot guarantee that it was never briefly visible. The app requests dismissal with `cancelNotification(key)` for matching clearable notifications. Android and device manufacturers control which notification types can be dismissed; the API does not confirm that the notification was removed, so history records a request rather than claiming confirmed success. Ongoing and non-clearable notifications are retained. No Accessibility Service, root, or hidden API is used.

Quiet Hours is intentionally not implemented: changing the device-wide Do Not Disturb policy requires a separate policy grant and affects the whole device. No permission for it is requested.

## Privacy

Notification contents are used in memory to evaluate rules and are not written to the database, logs, or a network service. Local history contains only app/package, timestamp, matched rule, action and processing result. You can clear history or choose a retention period in Settings. This history is not a recoverable copy of Android notifications.

The app queries launchable applications only and does not request broad package visibility. Network access is not needed for core features.

## Device verification

Unit tests cover rule priority, filtering, matching, malformed/risky regular expressions, empty notification fields, deduplication and conservative behavior. Verify listener enable/revoke/reconnect, posting and dismissing test notifications, reboot behavior, and vendor-specific settings on physical devices. Notification-listener behavior varies by Android version and manufacturer.
