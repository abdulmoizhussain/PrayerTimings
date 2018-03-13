package com.example.abdul.prayertimings;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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
	private final String databaseName;
	private final String databasePath;
	private final Context myContext;
	private SQLiteDatabase myDataBase;

	DBHelper(Context context, String DB_NAME) {
		super (context, DB_NAME, null, 1);
		this.databaseName = DB_NAME;
		this.myContext = context;

		if(android.os.Build.VERSION.SDK_INT >= 17) {
			this.databasePath = context.getApplicationInfo().dataDir + "/databases/";
		}
		else {
			ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
			this.databasePath = cw.getFilesDir().getParent()+ "/databases/";
		}
	}

	private void openDataBase() throws SQLException {
		String myPath = this.databasePath + this.databaseName;
		if (this.myDataBase != null && this.myDataBase.isOpen()) {
			return;
		}
		this.myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
	}

	private void closeDatabase () {
		if (this.myDataBase != null) {
			this.myDataBase.close();
		}
	}

	String copyDataBase() {
		try {
			File database = this.myContext.getApplicationContext().getDatabasePath(this.databaseName);
			if (!database.exists()) {
				this.getReadableDatabase();
				try {
					int length;
					byte[] buffer = new byte[1024];
					OutputStream myOutput = new FileOutputStream(this.databasePath + this.databaseName);
					InputStream myInput = this.myContext.getAssets().open(this.databaseName);
					while ((length = myInput.read(buffer)) > 0) {
						myOutput.write(buffer, 0, length);
					}
					myOutput.flush();
					myOutput.close();
					myInput.close();
					return "copied";
				} catch (IOException e) {
					e.printStackTrace();
					return "!copied";
				}
			} else if (database.exists()) {
				return "exists";
			}
			else
				return "!exist";
		} catch (Exception e) {
			e.printStackTrace();
			return "!copied";
		}
	}
	
	String[] fetchTime (String date, String month) throws Exception {
		String temp[] = new String[7];
		try {
			openDataBase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//Cursor cursor = this.myDataBase.query (month, null, date, null, null, null, null); //not working in order
		Cursor cursor = this.myDataBase.rawQuery("select * from " +month+ " where Date=" +date+ ";", null);
		cursor.moveToFirst();
		for (int i=0; i<7; i++) {
			temp[i] = cursor.getString(i+1);
		}
		//temp[7] = Integer.toString(cursor1.getCount());
		cursor.close();
		closeDatabase();
		return temp;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}