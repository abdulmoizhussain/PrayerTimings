package com.example.abdul.prayertimings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;

public class NotificationPublisher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Global.getNotificationFlag(context)) {
            int notificationId = intent.getIntExtra("index", 0);
            long setWhen = intent.getLongExtra("setWhen", 0);
            String contentText = intent.getStringExtra("MSG");

            try {
                Date currentSystemTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());
                Date timeToCheck = DateFormats.hour24.parse(DateFormats.hour24.format(setWhen));
                Date timeToCheck1 = DateFormats.hour24.parse(DateFormats.hour24.format(setWhen + 3000));

                assert currentSystemTime != null;
                if (currentSystemTime.equals(timeToCheck) || currentSystemTime.equals(timeToCheck1)) {
                    NotificationManagement.publishNotification(context, notificationId, contentText, setWhen);
                }
            } catch (ParseException e) {
                NotificationManagement.notifyWithErrorDetails(context, e.toString());
                e.printStackTrace();
                Toast.makeText(context, "Unable to parse time while publishing notification.", Toast.LENGTH_LONG).show();
            }
        }
    }


}