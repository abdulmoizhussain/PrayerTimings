# Prayer Timings — Technical Analysis

## Purpose and current scope

Prayer Timings is an Android application that:

1. Displays the seven daily prayer-related time slots for Karachi.
2. Shows Gregorian and Islamic-calendar dates plus a live countdown to the next time change.
3. Schedules a notification for each remaining slot that day.
4. Optionally changes the ringer to **vibrate** for a user-configured duration at up to five manually selected prayer times.

It is not a dynamic prayer-time calculator. Its sole data source is `app/src/main/assets/Karachi.json`: 366 static Karachi records (one per calendar day, including 29 February). There is no location permission, network client, city picker, or yearly-data refresh mechanism.

## Build and platform baseline

| Item | Value |
| --- | --- |
| Language/UI | Java; Android XML Views; legacy `PreferenceActivity` |
| Application ID | `com.example.abdul.prayertimings` |
| Version | `1.0.2` (`versionCode 3`) |
| Minimum / target / compile SDK | 19 / 30 / 30 |
| Gradle / Android Gradle Plugin | 6.5 / 4.1.3 |
| Main libraries | AppCompat 1.3.0, Material 1.3.0, ConstraintLayout 2.0.4, Joda-Time 2.9.9 |
| Tests | One instrumentation-test shell; no substantive unit or integration tests |

Top-level Gradle files still use `jcenter()`, which is retired. The project should be treated as legacy until its Gradle, Android Gradle Plugin, SDK target, and dependencies are modernized together.

### Build verification performed

On 2026-08-29, the checked-in wrapper was downloaded successfully and identified itself as Gradle 6.5. A debug build plus unit-test attempt under the available JDK 17 failed before project evaluation with `Unsupported class file major version 61`. This is a Gradle 6.5/JDK 17 incompatibility, not an application-source test failure. Use a compatible older JDK for the legacy toolchain (then verify which Android SDK packages are installed), or perform the planned toolchain upgrade before relying on JDK 17+.

## Architecture and request flow

```text
Karachi.json (366 static records)
        │ first lookup when SQLite is empty
        ▼
DatabaseManager ──► prayer_timings.db / PrayerTime table
        │ per-date seven HH:mm values
        ▼
PrayerTimeService
   ┌────┼───────────────────────────────────────┐
   ▼    ▼                                       ▼
MainActivity                              Global scheduler
screen + countdown                         AlarmManager exact alarms
                                               │
                              ┌────────────────┴────────────────┐
                              ▼                                 ▼
                    NotificationPublisher              TurnToSilentModeReceiver
                    posts notification                 vibrate → restore prior mode
```

`AppStartup` is the custom `Application` class and creates the notification channel on Android 8.0+.

## Source map

| Area | Files | Notes |
| --- | --- | --- |
| UI and date display | `MainActivity.java`, `res/layout/activity_main.xml` | Renders seven times, Gregorian/Islamic dates, and an every-second countdown. |
| Prayer data | `services/PrayerTimeService.java`, `DatabaseManager.java`, `DatabaseHelper.java`, `assets/Karachi.json` | Imports the asset into SQLite on first use, then fetches Karachi by month/day. |
| Notification scheduling | `Global.java`, `MyAlarmManager.java`, `NotificationPublisher.java`, `NotificationManagement.java` | Uses exact `RTC_WAKEUP` broadcast alarms; daily schedule is refreshed at 01:00 and on relevant system broadcasts. |
| Ringer handling | `PreferenceClass.java`, `TurnToSilentModeBroadcastReceiver.java` | Stores five user-selected trigger times, changes to vibrate, then restores the recorded prior ringer mode. |
| Resilience hooks | `ReScheduleNotificationsBroadcastReceiver.java`, `AndroidManifest.xml` | Reschedules after boot, date, time, or timezone changes. |
| Supporting utilities | `DateTime.java`, `DateFormats.java`, `CalendarHelper.java`, `Console.java` | Date wrappers/formats and logging. `PrayerTimeDto` is currently unused. |

