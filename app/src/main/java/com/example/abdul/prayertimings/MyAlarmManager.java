package com.example.abdul.prayertimings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

public class MyAlarmManager {
    public static void set(final Context context, final int type, final long triggerAtMillis, final PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(type, triggerAtMillis, pendingIntent);
        }
    }

    public static void setAlarmClock(final Context context, final int fallbackAlarmManagerType, final long triggerAtMillis, final PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent), pendingIntent);
        } else {
            alarmManager.setExact(fallbackAlarmManagerType, triggerAtMillis, pendingIntent);
        }
    }
}
