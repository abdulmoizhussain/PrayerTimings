package com.example.abdul.prayertimings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReScheduleNotificationsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Global.IntentActions.contains(intent.getAction())) {
            Global.cancelAllScheduledNotificationsOfThisDay(context);
            if (Global.getNotificationFlag(context)) {
                Global.scheduleNotificationsOfAllPrayerTimesForThisDay(context);
            }

            if (Global.getSilenceFlag(context)) {
                Global.setToSilentMode(context);
            }
        }
    }
}
