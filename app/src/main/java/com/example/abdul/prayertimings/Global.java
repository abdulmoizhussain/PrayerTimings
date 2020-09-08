package com.example.abdul.prayertimings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.example.abdul.prayertimings.services.PrayerTimeService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Abdul on 8/6/2017.
 * This is an extra line to remove warning.
 */

class Global {
    public static final Map<Integer, String> IslamicMonthFullName;
    public static final Map<Integer, String> NotificationMessage;
    public static final TimeZone timeZoneGmt = TimeZone.getTimeZone("GMT");
    public static final Collection<String> IntentActions = Collections.unmodifiableList(Arrays.asList(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
    ));

    static {
        {
            Map<Integer, String> map = new HashMap<>();
            map.put(1, "Muharram");
            map.put(2, "Safar");
            map.put(3, "Rabiul-Awwal");
            map.put(4, "Rabi-uthani");
            map.put(5, "Jumadi-ul-Awwal");
            map.put(6, "Jumadi-uthani");
            map.put(7, "Rajab");
            map.put(8, "Sha’ban");
            map.put(9, "Ramadan");
            map.put(10, "Shawwal");
            map.put(11, "Zhul-Q’ada");
            map.put(12, "Zhul-Hijja");
            IslamicMonthFullName = Collections.unmodifiableMap(map);
        }
        {
            Map<Integer, String> map = new HashMap<>();
            map.put(0, "Fajr time has started.");
            map.put(1, "Fajr time has ended.");
            map.put(2, "Zawal time.");
            map.put(3, "Zuhur time has started.");
            map.put(4, "Asr time has started.");
            map.put(5, "Maghrib time has started.");
            map.put(6, "Isha time has started.");
            NotificationMessage = Collections.unmodifiableMap(map);
        }
    }


    public static boolean getSilenceFlag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("silence_switch", false);
    }

    public static boolean getNotificationFlag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notification_switch", false);
    }


    /**
     * Removes extra seconds which cause delay.
     *
     * @return current time in milliseconds.
     */
    public static long getCurrentTimeMillis() {
        long remainder, currentTimeMillis;
        remainder = currentTimeMillis = System.currentTimeMillis();
        remainder %= 60000;
        currentTimeMillis -= remainder;
        return currentTimeMillis;
    }

    public static String formatThisTime(Context context, Long time) {
        if (time == 0) {
            return "Have not set.";
        }
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) (DateFormat.is24HourFormat(context) ? DateFormats.hour24.clone() : DateFormats.hour12.clone());
        simpleDateFormat.setTimeZone(Global.timeZoneGmt);
        return simpleDateFormat.format(new DateTime(time));
    }

    public static void scheduleNotificationsOfAllPrayerTimesForThisDay(Context context) {
        PrayerTimeService prayerTimeService = new PrayerTimeService(context);
        String[] time = prayerTimeService.getPrayerTimeOfThisDayAndMonth(new DateTime());

        for (int index = 0; index < 7; index++) {
            try {
                Date timeToSet = DateFormats.hour24.parse(time[index]);
                Date currentSystemTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());

                assert timeToSet != null && currentSystemTime != null;
                if (timeToSet.after(currentSystemTime)) {
                    long delay = timeToSet.getTime() - currentSystemTime.getTime();
                    makeNotificationPendingIntentWithRequestCode(context, index, delay);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void makeNotificationPendingIntentWithRequestCode(Context context, int index, long delay) {
        long alertTime = Global.getCurrentTimeMillis() + delay;

        Intent intentNotificationPublisher = new Intent(context, NotificationPublisher.class);
        intentNotificationPublisher.putExtra("index", index);
        intentNotificationPublisher.putExtra("setWhen", alertTime);
        intentNotificationPublisher.putExtra("MSG", Global.NotificationMessage.get(index));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, index, intentNotificationPublisher, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
    }

    public static void cancelAllScheduledNotificationsOfThisDay(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentNotificationPublisher = new Intent(context, NotificationPublisher.class);
        assert alarmManager != null;
        for (int index = 0; index < 7; index++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, index, intentNotificationPublisher, PendingIntent.FLAG_UPDATE_CURRENT);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public static String formatThisTimeIn24(Long time) {
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormats.hour24.clone();
        simpleDateFormat.setTimeZone(Global.timeZoneGmt);
        return simpleDateFormat.format(new DateTime(time));
    }

    public static void setToSilentMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("silence_timings", 0);
        for (int i = 11; i < 16; i++) {
            try {
                if (prefs.getLong(Integer.toString(i), 0) == 0) {
                    continue;
                }
                Date currentTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());
                Date silenceTime = DateFormats.hour24.parse(Global.formatThisTimeIn24(prefs.getLong(Integer.toString(i), 0)));

                assert currentTime != null;
                if (currentTime.before(silenceTime)) {
                    assert silenceTime != null;
                    long silencerTime = Global.getCurrentTimeMillis() + silenceTime.getTime() - currentTime.getTime();
                    Intent intent1 = new Intent(context, TurnToSilentModeBroadcastReceiver.class);
                    intent1.putExtra("switchCase", "toSilent");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent1,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    assert alarmManager != null;
                    alarmManager.set(AlarmManager.RTC_WAKEUP, silencerTime, pendingIntent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}


