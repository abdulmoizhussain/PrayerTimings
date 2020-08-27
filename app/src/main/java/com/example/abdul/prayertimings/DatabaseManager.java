package com.example.abdul.prayertimings;

//https://stackoverflow.com/questions/28489238/access-sqlite-database-simultaneously-from-different-threads/28489506

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Abdul on 8/23/2017.
 * an extra line to remove warning.
 */

public class DatabaseManager {
    private AtomicInteger atomicInteger = new AtomicInteger();
    private static DatabaseManager databaseManager;
    private static SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase sqLiteDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper sqLiteOpenHelper) {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            DatabaseManager.sqLiteOpenHelper = sqLiteOpenHelper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (databaseManager == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() + " is not initialized, call initializeInstance(..) method first.");
        }
        return databaseManager;
    }

    public synchronized void openDatabase() {
        if (atomicInteger.incrementAndGet() == 1) {
            sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
        }
    }

    public synchronized void closeDatabase() {
        if (atomicInteger.decrementAndGet() == 0) {
            sqLiteDatabase.close();
        }
    }

    public void copyDataBase(Context context, String databaseName) {
        String databasePath;
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            databasePath = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
            databasePath = contextWrapper.getFilesDir().getParent() + "/databases/";
        }

        File databaseFile = context.getApplicationContext().getDatabasePath(databaseName);
        if (!databaseFile.exists()) {
            sqLiteOpenHelper.getReadableDatabase();
            try {
                int length;
                byte[] buffer = new byte[1024];
                OutputStream outputStream = new FileOutputStream(databasePath + databaseName);
                InputStream inputStream = context.getAssets().open(databaseName);
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] fetchTime(String date, String month) {
        String[] timeStrings = new String[7];
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + month + " where Date=" + date + ";", null);
        cursor.moveToFirst();
        for (int index = 0; index < 7; index++) {
            timeStrings[index] = cursor.getString(index + 1);
        }
        cursor.close();
        return timeStrings;
    }
}