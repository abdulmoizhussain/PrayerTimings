package com.example.abdul.prayertimings.services;

import android.content.Context;

import com.example.abdul.prayertimings.DatabaseManager;
import com.example.abdul.prayertimings.DateTime;

import java.util.Calendar;

public class PrayerTimeService {
    private Context context;

    public PrayerTimeService(Context context) {
        this.context = context;
    }

    public String[] getPrayerTimeOfThisDayAndMonth(DateTime dateTime) {
        DatabaseManager databaseManager = DatabaseManager.getInstance(context);
        databaseManager.openDatabase();
        if (databaseManager.databaseIsEmpty()) {
            if (databaseManager.failsToInsertInitialDataIntoDatabase(context)) {
                return null;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);

        String[] prayerTimesOfThisDay = databaseManager.fetchPrayerTimeOfADayOfKarachi(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        databaseManager.closeDatabase();

        return prayerTimesOfThisDay;
    }
}
