package com.example.abdul.prayertimings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class MobileSilencer extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Global global = new Global();
		
		if (global.getSilenceFlag (context)) {
			AudioManager mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			switch (intent.getStringExtra("switchCase")) {
				case "toSilent":
					SharedPreferences sharedPreferences = context.getSharedPreferences("silence_timings", 0);
					
					Intent intent1 = new Intent(context, MobileSilencer.class);
					intent1.putExtra("ringerMode", mode.getRingerMode());
					intent1.putExtra("switchCase", "toGeneral");
					mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					/*
					long remainder, currentTimeMillis;
					currentTimeMillis = System.currentTimeMillis()/1000;
					remainder = currentTimeMillis;
					remainder %= 60;
					currentTimeMillis -= remainder;
					currentTimeMillis *= 1000;
					*/
					long backToGeneralTime = global.getCurrentTimeMillis() +
							(60*1000*sharedPreferences.getInt("silence_duration", 10));
					
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent1,
							PendingIntent.FLAG_UPDATE_CURRENT);
					AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					alarmManager.set(AlarmManager.RTC_WAKEUP, backToGeneralTime, pendingIntent);
					break;
				
				case "toGeneral":
					mode.setRingerMode(intent.getIntExtra("ringerMode", 2));
					break;
			}
		}
	}
}
