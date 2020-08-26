package com.example.abdul.prayertimings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import java.text.ParseException;
import java.util.Date;

public class NotificationPublisher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Global.getNotificationFlag(context)) {
            int ID = intent.getIntExtra("ID", 0);
            String contentText = intent.getStringExtra("MSG");
            long setWhen = intent.getLongExtra("setWhen", 0);

            try {
                Date currentSystemTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());
                Date timeToCheck = DateFormats.hour24.parse(DateFormats.hour24.format(setWhen));
                Date timeToCheck1 = DateFormats.hour24.parse(DateFormats.hour24.format(setWhen + 3000));

                if (currentSystemTime.equals(timeToCheck) || currentSystemTime.equals(timeToCheck1))
                    publishNotification(context, ID, contentText, setWhen);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void publishNotification(Context context, int ID, String contentText, long setWhen) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(ID, PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.abc);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(contentText)
                //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //.setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSmallIcon(R.drawable.abc)
                .setLargeIcon(bm)
                //.setLights(Color.RED, 0, 1)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(setWhen)
                //.setShowWhen(false)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(ID);
            notificationManager.notify(ID, notification);
        }
    }
}