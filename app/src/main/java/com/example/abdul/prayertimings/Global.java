package com.example.abdul.prayertimings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static final String DB_NAME = "Karachi.sqlite";
    public static final Map<Integer, String> IslamicMonthFullName;
    public static final Map<Integer, String> NotificationMessage;
    public static final TimeZone timeZoneGmt = TimeZone.getTimeZone("GMT");

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


    static boolean getSilenceFlag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("silence_switch", false);
    }

    static boolean getNotificationFlag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notification_switch", false);
    }


    /**
     * Removes extra seconds which cause delay.
     *
     * @return current time in milliseconds.
     */
    static long getCurrentTimeMillis() {
        long remainder, currentTimeMillis;
        remainder = currentTimeMillis = System.currentTimeMillis();
        remainder %= 60000;
        currentTimeMillis -= remainder;
        return currentTimeMillis;
    }

    static String formatThisTime(Context context, Long time) {
        if (time == 0)
            return "Have not set.";

        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) (DateFormat.is24HourFormat(context) ? DateFormats.hour24.clone() : DateFormats.hour12.clone());
        simpleDateFormat.setTimeZone(Global.timeZoneGmt);
        return simpleDateFormat.format(new Date(time));
    }

    public static void setNotifications(Context context) {
        DBHelper mDBHelper = new DBHelper(context, Global.DB_NAME);
        DateTime date = new DateTime();
        String[] time = mDBHelper.fetchTime(date.formatDate(), date.formatMonth());
        for (int index = 0; index < 7; index++) {
            try {
                Date timeToSet = DateFormats.hour24.parse(time[index]);
                Date currentSystemTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());

                assert timeToSet != null;
                if (timeToSet.after(currentSystemTime) /*|| timeToSet.equals(currentSystemTime)*/) {
                    assert currentSystemTime != null;
                    long delay = timeToSet.getTime() - currentSystemTime.getTime();
                    makeNotifications(index, delay, context);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        mDBHelper.close();
    }

    private static void makeNotifications(int index, long delay, Context context) {
        Intent alertIntent = new Intent(context, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long alertTime = Global.getCurrentTimeMillis() + delay;

        alertIntent.putExtra("index", index);
        alertIntent.putExtra("setWhen", alertTime);

        alertIntent.putExtra("MSG", Global.NotificationMessage.get(index));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, index, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
    }

    static void cancelNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 7; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    new Intent(context, NotificationPublisher.class),
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }


    public static String formatThisTimeIn24(Long time) {
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormats.hour24.clone();
        simpleDateFormat.setTimeZone(Global.timeZoneGmt);
        return simpleDateFormat.format(new Date(time));
    }

    public static void setToSilentMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("silence_timings", 0);
        for (int i = 11; i < 16; i++) {
            try {
                if (prefs.getLong(Integer.toString(i), 0) == 0)
                    continue;
                Date currentTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());
                Date silenceTime = DateFormats.hour24.parse(Global.formatThisTimeIn24(prefs.getLong(Integer.toString(i), 0)));

                assert currentTime != null;
                if (currentTime.before(silenceTime)) {
                    assert silenceTime != null;
                    long silencerTime = Global.getCurrentTimeMillis() + silenceTime.getTime() - currentTime.getTime();
                    Intent intent1 = new Intent(context, MobileSilencer.class);
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


