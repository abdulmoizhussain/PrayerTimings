# Prayer Timings: Agent Guide

This is a small, legacy Android app written in Java with XML Views. It displays a **bundled Karachi-only annual timetable**, sends prayer-time notifications, and can temporarily switch the device to vibrate mode at user-selected times.

Read [APP_ANALYSIS.md](docs/APP_ANALYSIS.md) before modifying behavior. It records the app's data flow, alarm lifecycle, preference stores, build state, and known risks.

## Fast orientation

- Entry point: `MainActivity`; settings: `PreferenceClass`.
- Timetable source: `app/src/main/assets/Karachi.json` (366 static entries); runtime cache: SQLite `prayer_timings.db`.
- Domain/data access: `services/PrayerTimeService.java`, `DatabaseManager.java`, `DatabaseHelper.java`.
- Scheduling and receivers: `Global.java`, `MyAlarmManager.java`, `NotificationPublisher.java`, `ReScheduleNotificationsBroadcastReceiver.java`, and `TurnToSilentModeBroadcastReceiver.java`.
- Build: Gradle 6.5, Android Gradle Plugin 4.1.3, compile/target SDK 30, min SDK 19.

## Working conventions

- The seven timetable slots are fixed and ordered: Fajr, Sunrise, Zawal, Zuhur, Asr, Maghrib, Isha. Preserve that ordering across JSON, SQL, UI, and notification-message code.
- Times are stored as `HH:mm` strings and interpreted in the device's current local time zone. They are *not* calculated from GPS, city selection, or a web API.
- There are two distinct preference files: default preferences hold feature toggles; `silence_timings` holds the duration as an integer and five custom trigger times as milliseconds since midnight.
- Treat Android alarm and notification behavior as a compatibility-sensitive area. Any target-SDK upgrade must be accompanied by an end-to-end alarm/notification audit and physical-device tests.
- Do not assume a timetable update will reach existing installs: the database schema version is `1` and initial asset data is imported only when the database is empty.

## Useful validation scope

At minimum, verify the main screen on a date rollover, toggling notifications, reboot/time-zone handling, and the silence trigger/restore cycle. See the test checklist in [APP_ANALYSIS.md](docs/APP_ANALYSIS.md).
