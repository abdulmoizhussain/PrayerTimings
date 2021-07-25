package com.example.abdul.prayertimings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textViewDateAD, textViewDateAH, textViewRemainingTime;
    private BroadcastReceiver broadcastReceiver;
    private ColorStateList defaultColorStateList;
    private static final int colorCodeGreen = Color.parseColor("#008000");
    private final Handler handler = new Handler();
    private Runnable runnable;
    private final int delay = 1000;
    private PrayerTimeService prayerTimeService;
    private final TextView[] prayerTimeTextViews = new TextView[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prayerTimeTextViews[0] = findViewById(R.id.textViewFajarTime);
        prayerTimeTextViews[1] = findViewById(R.id.textViewFajarEndsTime);
        prayerTimeTextViews[2] = findViewById(R.id.textViewZawalStartingTime);
        prayerTimeTextViews[3] = findViewById(R.id.textViewZuhurTime);
        prayerTimeTextViews[4] = findViewById(R.id.textViewAsrTime);
        prayerTimeTextViews[5] = findViewById(R.id.textViewMaghribTime);
        prayerTimeTextViews[6] = findViewById(R.id.textViewIshaTime);
        textViewRemainingTime = findViewById(R.id.textViewRemainingTime);

        prayerTimeService = new PrayerTimeService(this);
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

    static class DataClass {
        private DateTime lastDate;
        private DateTime[] prayerTimesCurrentDay, prayerTimesNextDay;

        public DateTime getLastDate() {
            return lastDate;
        }

        public void setLastDate(DateTime lastDate) {
            this.lastDate = lastDate;
        }

        public DateTime[] getPrayerTimesCurrentDay() {
            return prayerTimesCurrentDay;
        }

        public void setPrayerTimesCurrentDay(DateTime[] prayerTimesCurrentDay) {
            this.prayerTimesCurrentDay = prayerTimesCurrentDay;
        }

        public DateTime[] getPrayerTimesNextDay() {
            return prayerTimesNextDay;
        }

        public void setPrayerTimesNextDay(DateTime[] prayerTimesNextDay) {
            this.prayerTimesNextDay = prayerTimesNextDay;
        }
    }

    private void runnableListener(DataClass dataClass) {
        DateTime dateTimeNow = new DateTime();
        long dateTimeNowMillis = dateTimeNow.getTime();

        if (dataClass.getLastDate().getMinuteOfHour() != dateTimeNow.getMinuteOfHour()) {
            // it is change of minute
            // now update the values from DB.

            dataClass.setLastDate(new DateTime(dateTimeNowMillis));
            try {
                dataClass.setPrayerTimesCurrentDay(prayerTimeService.getPrayerTimes(dateTimeNow));
                dataClass.setPrayerTimesNextDay(prayerTimeService.getPrayerTimes(dateTimeNow.addDays(1)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // TODO there is a bug in here

        DateTime[] prayerTimesOfThisDay = dataClass.getPrayerTimesCurrentDay();

        int indexOfCurrentPrayerTime = PrayerTimeService.findIndexOfCurrentPrayerTime(prayerTimesOfThisDay, dateTimeNow);

        long timeMillisNextPrayer;

        Console.log(Integer.toString(indexOfCurrentPrayerTime));
        Console.log(Integer.toString(prayerTimesOfThisDay.length));

        if (indexOfCurrentPrayerTime == (prayerTimesOfThisDay.length - 1)) {
            // when it is the last index it means it is Esha-prayer time,
            // now we have to take Fajr-prayer-time of the next day.
            DateTime[] prayerTimesOfNextDay = dataClass.getPrayerTimesNextDay();

            // getting Fajr-prayer time of next day.
            timeMillisNextPrayer = prayerTimesOfNextDay[0].getTime();
        } else {
            timeMillisNextPrayer = prayerTimesOfThisDay[indexOfCurrentPrayerTime + 1].getTime();
        }

        Console.log("--------");
        Console.log(new Date(timeMillisNextPrayer).toString());
        Console.log(new Date(dateTimeNowMillis).toString());

        DateTime difference = new DateTime(timeMillisNextPrayer - dateTimeNowMillis);

        // source: https://stackoverflow.com/a/13904621
        // source: https://stackoverflow.com/a/2003612
        long totalHours = difference.totalHours();
        long totalMinutesAfterHours = difference.totalMinutes() - (totalHours * 60);
        long totalSeconds = difference.totalSeconds() % 60L;

        // source to add leading zeros: https://javarevisited.blogspot.com/2013/02/add-leading-zeros-to-integers-Java-String-left-padding-example-program.html
        textViewRemainingTime.setText(
                String.format(Locale.US, "Next time-change in: %02d:%02d:%02d", totalHours, totalMinutesAfterHours, totalSeconds)
        );
    }

    @Override
    protected void onResume() {
        final DataClass dataClass = new DataClass();
        DateTime dateTimeNow = new DateTime();

        dataClass.setLastDate(dateTimeNow);
        try {
            dataClass.setPrayerTimesCurrentDay(prayerTimeService.getPrayerTimes(dataClass.getLastDate()));
            dataClass.setPrayerTimesNextDay(prayerTimeService.getPrayerTimes(dataClass.getLastDate().addDays(1)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        runnableListener(dataClass);

        // source: https://stackoverflow.com/a/40058010
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                // do something
                runnableListener(dataClass);

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        {
            setDateAnnoDomini(dateTimeNow);
            setDateAnnoHegirae(dateTimeNow);
            checkAndSetTimeWithDatabaseManager(dateTimeNow);
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

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterBroadcastReceiver();
        handler.removeCallbacks(runnable); //stop handler when activity not visible
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

//    private void clearAllNotificationsFromShutter() {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        for (int index = 0; index < 7; index++) {
//            notificationManager.cancel(index);
//        }
//    }

    private void checkAndSetTimeWithDatabaseManager(DateTime dateTime) {
        renderPrayerTimings(
                prayerTimeService.getPrayerTimes(
                        dateTime.getMonthOfYear(),
                        dateTime.getDayOfMonth()
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
        final boolean is24HourFormat = DateFormat.is24HourFormat(this);
        try {
            Date currentSystemTime = DateFormats.hour24.parse(new DateTime().formatIn24Hour());
            for (int index = 0; index < 7; index++) {
                Date timeToSet = DateFormats.hour24.parse(time[index]);

                assert currentSystemTime != null;
                assert timeToSet != null;
                {
                    String timeString = is24HourFormat ? DateFormats.hour24.format(timeToSet) : DateFormats.hour12.format(timeToSet);
                    prayerTimeTextViews[index].setText(timeString);
                }

                prayerTimeTextViews[index].setTextColor(defaultColorStateList);
                if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet) || currentSystemTime.before(DateFormats.hour24.parse(time[0]))) {
                    prayerTimeTextViews[index].setTextColor(colorCodeGreen);
                    if ((index - 1) > -1) {
                        prayerTimeTextViews[index - 1].setTextColor(defaultColorStateList);
                    } else {
                        prayerTimeTextViews[6].setTextColor(defaultColorStateList);
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
