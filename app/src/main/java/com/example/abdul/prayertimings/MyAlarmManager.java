package com.example.abdul.prayertimings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

public class MyAlarmManager {
    public static void set(final AlarmManager alarmManager, final int type, final long triggerAtMillis, final PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(type, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(type, triggerAtMillis, pendingIntent);
        }
    }
}
