package com.example.abdul.prayertimings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class NotificationManagement {
    final private static int smallIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ? R.drawable.app_icon_transparent : R.drawable.app_icon;

    public static void publishNotification(Context context, int notificationId, String contentText, long setWhen) {
        Intent intentMainActivity = new Intent(context, MainActivity.class);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intentMainActivity);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppStartup.AppNotificationChannels.PRAYER_TIME_NOTIFICATION);
        builder.setSmallIcon(smallIcon);
//        Bitmap bitmapAppIconLarge = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
//        builder.setLargeIcon(bitmapAppIconLarge);

        Notification notification = builder
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(setWhen)
                .setShowWhen(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, notification);
    }

    public static void notifyWithErrorDetails(Context context, String details) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppStartup.AppNotificationChannels.PRAYER_TIME_NOTIFICATION);
        builder.setSmallIcon(smallIcon);

        Notification notification = builder
                .setContentTitle("Error!")
                .setContentText(details)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(details))
//                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int countAsId = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt("error_notification_count", 2000);

        notificationManager.cancel(countAsId);
        notificationManager.notify(countAsId, notification);

        SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedPreferences.putInt("error_notification_count", ++countAsId);
        sharedPreferences.apply();
    }
}
