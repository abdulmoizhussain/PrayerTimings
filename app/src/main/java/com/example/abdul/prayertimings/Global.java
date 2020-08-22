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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by Abdul on 8/6/2017.
 * This is an extra line to remove warning.
 */

class Global {
    public static final String DB_NAME = "Karachi.sqlite";
    public static final HashMap<Integer, String> fullNameOfIslamicMonths;

    static {
        HashMap<Integer, String> namesOfIslamicMonths = new HashMap<>();
        namesOfIslamicMonths.put(1, "Muharram");
        namesOfIslamicMonths.put(2, "Safar");
        namesOfIslamicMonths.put(3, "Rabiul-Awwal");
        namesOfIslamicMonths.put(4, "Rabi-uthani");
        namesOfIslamicMonths.put(5, "Jumadi-ul-Awwal");
        namesOfIslamicMonths.put(6, "Jumadi-uthani");
        namesOfIslamicMonths.put(7, "Rajab");
        namesOfIslamicMonths.put(8, "Sha’ban");
        namesOfIslamicMonths.put(9, "Ramadan");
        namesOfIslamicMonths.put(10, "Shawwal");
        namesOfIslamicMonths.put(11, "Zhul-Q’ada");
        namesOfIslamicMonths.put(12, "Zhul-Hijja");
        fullNameOfIslamicMonths = namesOfIslamicMonths;
    }

    public static final TimeZone timeZoneGmt = TimeZone.getTimeZone("GMT");

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
        String[] time = mDBHelper.fetchTime(Global.currentDate(), Global.currentMonth());
        for (int index = 0; index < 7; index++) {
            try {
                Date timeToSet = DateFormats.hour24.parse(time[index]);
                Calendar calender = Calendar.getInstance();
                Date currentSystemTime = DateFormats.hour24.parse(DateFormats.hour24.format(calender.getTime()));

                if (timeToSet.after(currentSystemTime) /*|| timeToSet.equals(currentSystemTime)*/) {
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

        alertIntent.putExtra("MSG", getPrayerTimeMessageByIndexForNotification(index));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, index, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
    }

    private static String getPrayerTimeMessageByIndexForNotification(int index) {
        switch (index) {
            case 0:
                return "Fajr time has started.";
            case 1:
                return "Fajr time has ended.";
            case 2:
                return "Zawal time.";
            case 3:
                return "Zuhur time has started.";
            case 4:
                return "Asr time has started.";
            case 5:
                return "Maghrib time has started.";
            case 6:
                return "Isha time has started.";
            default:
                return "---";
        }
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

    public static String currentDate() {
        return DateFormats.dd.format(new Date());
    }

    public static String currentMonth() {
        return DateFormats.MMMM.format(new Date());
    }

    public static String currentMonth_MM() {
        return DateFormats.MM.format(new Date());
    }

    public static String currentYear() {
        return DateFormats.yyyy.format(new Date());
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
                Date currentTime = DateFormats.hour24.parse(DateFormats.hour24.format(Calendar.getInstance().getTime()));
                Date silenceTime = DateFormats.hour24.parse(Global.formatThisTimeIn24(prefs.getLong(Integer.toString(i), 0)));

                if (currentTime.before(silenceTime)) {

                    long silencerTime = Global.getCurrentTimeMillis() + silenceTime.getTime() - currentTime.getTime();
                    Intent intent1 = new Intent(context, MobileSilencer.class);
                    intent1.putExtra("switchCase", "toSilent");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent1,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, silencerTime, pendingIntent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}