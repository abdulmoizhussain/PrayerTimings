package com.example.abdul.prayertimings;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AppStartup extends Application {
    public static class AppNotificationChannels {
        public static String PRAYER_TIME_NOTIFICATION = "CHANNEL_PRAYER_TIME_NOTIFICATIONS";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    // source: https://developer.android.com/training/notify-user/build-notification#java
    // source: https://www.youtube.com/watch?v=tTbd1Mfi-Sk
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AppNotificationChannels.PRAYER_TIME_NOTIFICATION,
                    "Prayer Time Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("These will notify you about every Prayer timings.");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
