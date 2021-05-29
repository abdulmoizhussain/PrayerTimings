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
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String switchCase = intent.getStringExtra("switchCase");

            assert audioManager != null && switchCase != null;

            switch (switchCase) {
                case "toSilent":
                    SharedPreferences sharedPreferences = context.getSharedPreferences("silence_timings", 0);

                    Intent intent1 = new Intent(context, TurnToSilentModeBroadcastReceiver.class);
                    intent1.putExtra("ringerMode", audioManager.getRingerMode());
                    intent1.putExtra("switchCase", "toGeneral");
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                    long silenceDuration = sharedPreferences.getInt("silence_duration", 10);
                    long backToGeneralTime = Global.getCurrentTimeMillisTruncated() + (60 * 1000 * silenceDuration);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    MyAlarmManager.set(context, AlarmManager.RTC_WAKEUP, backToGeneralTime, pendingIntent);
                    break;

                case "toGeneral":
                    audioManager.setRingerMode(intent.getIntExtra("ringerMode", 2));
                    break;
            }
        }
    }
}
