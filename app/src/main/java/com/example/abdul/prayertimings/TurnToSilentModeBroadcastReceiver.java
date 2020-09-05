package com.example.abdul.prayertimings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class TurnToSilentModeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Global.getSilenceFlag(context)) {
            AudioManager mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String switchCase = intent.getStringExtra("switchCase");
            if (mode == null || switchCase == null) {
                return;
            }
            switch (switchCase) {
                case "toSilent":
                    SharedPreferences sharedPreferences = context.getSharedPreferences("silence_timings", 0);

                    Intent intent1 = new Intent(context, TurnToSilentModeBroadcastReceiver.class);
                    intent1.putExtra("ringerMode", mode.getRingerMode());
                    intent1.putExtra("switchCase", "toGeneral");
                    mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                    long silenceDuration = sharedPreferences.getInt("silence_duration", 10);
                    long backToGeneralTime = Global.getCurrentTimeMillis() + (60 * 1000 * silenceDuration);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager == null) {
                        break;
                    }
                    alarmManager.set(AlarmManager.RTC_WAKEUP, backToGeneralTime, pendingIntent);
                    break;

                case "toGeneral":
                    mode.setRingerMode(intent.getIntExtra("ringerMode", 2));
                    break;
            }
        }
    }
}