## Daily timetable contract

The app relies on this exact seven-item order throughout the UI, database query, countdown, and notification lookup:

| Index | Field | Meaning |
| ---: | --- | --- |
| 0 | `Fajr` | Fajr begins |
| 1 | `Sunrise` | Fajr ends / sunrise |
| 2 | `Zawal` | Zawal begins |
| 3 | `Zuhur` | Zuhur begins |
| 4 | `Asr` | Asr begins |
| 5 | `Maghrib` | Maghrib begins |
| 6 | `Isha` | Isha begins |

Times in the asset and database use 24-hour `HH:mm`. `MainActivity` displays them in the device's 12/24-hour preference. All times are interpreted in the device local time zone, despite some formatting helpers using GMT to render stored “milliseconds since midnight” values.

### SQLite lifecycle

- Database name/table: `prayer_timings.db` / `PrayerTime`.
- Schema version: `1`.
- First `PrayerTimeService` lookup opens the database, checks whether row ID 1 exists, reads the whole JSON asset, and inserts it in a transaction.
- Future lookups query `City='Karachi'` and the current numeric month/day.
- Asset edits do **not** update an existing installed database. A migration/version bump and deliberate data-replacement strategy are required for that.

## Runtime behavior

### Main screen

On resume, `MainActivity` loads today and tomorrow's time arrays, starts a one-second `Handler` countdown, renders today's times, and registers a `TIME_TICK` receiver to update the dates/times every minute. It marks the current interval green. The final interval uses tomorrow's Fajr for the countdown.

The Islamic date is converted with Joda-Time's UTC Islamic chronology from the displayed civil date. This is a calculated Islamic calendar, not an observed/local moon-sighting calendar.

### Notifications

- Default preference: enabled (`notification_switch=true`).
- The first launch cancels existing alarms and schedules only future time slots today.
- Each prayer alarm targets `NotificationPublisher` with request code `0`–`6` and posts a high-priority notification.
- A separate request code `-50` should invoke the rescheduler at roughly 01:00 the next day.
- `ReScheduleNotificationsBroadcastReceiver` also handles boot, clock/date, and timezone events, canceling and recreating today's notification alarms.

### Silence feature

- Default preference: disabled (`silence_switch=false`).
- The user enters five trigger times (Fajr, Zuhur, Asr, Maghrib, Isha); these do not automatically follow the timetable.
- When a future trigger fires, the app records the current ringer mode, sets `RINGER_MODE_VIBRATE`, then schedules request code `99` to restore the prior mode after the selected duration (default 10 minutes).
- The setting label says “silent”, but current implementation always uses vibrate—not silent mode—and it does not use the unused `silenceMode` arrays.

## Preferences and persistent state

| Store | Key(s) | Type / default | Use |
| --- | --- | --- | --- |
| Default shared preferences | `notification_switch` | boolean / `true` | Enables notifications. |
| Default shared preferences | `silence_switch` | boolean / `false` | Enables ringer-change behavior. |
| Default shared preferences | `firstTime` | boolean / absent | Prevents first-launch notification scheduling from repeating. |
| `silence_timings` | `silence_duration` | int / `10` | Minutes to retain vibrate mode. |
| `silence_timings` | `"11"`–`"15"` | long / `0` | Milliseconds since midnight for Fajr through Isha user triggers. |
| Default shared preferences | `error_notification_count` | int / `2000` | Incrementing IDs for error notifications. |

`ListPreference` owns a default-preference string value, while `PreferenceClass` copies the selected duration as an integer into `silence_timings`. Both are relevant when changing the settings implementation.

## Manifest components and permissions

Declared receivers are internal except the rescheduler:

- `NotificationPublisher` — not exported.
- `TurnToSilentModeBroadcastReceiver` — not exported.
- `ReScheduleNotificationsBroadcastReceiver` — exported for boot/time/date/timezone actions.

The manifest requests `RECEIVE_BOOT_COMPLETED`. It declares `VIBRATE` only through API 18, so the permission is absent on newer devices even though notification defaults request vibration.

