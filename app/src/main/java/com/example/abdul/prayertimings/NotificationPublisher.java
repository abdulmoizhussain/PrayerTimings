package com.example.abdul.prayertimings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class NotificationPublisher extends BroadcastReceiver {
	Global global;
	@Override
	public void onReceive(Context context, Intent intent) {
		global = new Global();

		// START OF TRY 3
		if (global.getNotificationFlag(context)) {
			int ID = intent.getIntExtra("ID", 0);
			String contentText = intent.getStringExtra("MSG");
			long setWhen = intent.getLongExtra("setWhen", 0);

			try {
				Calendar calender = Calendar.getInstance();// can be replaced with "Date date = new Date();"
				Date currentSystemTime = Global.format24().parse(Global.format24().format(calender.getTime()));
				Date timeToCheck = Global.format24().parse(Global.format24().format(setWhen));
				Date timeToCheck1 = Global.format24().parse(Global.format24().format(setWhen + 3000));
				
				if (currentSystemTime.equals(timeToCheck) || currentSystemTime.equals(timeToCheck1))
					publishNotification(context, ID, contentText, setWhen);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// END OF TRY 3
	}

	void publishNotification (Context context, int ID, String contentText, long setWhen) {
		try {
			Intent notificationIntent = new Intent(context, MainActivity.class);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(notificationIntent);

			PendingIntent pendingIntent = stackBuilder.getPendingIntent(ID, PendingIntent.FLAG_CANCEL_CURRENT);
			
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(),R.drawable.abc);
			
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
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(ID);
			notificationManager.notify(ID, notification);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}