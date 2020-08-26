package com.example.abdul.prayertimings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Updater extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
//            Toast.makeText(context, "date changed.", Toast.LENGTH_LONG).show();
//        }

        if (Global.getNotificationFlag(context)) {
            Global.cancelNotifications(context);
            Global.setNotifications(context);
        }

        if (Global.getSilenceFlag(context)) {
            Global.setToSilentMode(context);
        }
    }
}