## Key risks and recommended remediation order

| Priority | Finding | Evidence and impact | Recommended direction |
| --- | --- | --- | --- |
| P0 | Modern Android compatibility is incomplete | All `PendingIntent` calls omit `FLAG_IMMUTABLE`/`FLAG_MUTABLE`; apps targeting Android 12+ will fail when creating them. Android 13+ notification permission and modern exact-alarm policies are not handled. | Modernize the build/target SDK, add immutable flags, request notification permission where required, and handle exact-alarm eligibility/fallbacks. Test on current physical Android versions. |
| P0 | Ringer changes need user-granted policy access on modern Android | `AudioManager.setRingerMode()` can be blocked by Do Not Disturb policy access, and no policy-access flow is present. | Check notification-policy access before enabling the feature, guide the user to settings, and expose failure clearly. |
| P1 | Daily reschedule cancellation likely misses its target | The schedule creates the `-50` PendingIntent with action `ACTION_DATE_CHANGED`; cancellation creates it without that action. PendingIntent identity includes action, so the daily alarm may not be canceled. | Centralize creation of this PendingIntent and use identical action/component/request-code/flags for schedule and cancellation. |
| P1 | Timetable is frozen and Karachi-only | Static annual clock times can become stale and are unrelated to device city/year. | Choose an explicit data strategy: maintained local annual data with migration, or a validated calculation/data provider plus city/time-zone support. |
| P1 | Database updates are destructive and unavailable in practice | Schema is version 1; `onUpgrade()` drops the table; asset import only occurs when empty. | Introduce versioned, non-destructive migrations and an idempotent data refresh/replacement path. |
| P1 | Silence scheduling is incomplete | Existing silence alarms are not centrally canceled/rebuilt when times or toggle state change. The restore alarm uses one shared request code, so overlapping triggers can overwrite each other. | Model each trigger and restore alarm with stable distinct identities; cancel/rebuild on every related preference change. |
| P2 | Date/time implementation has correctness limits | `DateTime.addDays()` adds a fixed 86,400,000 ms and is wrong across daylight-saving transitions. The timetable is local-wall-clock data but some formatting adopts GMT. | Use `java.time`/`Calendar.add` with an explicit zone after modernizing min SDK (or a compatible backport). |
| P2 | UI and code are aging | `PreferenceActivity`, raw SQL concatenation, a per-second UI loop, unused menu/layout artifacts, and unused `PrayerTimeDto` add maintenance cost. | Migrate incrementally to AndroidX preference APIs, parameterized queries, lifecycle-aware updates, and remove/refactor dead code after coverage is in place. |

## Validation checklist for a future change

1. Build debug and release variants with the selected supported JDK/Gradle/SDK combination.
2. Fresh-install test: confirm the asset imports once, all seven times render, and notifications are scheduled only for future times.
3. Test dates around 28/29 February, 31-day month boundaries, midnight, and 01:00 rescheduling.
4. Toggle notifications repeatedly and inspect that all expected alarms are created/canceled exactly once.
5. Reboot the device; change date, time, and timezone; confirm notification and silence schedules are recreated.
6. Test silence mode with permitted and denied Do Not Disturb policy access, plus overlapping trigger times and duration changes.
7. Test 12-hour and 24-hour display modes.
8. Test at least one current Android version after target-SDK upgrades: notifications permission, exact alarms, boot behavior, and PendingIntent creation.

## Suggested modernization sequence

1. Establish tests around `PrayerTimeService`, schedule construction, date boundaries, and preference migrations.
2. Upgrade Gradle/AGP/dependencies/repositories and target a current supported SDK.
3. Resolve platform permissions and all `PendingIntent`/exact-alarm behavior.
4. Make timezone, data-source, city, and timetable-refresh decisions explicit.
5. Replace legacy settings/UI APIs and simplify date/time handling.

## Scope notes

This analysis reflects source inspection as of 2026-08-29. It deliberately does not claim that legacy alarm behavior succeeds on every OEM/device; those claims require device-level verification.
