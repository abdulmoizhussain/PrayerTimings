package com.example.abdul.prayertimings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationPublisher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Global.getNotificationFlag(context)) {
            return;
        }

        int notificationId = intent.getIntExtra("index", 0);
        long setWhen = intent.getLongExtra("setWhen", 0);
        String contentText = intent.getStringExtra("MSG");
        NotificationManagement.publishNotification(context, notificationId, contentText, setWhen);
    }
}