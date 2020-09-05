package com.example.abdul.prayertimings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Abdul on 6/6/2017.
 * This is an extra line to remove warning.
 */

public final class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "prayer_timings.db";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TableNames.PrayerTime + " (" +
                ColumnNames.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ColumnNames.City + " TEXT," +
                ColumnNames.Month + " INTEGER," +
                ColumnNames.Date + " INTEGER," +
                ColumnNames.Fajr + " TEXT," +
                ColumnNames.Sunrise + " TEXT," +
                ColumnNames.Zawal + " TEXT," +
                ColumnNames.Zuhur + " TEXT," +
                ColumnNames.Asr + " TEXT," +
                ColumnNames.Maghrib + " TEXT," +
                ColumnNames.Isha + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TableNames.PrayerTime);
        onCreate(db);
    }

    public static final class ColumnNames {
        public static final String ID = "ID";
        public static final String City = "City";
        public static final String Month = "Month";
        public static final String Date = "Date";
        public static final String Fajr = "Fajr";
        public static final String Sunrise = "Sunrise";
        public static final String Zawal = "Zawal";
        public static final String Zuhur = "Zuhur";
        public static final String Asr = "Asr";
        public static final String Maghrib = "Maghrib";
        public static final String Isha = "Isha";
    }

    public static final class TableNames {
        public static final String PrayerTime = "PrayerTime";
    }
}