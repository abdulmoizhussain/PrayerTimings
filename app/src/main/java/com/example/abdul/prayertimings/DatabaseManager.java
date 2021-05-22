package com.example.abdul.prayertimings;

//https://stackoverflow.com/questions/28489238/access-sqlite-database-simultaneously-from-different-threads/28489506

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Abdul on 8/23/2017.
 * an extra line to remove warning.
 */

public class DatabaseManager {
    private final AtomicInteger atomicInteger = new AtomicInteger();
    private static DatabaseManager databaseManager;
    private static SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase sqLiteDatabase;

    public static synchronized DatabaseManager getInstance(Context context) {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            sqLiteOpenHelper = new DatabaseHelper(context);
        }
        return databaseManager;
    }

    public final synchronized void openDatabase() {
        if (atomicInteger.incrementAndGet() == 1) {
            sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
        }
    }

    public final synchronized void closeDatabase() {
        if (atomicInteger.decrementAndGet() == 0) {
            sqLiteDatabase.close();
        }
    }

    public synchronized boolean databaseIsEmpty() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT EXISTS(SELECT * FROM " + DatabaseHelper.TableNames.PrayerTime + " WHERE ID=1)", null);
        int result = 0;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result == 0;
    }

    public synchronized void insertPrayerTimes(JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        sqLiteDatabase.beginTransaction();

        for (int index = 0; index < length; index++) {
            JSONObject prayerTime = jsonArray.getJSONObject(index);
            ContentValues contentValues = new ContentValues();

            contentValues.put(DatabaseHelper.ColumnNames.City, prayerTime.getString("City"));
            contentValues.put(DatabaseHelper.ColumnNames.Month, prayerTime.getInt("Month"));
            contentValues.put(DatabaseHelper.ColumnNames.Date, prayerTime.getInt("Date"));
            contentValues.put(DatabaseHelper.ColumnNames.Fajr, prayerTime.getString("Fajr"));
            contentValues.put(DatabaseHelper.ColumnNames.Sunrise, prayerTime.getString("Sunrise"));
            contentValues.put(DatabaseHelper.ColumnNames.Zawal, prayerTime.getString("Zawal"));
            contentValues.put(DatabaseHelper.ColumnNames.Zuhur, prayerTime.getString("Zuhur"));
            contentValues.put(DatabaseHelper.ColumnNames.Asr, prayerTime.getString("Asr"));
            contentValues.put(DatabaseHelper.ColumnNames.Maghrib, prayerTime.getString("Maghrib"));
            contentValues.put(DatabaseHelper.ColumnNames.Isha, prayerTime.getString("Isha"));

            sqLiteDatabase.insert(DatabaseHelper.TableNames.PrayerTime, null, contentValues);
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    public synchronized boolean failsToInsertInitialDataIntoDatabase(Context context) {
        String jsonString;
        try {
            InputStream inputStream = context.getAssets().open("Karachi.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null)
                stringBuilder.append(readLine);
            inputStream.close();
            jsonString = stringBuilder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "Unable to read initial data file from assets.", Toast.LENGTH_LONG).show();
            return true;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            insertPrayerTimes(jsonArray);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "Unable to parse initial-data-file from assets.", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    public synchronized final String[] fetchPrayerTimeOfADayOfKarachi(int month, int date) {
        final int totalColumns = 7;
        final String[] timeStrings = new String[totalColumns];
        final Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT " +
                        DatabaseHelper.ColumnNames.Fajr + ", " +
                        DatabaseHelper.ColumnNames.Sunrise + ", " +
                        DatabaseHelper.ColumnNames.Zawal + ", " +
                        DatabaseHelper.ColumnNames.Zuhur + ", " +
                        DatabaseHelper.ColumnNames.Asr + ", " +
                        DatabaseHelper.ColumnNames.Maghrib + ", " +
                        DatabaseHelper.ColumnNames.Isha +
                        " FROM " + DatabaseHelper.TableNames.PrayerTime +
                        " WHERE " +
                        DatabaseHelper.ColumnNames.City + "='Karachi'" +
                        " AND " +
                        DatabaseHelper.ColumnNames.Month + "=" + month +
                        " AND " + DatabaseHelper.ColumnNames.Date + "=" + date,
                null
        );
        if (cursor.moveToFirst()) {
            for (int index = 0; index < totalColumns; index++) {
                timeStrings[index] = cursor.getString(index);
            }
        }
        cursor.close();
        return timeStrings;
    }
}