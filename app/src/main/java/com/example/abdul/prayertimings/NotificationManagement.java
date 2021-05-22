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
//        Bitmap bitmapAppIconLarge = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
//        builder.setLargeIcon(bitmapAppIconLarge);

        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        builder.setContentText(contentText);
        builder.setSmallIcon(smallIcon);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setWhen(setWhen);
        builder.setShowWhen(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, builder.build());
    }

    public static void notifyWithErrorDetails(Context context, String details) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppStartup.AppNotificationChannels.PRAYER_TIME_NOTIFICATION);

        builder.setContentTitle("Error!");
        builder.setContentText(details);
        builder.setSmallIcon(smallIcon);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(details));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int countAsId = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt("error_notification_count", 2000);

        notificationManager.notify(countAsId, builder.build());

        SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedPreferences.putInt("error_notification_count", ++countAsId);
        sharedPreferences.apply();
    }
}
