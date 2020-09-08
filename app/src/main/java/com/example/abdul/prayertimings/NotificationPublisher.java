package com.example.abdul.prayertimings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

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
                    publishNotification(context, notificationId, contentText, setWhen);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(context, "Unable to parse time while publishing notification.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void publishNotification(Context context, int notificationId, String contentText, long setWhen) {
        Intent intentMainActivity = new Intent(context, MainActivity.class);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intentMainActivity);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "DefaultNotificationChannel");

        builder.setSmallIcon(
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ? R.drawable.app_icon_transparent : R.drawable.app_icon
        );
//        Bitmap bitmapAppIconLarge = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
//        builder.setLargeIcon(bitmapAppIconLarge);

        Notification notification = builder
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(setWhen)
                .setShowWhen(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;

        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, notification);
    }
}