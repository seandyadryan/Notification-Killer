# Notification Killer

Android-native notification filtering app built with Kotlin, Jetpack Compose, Room, DataStore and `NotificationListenerService`.

## Build

Requirements: JDK 17 and Android SDK Platform 36 (or a compatible newer SDK).

```powershell
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`. Do not distribute a debug APK as the public release.

## Languages

The interface currently includes 14 locales: English, Indonesian, Spanish, French, German, Italian, Brazilian Portuguese, Japanese, Korean, Simplified Chinese, Traditional Chinese, Hindi, Arabic and Russian. Android 13 and newer expose these locales in the system's per-app language settings; older versions follow the device language. Other locales fall back to English. Human-reviewed translations for every language are not available yet, so this build does not claim to support every language worldwide.

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

## Play Protect and release distribution

The screenshot is a Google Play Protect block for an app installed outside Google Play that requests access to sensitive notification data. An app icon, a release signature, or an APK code change cannot override this decision. Do not disable Play Protect or try to hide Notification Access: it is required for the app's core feature.

For public distribution, create a release upload key that you control, provide its path and credentials through the `NK_RELEASE_STORE_FILE`, `NK_RELEASE_STORE_PASSWORD`, `NK_RELEASE_KEY_ALIAS` and `NK_RELEASE_KEY_PASSWORD` environment variables, then build an Android App Bundle with `./gradlew bundleRelease`. Keep the key and passwords private and out of Git. Upload the signed bundle to your Play Console testing track, complete the privacy/data-safety declarations, and install the reviewed build from Google Play. Google may still require an appeal if Play Protect incorrectly blocks it; use the official [Play Protect app-verification appeal](https://support.google.com/googleplay/android-developer/answer/2992033).

## Device verification

Unit tests cover rule priority, filtering, matching, malformed/risky regular expressions, empty notification fields, deduplication and conservative behavior. Verify listener enable/revoke/reconnect, posting and dismissing test notifications, reboot behavior, and vendor-specific settings on physical devices. Notification-listener behavior varies by Android version and manufacturer.
