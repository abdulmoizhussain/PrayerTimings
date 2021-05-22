package com.example.abdul.prayertimings.services;

import android.content.Context;

import com.example.abdul.prayertimings.DatabaseManager;

public class PrayerTimeService {
    private final Context context;

    public PrayerTimeService(Context context) {
        this.context = context;
    }

    public String[] getPrayerTimeByMonthAndDate(final int month, final int dayOfMonth) {
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
}
