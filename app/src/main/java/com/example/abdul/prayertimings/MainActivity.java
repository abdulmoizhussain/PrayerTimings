package com.example.abdul.prayertimings;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abdul.prayertimings.services.PrayerTimeService;

import org.joda.time.Chronology;
import org.joda.time.LocalDate;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private TextView textViewDateAD, textViewDateAH;
    private BroadcastReceiver broadcastReceiver;
    private ColorStateList defaultColorStateList;
    private static final int colorCodeGreen = Color.parseColor("#008000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings_screen, true);

        textViewDateAD = findViewById(R.id.textViewAdValue);
        textViewDateAH = findViewById(R.id.textViewAhValue);
        defaultColorStateList = textViewDateAD.getTextColors();

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
    protected void onResume() {
        super.onResume();
        {
            DateTime date = new DateTime();
            setDateAnnoDomini(date);
            setDateAnnoHegirae(date);
            checkAndSetTimeWithDatabaseManager(date);
        }
//        clearAllNotificationsFromShutter();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean("firstTime", false)) {
            //----\  /---
            // ----\/---- run your one time code here
            checkAndScheduleNotifications();
            // mark first time has run.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
        }
        registerActionTimeTickBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        unregisterBroadcastReceiver();
        super.onPause();
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

    private void clearAllNotificationsFromShutter() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for (int index = 0; index < 7; index++) {
            notificationManager.cancel(index);
        }
    }

    private void checkAndSetTimeWithDatabaseManager(DateTime dateTime) {
        PrayerTimeService prayerTimeService = new PrayerTimeService(this);
        Calendar calendar = CalendarHelper.toCalendar(dateTime);
        renderPrayerTimings(
                prayerTimeService.getPrayerTimeByMonthAndDate(
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
        );
    }

    private void checkAndScheduleNotifications() {
        Global.cancelAllScheduledNotificationsOfThisDay(this);
        if (Global.getNotificationFlag(this)) {
            Global.scheduleNotificationsOfAllPrayerTimesForThisDay(this);
        }
    }

    private void registerActionTimeTickBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DateTime date = new DateTime();
                setDateAnnoDomini(date);
                setDateAnnoHegirae(date);
                checkAndSetTimeWithDatabaseManager(date);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private void renderPrayerTimings(String[] time) {
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
            Date currentSystemTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());
            for (int index = 0; index < 7; index++) {
                Date timeToSet = DateFormats.hour24.parse(time[index]);

                assert currentSystemTime != null;
                assert timeToSet != null;
                {
                    String timeString = is24HourFormat ? DateFormats.hour24.format(timeToSet) : DateFormats.hour12.format(timeToSet);
                    view[index].setText(timeString);
                }

                view[index].setTextColor(defaultColorStateList);
                if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet) || currentSystemTime.before(DateFormats.hour24.parse(time[0]))) {
                    view[index].setTextColor(colorCodeGreen);
                    if ((index - 1) > -1) {
                        view[index - 1].setTextColor(defaultColorStateList);
                    } else {
                        view[6].setTextColor(defaultColorStateList);
                    }
                }
            }
        } catch (ParseException e) {
            NotificationManagement.notifyWithErrorDetails(this, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sets date of Anno Domini (In the year of Jesus Christ) a.k.a AD.
     *
     * @param date DateTime/Date from which AD-date will be displayed.
     */
    private void setDateAnnoDomini(DateTime date) {
        textViewDateAD.setText(String.format("%s-%s-%s", date.formatDate(), date.formatMonth(), date.formatYear()));
    }

    /**
     * Sets date of Anno Hegirae (In the year of the Hijra). Anno (in the year) + Hegirae (of the Hijra).
     *
     * @param date DateTime/Date from which AH-date will be calculated and displayed.
     */
    private void setDateAnnoHegirae(DateTime date) {
        Chronology isoChronology = ISOChronology.getInstanceUTC();
        Chronology islamicChronology = IslamicChronology.getInstanceUTC();
        LocalDate localDateIso = new LocalDate(
                Integer.parseInt(date.formatYear()),
                Integer.parseInt(date.formatMonth_MM()),
                Integer.parseInt(date.formatDate()),
                isoChronology
        );
        LocalDate localDateIslamic = new LocalDate(localDateIso.toDateTimeAtStartOfDay(), islamicChronology);
        String day = localDateIslamic.toString("dd");
        String month = Global.IslamicMonthFullName.get(localDateIslamic.getMonthOfYear());
        String year = localDateIslamic.toString("yyyy");

        textViewDateAH.setText(String.format("%s-%s-%s", day, month, year));
    }
}
