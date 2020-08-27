package com.example.abdul.prayertimings;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Abdul on 6/6/2017.
 * This is an extra line to remove warning.
 */

public class DBHelper extends SQLiteOpenHelper {
    private final String databaseName, databasePath;
    private final Context context;
    private SQLiteDatabase sqLiteDatabase;

    public DBHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        this.context = context;
        this.databaseName = databaseName;

        if (android.os.Build.VERSION.SDK_INT >= 17) {
            databasePath = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
            databasePath = contextWrapper.getFilesDir().getParent() + "/databases/";
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void openDataBase() throws SQLiteException {
        String completeDatabasePath = databasePath + databaseName;
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            return;
        }
        sqLiteDatabase = SQLiteDatabase.openDatabase(completeDatabasePath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void closeDatabase() {
        if (sqLiteDatabase != null) {
            sqLiteDatabase.close();
        }
    }

    public String copyDataBase() {
        try {
            File database = context.getApplicationContext().getDatabasePath(databaseName);
            if (!database.exists()) {
                this.getReadableDatabase();
                try {
                    int length;
                    byte[] byteArrayBuffer = new byte[1024];
                    OutputStream outputStream = new FileOutputStream(databasePath + databaseName);
                    InputStream inputStream = context.getAssets().open(databaseName);
                    while ((length = inputStream.read(byteArrayBuffer)) > 0) {
                        outputStream.write(byteArrayBuffer, 0, length);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                    return "copied";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "!copied";
                }
            } else if (database.exists()) {
                return "exists";
            } else
                return "!exist";
        } catch (Exception e) {
            e.printStackTrace();
            return "!copied";
        }
    }

    public String[] fetchTime(String date, String month) {
        String[] timeStrings = new String[7];
        openDataBase();
        //Cursor cursor = sqLiteDatabase.query(month, null, date, null, null, null, null); // not working in order
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + month + " where Date=" + date + ";", null);
        cursor.moveToFirst();
        for (int index = 0; index < 7; index++) {
            timeStrings[index] = cursor.getString(index + 1);
        }
        cursor.close();
        closeDatabase();
        return timeStrings;
    }
}