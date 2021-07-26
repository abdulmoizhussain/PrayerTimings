package com.example.abdul.prayertimings.services;

import android.content.Context;

import com.example.abdul.prayertimings.DatabaseManager;
import com.example.abdul.prayertimings.DateFormats;
import com.example.abdul.prayertimings.DateTime;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class PrayerTimeService {
    private final Context context;

    public PrayerTimeService(Context context) {
        this.context = context;
    }

    public String[] getPrayerTimes(final int month, final int dayOfMonth) {
        DatabaseManager databaseManager = DatabaseManager.getInstance(context);
        databaseManager.openDatabase();
        if (databaseManager.databaseIsEmpty()) {
            if (databaseManager.failsToInsertInitialDataIntoDatabase(context)) {
                return null;
            }
        }

        String[] prayerTimesOfThisDay = databaseManager.fetchPrayerTimeOfADayOfKarachi(month, dayOfMonth);
        databaseManager.closeDatabase();

        return prayerTimesOfThisDay;
    }

    public DateTime[] getPrayerTimes(DateTime dateTime) throws ParseException, NullPointerException {
        DatabaseManager databaseManager = DatabaseManager.getInstance(context);
        databaseManager.openDatabase();
        if (databaseManager.databaseIsEmpty()) {
            if (databaseManager.failsToInsertInitialDataIntoDatabase(context)) {
                return null;
            }
        }

        String[] prayerTimesStrings = databaseManager.fetchPrayerTimeOfADayOfKarachi(dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        databaseManager.closeDatabase();

        return PrayerTimeService.convertDateStringsToDateTimes(prayerTimesStrings, dateTime);
    }

    public static DateTime[] convertDateStringsToDateTimes(String[] prayerTimes, DateTime dateTimeContext) throws ParseException, NullPointerException {
        Calendar calendarPrayerTime = Calendar.getInstance();
        calendarPrayerTime.setTime(dateTimeContext);

        DateTime[] prayerDateTimes = new DateTime[prayerTimes.length];

        for (int index = 0; index < prayerTimes.length; index++) {
            Date parsedDate = DateFormats.hour24.parse(prayerTimes[index]);
            if (parsedDate == null) {
                throw new NullPointerException();
            }

            DateTime dateTimeParsedDate = new DateTime(parsedDate.getTime());

            calendarPrayerTime.set(Calendar.HOUR_OF_DAY, dateTimeParsedDate.getHourOfDay());
            calendarPrayerTime.set(Calendar.MINUTE, dateTimeParsedDate.getMinuteOfHour());
            calendarPrayerTime.set(Calendar.SECOND, 0);
            calendarPrayerTime.set(Calendar.MILLISECOND, 0);

            prayerDateTimes[index] = new DateTime(calendarPrayerTime.getTimeInMillis());
        }
        return prayerDateTimes;
    }

    public static int findIndexOfCurrentPrayerTime(DateTime[] prayerTimes, DateTime dateTimeNow) {
        // finding index of current-prayer-time
        final int lastIndex = prayerTimes.length - 1;
        for (int index = 0; index < prayerTimes.length; index++) {
            if (index == lastIndex) {
                return index;
            } else if ((dateTimeNow.after(prayerTimes[index]) || dateTimeNow.equals(prayerTimes[index])) && dateTimeNow.before(prayerTimes[index + 1])) {
                return index;
            }
        }
        return 0;
    }
}
