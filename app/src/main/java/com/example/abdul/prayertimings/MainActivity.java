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
    private TextView textViewDateADValue;
    private BroadcastReceiver broadcastReceiver;
    private ColorStateList _defaultColorStateList;
    private int _colorCodeGreen = Color.parseColor("#008000");
    private boolean receiverIsRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.settings_screen, true);

        textViewDateADValue = findViewById(R.id.textView15);
        _defaultColorStateList = textViewDateADValue.getTextColors();

        registerUIUpdater();

//		testNotification();
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
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

/*
		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			public void run() {
				checkAndSetTimeWithDatabaseManager();
				handler.postDelayed(this, 1000);
			}
		};
		handler.postDelayed(runnable, 1000);
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



/*
	void setTime (DBHelper mDBHelper) {
		String[] text_view = getResources().getStringArray(R.array.text_view_array);
		TextView view[] = new TextView[7];
		view[0] = (TextView) findViewById(R.id.textView);
		view[1] = (TextView) findViewById(R.id.textView2);
		view[2] = (TextView) findViewById(R.id.textView3);
		view[3] = (TextView) findViewById(R.id.textView4);
		view[4] = (TextView) findViewById(R.id.textView5);
		view[5] = (TextView) findViewById(R.id.textView6);
		view[6] = (TextView) findViewById(R.id.textView7);

		try {
			String time[] = mDBHelper.fetchTime(currentDate(), currentMonth());

			Calendar calender = Calendar.getInstance();
			try {
				Date currentSystemTime = format24().parse(format24().format(calender.getTime()));
				for (int i = 0; i < 7; i++) {
					Date timeToSet = format24().parse(time[i]);

					if (_is12HourFormat) {
						view[i].setText(text_view[i] +" "+ time[i]);
					} else {
						view[i].setText(text_view[i] +" "+ format12().format(timeToSet));
						//view[i].setText(format12().format(timeToSet));
					}

					view[i].setTextColor(_defaultColorStateList);
					if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet)
							|| currentSystemTime.before(format24().parse(time[0]))) {

						view[i].setTextColor(_colorCodeGreen);
						//view[i].getTextColors();
						if ( (i-1) > -1) {
							//view[i-1].setTextColor(_colorCodeGreen);
							view[i-1].setTextColor(_defaultColorStateList);
						} else {
							//view[6].setTextColor(_colorCodeGreen);
							view[6].setTextColor(_defaultColorStateList);
						}
					}
				}
				mDBHelper.close();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this,"Error Reading Database.",Toast.LENGTH_LONG).show();
		}
	}
*/

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
            for (int i = 0; i < 7; i++) {
                Date timeToSet = DateFormats.hour24.parse(time[i]);

                assert currentSystemTime != null;
                assert timeToSet != null;
                {
                    String timeString = is24HourFormat ? DateFormats.hour24.format(timeToSet) : DateFormats.hour12.format(timeToSet);
                    view[i].setText(timeString);
                }

                view[i].setTextColor(_defaultColorStateList);
                if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet) || currentSystemTime.before(DateFormats.hour24.parse(time[0]))) {
                    view[i].setTextColor(_colorCodeGreen);
                    if ((i - 1) > -1) {
                        view[i - 1].setTextColor(_defaultColorStateList);
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
        Chronology iso = ISOChronology.getInstanceUTC();
        Chronology hijri = IslamicChronology.getInstanceUTC();

        LocalDate todayIso = new LocalDate(
                Integer.parseInt(Global.currentYear()),
                Integer.parseInt(Global.currentMonth_MM()),
                Integer.parseInt(Global.currentDate()),
                iso
        );
        LocalDate todayHijri = new LocalDate(todayIso.toDateTimeAtStartOfDay(), hijri);

        TextView textView = (TextView) findViewById(R.id.textView8);
        textView.setText(todayHijri.toString("dd"));
        textView.append("-" + getNameOfIslamicMonth(todayHijri.getMonthOfYear()) + "-");
        textView.append(todayHijri.toString("yyyy"));
    }

    private String getNameOfIslamicMonth(int monthNumber) {
        switch (monthNumber) {
            case 1:
                return "Muharram";
            case 2:
                return "Safar";
            case 3:
                return "Rabiul-Awwal";
            case 4:
                return "Rabi-uthani";
            case 5:
                return "Jumadi-ul-Awwal";
            case 6:
                return "Jumadi-uthani";
            case 7:
                return "Rajab";
            case 8:
                return "Sha’ban";
            case 9:
                return "Ramadan";
            case 10:
                return "Shawwal";
            case 11:
                return "Zhul-Q’ada";
            case 12:
                return "Zhul-Hijja";
            default:
                return " ";
        }
    }

	/*
	private Notification getNotification(String content, long setWhenTime) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("Scheduled Notification");
		builder.setContentText(content);
		builder.setSmallIcon(R.drawable.abc);
		builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
		//builder.setWhen(setWhenTime);
		builder.setShowWhen(true);

		return builder.build();
	}
*/
    //START OF  TRY 2
    //long alertTime = SystemClock.elapsedRealtime()+3*60*1000;
/*
		long alertTime = System.currentTimeMillis() + 3*60*1000;
		Intent alertIntent = new Intent(this,
				NotificationPublisher.class);
		AlarmManager alarmManager = (AlarmManager)
				getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
*/
				/*
//				Sample code to create Handler/Runnable/Thread
		Handler handler = new Handler(this.getMainLooper());
		Runnable runnable = new Runnable() {
			@Override
			public void run () {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						checkNsetTime();
					}
				});
			}
		};
		new Thread(runnable).start();
		*/
}
