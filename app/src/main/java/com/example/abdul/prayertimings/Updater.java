package com.example.abdul.prayertimings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Updater extends BroadcastReceiver {
	Global global;
	@Override
	public void onReceive(Context context, Intent intent) {
		global = new Global();
/*
		if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
			Toast.makeText(context, "date changed.", Toast.LENGTH_LONG).show();
		}
*/
	if (global.getNotificationFlag(context)) {
		global.cancelNotifications(context);
			try {
				global.setNotifications(context);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Error creating notifications.", Toast.LENGTH_LONG).show();
			}
		}
		
		if (global.getSilenceFlag (context)) {
			global.setToSilentMode (context);
		}
	}
}
