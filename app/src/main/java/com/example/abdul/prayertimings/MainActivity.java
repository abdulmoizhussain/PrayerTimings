package com.example.abdul.prayertimings;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.Chronology;
import org.joda.time.LocalDate;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;

import java.text.ParseException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private TextView textViewDateADValue, textViewDateHijri;
    private BroadcastReceiver broadcastReceiver;
    private ColorStateList _defaultColorStateList;
    private static final int _colorCodeGreen = Color.parseColor("#008000");
    private boolean receiverIsRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.settings_screen, true);

        textViewDateHijri = findViewById(R.id.textView8);
        textViewDateADValue = findViewById(R.id.textView15);
        _defaultColorStateList = textViewDateADValue.getTextColors();

        registerUIUpdater();

/*
		//FOR DEVICES WHICH HAVE HARDWARE OPTIONS/SETTINGS BUTTONS
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch (Exception e) {
			// presumably, not relevant
		}
*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterUIUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setHijriDate();
        textViewDateADValue.setText(String.format("%s-%s-%s", Global.currentDate(), Global.currentMonth(), Global.currentYear()));
        checkAndSetTimeWithDatabaseManager();
        clearNotifications();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean("firstTime", false)) {
            //----\  /---
            // ----\/---- run your one time code here
            checkAndSetNotifications();
            // mark first time has run.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
        }
        registerUIUpdater();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterUIUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterUIUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings, menu);
        //Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(true);
        //super.onCreateOptionsMenu(menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, PreferenceClass.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void clearNotifications() {
        for (int ID = 0; ID < 7; ID++) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.cancel(ID);
        }
    }

    private void checkAndSetTimeWithDatabaseManager() {
        DatabaseManager.initializeInstance(new DBHelper(this, Global.DB_NAME));

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.copyDataBase(this, Global.DB_NAME);

        SQLiteDatabase database = databaseManager.openDatabase();
        String[] time = databaseManager.fetchTime(Global.currentDate(), Global.currentMonth(), database);

        renderPrayerTimings(time);
        // database.close(); Don't close it directly!
        databaseManager.closeDatabase(); // correct way
    }

    private void checkAndSetNotifications() {
        if (Global.getNotificationFlag(this)) {
            Global.cancelNotifications(this);
            Global.setNotifications(this);
        }
    }

    private void registerUIUpdater() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setHijriDate();
                textViewDateADValue.setText(String.format("%s-%s-%s", Global.currentDate(), Global.currentMonth(), Global.currentYear()));
                checkAndSetTimeWithDatabaseManager();
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
        receiverIsRegistered = true;
    }

    private void unregisterUIUpdate() {
        if (receiverIsRegistered && broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        receiverIsRegistered = false;
    }

    void renderPrayerTimings(String[] time) {
        TextView[] view = new TextView[7];
        view[0] = findViewById(R.id.textViewFajarTime);
        view[1] = findViewById(R.id.textViewFajarEndsTime);
        view[2] = findViewById(R.id.textViewZawalStartingTime);
        view[3] = findViewById(R.id.textViewZuhurTime);
        view[4] = findViewById(R.id.textViewAsrTime);
        view[5] = findViewById(R.id.textViewMaghribTime);
        view[6] = findViewById(R.id.textViewIshaTime);

        final boolean is24HourFormat = DateFormat.is24HourFormat(this);
        try {
            Date currentSystemTime = DateFormats.hour24.parse(DateFormats.hour24.format(new Date()));
            for (int index = 0; index < 7; index++) {
                Date timeToSet = DateFormats.hour24.parse(time[index]);

                assert currentSystemTime != null;
                assert timeToSet != null;
                {
                    String timeString = is24HourFormat ? DateFormats.hour24.format(timeToSet) : DateFormats.hour12.format(timeToSet);
                    view[index].setText(timeString);
                }

                view[index].setTextColor(_defaultColorStateList);
                if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet) || currentSystemTime.before(DateFormats.hour24.parse(time[0]))) {
                    view[index].setTextColor(_colorCodeGreen);
                    if ((index - 1) > -1) {
                        view[index - 1].setTextColor(_defaultColorStateList);
                    } else {
                        view[6].setTextColor(_defaultColorStateList);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void setHijriDate() {
        Chronology isoChronology = ISOChronology.getInstanceUTC();
        Chronology islamicChronology = IslamicChronology.getInstanceUTC();
        LocalDate localDateIso = new LocalDate(
                Integer.parseInt(Global.currentYear()),
                Integer.parseInt(Global.currentMonth_MM()),
                Integer.parseInt(Global.currentDate()),
                isoChronology
        );
        LocalDate localDateIslamic = new LocalDate(localDateIso.toDateTimeAtStartOfDay(), islamicChronology);
        String date = localDateIslamic.toString("dd");
        String month = Global.IslamicMonthFullName.get(localDateIslamic.getMonthOfYear());
        String year = localDateIslamic.toString("yyyy");

        textViewDateHijri.setText(String.format("%s-%s-%s", date, month, year));
    }
//	    Sample code to create Handler/Runnable/Thread
//		Handler handler = new Handler(this.getMainLooper());
//		Runnable runnable = new Runnable() {
//			@Override
//			public void run () {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				handler.post(new Runnable() {
//					@Override
//					public void run() {
//						checkNsetTime();
//					}
//				});
//			}
//		};
//		new Thread(runnable).start();
}
