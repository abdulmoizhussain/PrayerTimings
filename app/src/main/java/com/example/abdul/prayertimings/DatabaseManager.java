 package com.example.abdul.prayertimings;

//https://stackoverflow.com/questions/28489238/access-sqlite-database-simultaneously-from-different-threads/28489506
//

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
import java.util.concurrent.atomic.AtomicInteger;

 /**
 * Created by Abdul on 8/23/2017.
 * an extra line to remove warning.
 */

class DatabaseManager {
/*
	static int mOpenCounter;
	Context myContext;
	static DatabaseManager instance;
	static String databasePath,databaseName;
	//private static SQLiteOpenHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;
	
	private SQLiteDatabase myDataBase;
	static SQLiteOpenHelper mDatabaseHelper;
	
	public static synchronized void initializeInstance (SQLiteOpenHelper helper) {
		if (instance == null) {
			instance = new DatabaseManager();
			mDatabaseHelper = helper;
		}
	}
	
	static synchronized boolean getInstance() {
		if (instance == null) {
			throw new IllegalStateException (DatabaseManager.class.getSimpleName() +
					" is not initialized, call initializeInstance(..) method first.");
		}
		return true;
	}
*/
/*
	public synchronized SQLiteDatabase openDatabase() {
		mOpenCounter++;
		if (mOpenCounter == 1) {
			// Opening new database
			if (this.myDataBase != null && this.myDataBase.isOpen()) {
				return mDatabase;
			}
			this.myDataBase = SQLiteDatabase.openDatabase(this.databasePath+this.databaseName,
					null, SQLiteDatabase.OPEN_READWRITE);
		}
		return mDatabase;
	}
*//*

	static synchronized SQLiteDatabase openDatabase() {
		mOpenCounter++;
		if (mOpenCounter == 1) {
			// Opening new database
			if (this.myDataBase != null && this.myDataBase.isOpen()) {
				return this.myDataBase;
			}
			this.myDataBase = SQLiteDatabase.openDatabase(this.databasePath+this.databaseName,
					null, SQLiteDatabase.OPEN_READWRITE);
		}
		return this.myDataBase;
	}
	
	synchronized void closeDatabase () {
		mOpenCounter--;
		if (mOpenCounter == 0) {
*/
/*
			if (this.myDataBase != null) {
				this.myDataBase.close();
			}
*//*

			mDatabase.close(); // Closing database
		}
	}
*/

/*
	private void openDataBase() throws SQLException {
		if (this.myDataBase != null && this.myDataBase.isOpen()) {
			return;
		}
		this.myDataBase = SQLiteDatabase.openDatabase(this.databasePath + this.databaseName,
				null, SQLiteDatabase.OPEN_READWRITE);
	}
*/

/*
	SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
	database.insert(...);
	// database.close(); Don't close it directly!
	DatabaseManager.getInstance().closeDatabase(); // correct way
*/

	private AtomicInteger mOpenCounter = new AtomicInteger();

	private static DatabaseManager instance;
	private static SQLiteOpenHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	public static synchronized void initializeInstance (SQLiteOpenHelper helper) {
		if (instance == null) {
			instance = new DatabaseManager();
			mDatabaseHelper = helper;
		}
	}

	public static synchronized DatabaseManager getInstance() {
		if (instance == null) {
			throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
					" is not initialized, call initializeInstance(..) method first.");
		}
		return instance;
	}

	public synchronized SQLiteDatabase openDatabase() {
		if(mOpenCounter.incrementAndGet() == 1) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		return mDatabase;
	}

	public synchronized void closeDatabase() {
		if(mOpenCounter.decrementAndGet() == 0) {
			mDatabase.close();
		}
	}
	 
	String copyDataBase (Context context, String databaseName) {
		String databasePath;
		if(android.os.Build.VERSION.SDK_INT >= 17) {
			databasePath = context.getApplicationInfo().dataDir + "/databases/";
		}
		else {
			ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
			databasePath = cw.getFilesDir().getParent()+ "/databases/";
		}
		
		try {
			File database = context.getApplicationContext()
					 .getDatabasePath(databaseName);
			if (!database.exists()) {
				mDatabaseHelper.getReadableDatabase();
				try {
					int length;
					byte[] buffer = new byte[1024];
					OutputStream myOutput = new FileOutputStream(
							databasePath + databaseName);
					 InputStream myInput = context.getAssets()
							 .open(databaseName);
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
	 String[] fetchTime (String date, String month, SQLiteDatabase mDatabase) {
		 String temp[] = new String[7];
		 Cursor cursor = mDatabase.rawQuery("select * from "+month+ " where Date=" +date+ ";", null);
		 cursor.moveToFirst();
		 for (int i=0; i<7; i++) {
			 temp[i] = cursor.getString(i+1);
		 }
		 cursor.close();
		 return temp;
	 }
	
	
 }