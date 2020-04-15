package com.example.abdul.prayertimings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.text.format.DateFormat;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.LocalDate;
import org.joda.time.chrono.IslamicChronology;
import org.joda.time.chrono.ISOChronology;

public class MainActivity extends AppCompatActivity {
	private TextView t15;
	private BroadcastReceiver broadcastReceiver;
	private boolean receiverIsRegistered;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.settings_screen, true);
		t15 = (TextView)findViewById(R.id.textView15);
		registerUIUpdater();
		
//		testNotification();
		//Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		//setSupportActionBar(toolbar);
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

/*
		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			public void run() {
				checkAndSetTimeWithDatabaseManager();
				handler.postDelayed(this, 1000);
			}
		};
		handler.postDelayed(runnable, 1000);
*/
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterUIUpdate();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setHijriDate();
		t15.setText("");
		t15.append(currentDate()+"-"+currentMonth()+"-"+currentYear());
		checkAndSetTimeWithDatabaseManager();
		clearNotifications();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (!sharedPreferences.getBoolean("firstTime", false)) {
			//----\  /---
			// ----\/---- run your one time code here
			checkAndSetNotifications();
			// mark first time has run.
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("firstTime", true);
			editor.apply(); //editor.commit();
		}
		registerUIUpdater();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterUIUpdate();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterUIUpdate();
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
		switch (item.getItemId()) {
			case R.id.settings:
				startActivity (new Intent(this, PreferenceClass.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void testNotification () {
		try {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
			Notification notification = builder
					.setContentTitle("Test Notification")
					.setContentText("Test Content")
					//.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
					//.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
					//.setDefaults(NotificationCompat.DEFAULT_SOUND)
					.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
					//.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
					.setSmallIcon(R.drawable.abc)
					//.setLargeIcon(bm)
					//.setLights(Color.RED, 0, 1)
					//.setContentIntent(pendingIntent)
					.setAutoCancel(false)
					//.setOngoing(true) //TO PREVENT A NOTIFICATION FROM BEING CLEARED.
					//.setWhen(setWhen)
					//.setShowWhen(false)
					.build();
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			//notificationManager.cancel(50);
			notificationManager.notify(50, notification);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	void clearNotifications() {
		for (int ID=0; ID<7; ID++) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(ID);
		}
	}
/*
	private void checkAndSetTime() {
		DBHelper mDBHelper = new DBHelper(this, Global.DB_NAME);

		if (mDBHelper.copyDataBase().equals("copied")) {
			Toast.makeText(this, "Database Copied", Toast.LENGTH_LONG).show();
			setTime (mDBHelper);
		} else if (mDBHelper.copyDataBase().equals("exists")) {
			setTime (mDBHelper);
		} else {
			Toast.makeText(this, "Error Copying Database!", Toast.LENGTH_LONG).show();
		}
	}
*/
	
	private void checkAndSetTimeWithDatabaseManager () {
		DatabaseManager.initializeInstance(new DBHelper(this, Global.DB_NAME));
		DatabaseManager.getInstance().copyDataBase(this, Global.DB_NAME);
		SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
		String[] time = DatabaseManager.getInstance().fetchTime (
				currentDate(), currentMonth(), database);
		setTimeWithDatabaseManager(time);
		//database.insert(...);
		// database.close(); Don't close it directly!
		DatabaseManager.getInstance().closeDatabase(); // correct way
	}
	
	boolean getNotificationFlag (Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notification_switch", false);
	}
	
	boolean getSilenceFlag (Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("silence_switch", false);
	}

	private void checkAndSetNotifications () {
		if (getNotificationFlag(this)) {
			try {
				cancelNotifications(this);
				setNotifications(this);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "Error Creating Notifications", Toast.LENGTH_LONG).show();
			}
		}
	}

	void setNotifications (Context context) throws Exception {
		DBHelper mDBHelper = new DBHelper (context, "Karachi.sqlite");
		String[] time = mDBHelper.fetchTime(currentDate(), currentMonth());
		for (int i=0; i<7; i++) {
			try {
				Date timeToSet = format24().parse(time[i]);
				Calendar calender = Calendar.getInstance();
				Date currentSystemTime = format24().parse(format24().format(calender.getTime()));
				
				if (timeToSet.after(currentSystemTime) /*|| timeToSet.equals(currentSystemTime)*/) {
					long delay = timeToSet.getTime() - currentSystemTime.getTime();
					makeNotifications (i, delay, context);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		mDBHelper.close();
	}

	private void registerUIUpdater() {
		
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setHijriDate();
				t15.setText("");
				t15.append(currentDate() +"-"+currentMonth()+"-"+currentYear());
				checkAndSetTimeWithDatabaseManager();
			}
		};
		registerReceiver (broadcastReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
		receiverIsRegistered=true;
	}
	
	private void unregisterUIUpdate() {
		if (receiverIsRegistered && broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		receiverIsRegistered=false;
	}
	
	private void makeNotifications (int ID, long delay, Context context) {
		Intent alertIntent = new Intent(context, NotificationPublisher.class);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		long alertTime = getCurrentTimeMillis() + delay;

		alertIntent.putExtra("ID", ID);
		alertIntent.putExtra("setWhen", alertTime);
		alertIntent = putStringExtra(ID, alertIntent);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast (context, ID, alertIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set (AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
	}

	void cancelNotifications(Context context) {
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		try {
			for (int i =0; i <7; i++) {
				PendingIntent pendingIntent = PendingIntent.getBroadcast (context, i,
						new Intent(context, NotificationPublisher.class),
						PendingIntent.FLAG_UPDATE_CURRENT);
				if (pendingIntent != null) {
					alarmManager.cancel (pendingIntent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/*
	void setTime (DBHelper mDBHelper) {
		String[] text_view = getResources().getStringArray(R.array.text_view_array);
		TextView view[] = new TextView[7];
		view[0] = (TextView) findViewById(R.id.textView);
		view[1] = (TextView) findViewById(R.id.textView2);
		view[2] = (TextView) findViewById(R.id.textView3);
		view[3] = (TextView) findViewById(R.id.textView4);
		view[4] = (TextView) findViewById(R.id.textView5);
		view[5] = (TextView) findViewById(R.id.textView6);
		view[6] = (TextView) findViewById(R.id.textView7);

		try {
			String time[] = mDBHelper.fetchTime(currentDate(), currentMonth());

			Calendar calender = Calendar.getInstance();
			try {
				Date currentSystemTime = format24().parse(format24().format(calender.getTime()));
				for (int i = 0; i < 7; i++) {
					Date timeToSet = format24().parse(time[i]);

					if ( DateFormat.is24HourFormat(this) ) {
						view[i].setText(text_view[i] +" "+ time[i]);
					} else {
						view[i].setText(text_view[i] +" "+ format12().format(timeToSet));
						//view[i].setText(format12().format(timeToSet));
					}

					view[i].setTextColor(t15.getTextColors());
					if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet)
							|| currentSystemTime.before(format24().parse(time[0]))) {

						view[i].setTextColor(Color.parseColor("#008000"));
						//view[i].getTextColors();
						if ( (i-1) > -1) {
							//view[i-1].setTextColor(Color.parseColor("#808080"));
							view[i-1].setTextColor(t15.getTextColors());
						} else {
							//view[6].setTextColor(Color.parseColor("#808080"));
							view[6].setTextColor(t15.getTextColors());
						}
					}
				}
				mDBHelper.close();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this,"Error Reading Database.",Toast.LENGTH_LONG).show();
		}
	}
*/

	void setTimeWithDatabaseManager (String[] time) {
		String[] text_view = getResources().getStringArray(R.array.text_view_array);
		TextView view[] = new TextView[7];
		view[0] = (TextView) findViewById(R.id.textView);
		view[1] = (TextView) findViewById(R.id.textView2);
		view[2] = (TextView) findViewById(R.id.textView3);
		view[3] = (TextView) findViewById(R.id.textView4);
		view[4] = (TextView) findViewById(R.id.textView5);
		view[5] = (TextView) findViewById(R.id.textView6);
		view[6] = (TextView) findViewById(R.id.textView7);

		try {
			Date currentSystemTime = format24().parse(
					format24().format(
							Calendar.getInstance().getTime()));
			for (int i = 0; i < 7; i++) {
				Date timeToSet = format24().parse(time[i]);

				if ( DateFormat.is24HourFormat(this) ) {
					view[i].setText(text_view[i] +" "+ time[i]);
				} else {
					view[i].setText(text_view[i] +" "+ format12().format(timeToSet));
				}

				view[i].setTextColor(t15.getTextColors());
				if (currentSystemTime.after(timeToSet) || currentSystemTime.equals(timeToSet)
						|| currentSystemTime.before(format24().parse(time[0]))) {

					view[i].setTextColor(Color.parseColor("#008000"));
					if ( (i-1) > -1) {
						view[i-1].setTextColor(t15.getTextColors());
					} else {
						view[6].setTextColor(t15.getTextColors());
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private Intent putStringExtra(int ID, Intent alertIntent) {
		switch (ID){
			case 0:
				return alertIntent.putExtra("MSG","Fajr time has started.");
			case 1:
				return alertIntent.putExtra("MSG","Fajr time has ended.");
			case 2:
				return alertIntent.putExtra("MSG","Zawal time.");
			case 3:
				return alertIntent.putExtra("MSG","Zuhur time has started.");
			case 4:
				return alertIntent.putExtra("MSG","Asr time has started.");
			case 5:
				return alertIntent.putExtra("MSG","Maghrib time has started.");
			case 6:
				return alertIntent.putExtra("MSG", "Isha time has started.");
			default:
				return alertIntent.putExtra("MSG", "---");
		}
	}
	
	void setToSilentMode (Context context) {
		SharedPreferences prefs = context.getSharedPreferences("silence_timings", 0);
		for (int i = 11; i < 16; i++) {
			try {
				if (prefs.getLong(Integer.toString(i), 0) == 0)
					continue;
				Date currentTime = Global.format24().parse(
						Global.format24().format(
								Calendar.getInstance().getTime()));
				Date silenceTime = Global.format24().parse(
						Global.formatThisTimeIn24(
								prefs.getLong(Integer.toString(i), 0)));
				
				if (currentTime.before(silenceTime)) {
					
					long silencerTime = getCurrentTimeMillis() + silenceTime.getTime() - currentTime.getTime();
					Intent intent1 = new Intent(context, MobileSilencer.class);
					intent1.putExtra("switchCase", "toSilent");
					
					PendingIntent pendingIntent = PendingIntent.getBroadcast (context, i, intent1,
							PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
					alarmManager.set(AlarmManager.RTC_WAKEUP, silencerTime, pendingIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void setHijriDate () {
		Chronology iso = ISOChronology.getInstanceUTC();
		Chronology hijri = IslamicChronology.getInstanceUTC();
		
		LocalDate todayIso = new LocalDate( Integer.parseInt(currentYear()),
				Integer.parseInt(currentMonth_MM()),
				Integer.parseInt(currentDate()), iso);
		LocalDate todayHijri = new LocalDate(todayIso.toDateTimeAtStartOfDay(),	hijri);
		
		TextView textView = (TextView)findViewById(R.id.textView8);
		textView.setText(todayHijri.toString("dd"));
		textView.append("-"+nameOfMonth(todayHijri.toString("MM"))+"-");
		textView.append(todayHijri.toString("yyyy"));
	}
	
	private String nameOfMonth (String id) {
		switch (Integer.parseInt(id)) {
			case 1: return "Muharram";
			case 2: return "Safar";
			case 3: return "Rabiul-Awwal";
			case 4: return "Rabi-uthani";
			case 5: return "Jumadi-ul-Awwal";
			case 6: return "Jumadi-uthani";
			case 7: return "Rajab";
			case 8: return "Sha’ban";
			case 9: return "Ramadan";
			case 10: return "Shawwal";
			case 11: return "Zhul-Q’ada";
			case 12: return "Zhul-Hijja";
			default: return " ";
		}
	}
	
	long getCurrentTimeMillis () { //removes extra seconds which cause delay.
		long remainder, currentTimeMillis;
		currentTimeMillis = System.currentTimeMillis();
		remainder = currentTimeMillis;
		remainder %= 60000;
		currentTimeMillis -= remainder;
		return currentTimeMillis;
	}
	
	static String currentDate () {
		return new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
	}
	
	static String currentMonth () {
		return new SimpleDateFormat("MMMM", Locale.getDefault()).format(new Date());
	}
	
	static String currentMonth_MM () {
		return new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());
	}

	static String currentYear () {
		return new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
	}
	
	static SimpleDateFormat format24() {
		return new SimpleDateFormat("HH:mm", Locale.getDefault());
	}

	static SimpleDateFormat format12() {
		return new SimpleDateFormat("hh:mm a", Locale.getDefault());
	}
	
	static String formatThisTime (Context context, Long time) {
		if (time == 0) {
			return "Have not set.";
		} else {
			SimpleDateFormat simpleDateFormat;
			if (DateFormat.is24HourFormat(context)) {
				simpleDateFormat = format24();
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				return simpleDateFormat.format(new Date(time));
			} else {
				simpleDateFormat = format12();
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				return simpleDateFormat.format(new Date(time));
			}
		}
	}

	static String formatThisTimeIn24 (Long time) {
		SimpleDateFormat simpleDateFormat = format24();
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return simpleDateFormat.format(new Date(time));
	}
	
/*
	static String formatThisDateTime (Long time) {
		SimpleDateFormat simpleDateFormat;
		simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm", Locale.getDefault());
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return simpleDateFormat.format(new Date(time));
	}
*/
	
	//Toast.makeText(this," MAIN's Handler completed..",Toast.LENGTH_LONG).show();

/*		Timer timer = new Timer();
*//*		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {*//*
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
			}
		}, 1);
		timer.cancel();*/

	/*		if (timeToSet.after(currentSystemTime)) {
			long diffInMillies = timeToSet.getTime() - currentSystemTime.getTime();
			//makeNotification(diffInMillies, i);
		}*/

	//long diffMinutes = diffInMillies / (60 * 1000);
	//t15.setText(Long.toString(diffMinutes));
			/*
			long min2 = SystemClock.elapsedRealtime() + (long)(120*1000);
			min2 += (long)(120*1000);
			long delay = 120*1000;
			scheduleNotification(getNotification("Isha time has Started", min2), delay, 1);
			*/
		/*
	private void scheduleNotification (Notification notification, long delay, int id) {

		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent = new Intent(this, NotificationPublisher.class);
		notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
		notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CONTENT, notification);

		notificationIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		long futureInMillis = SystemClock.elapsedRealtime() + delay;
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent2);
	}
*/
	/*
	private Notification getNotification(String content, long setWhenTime) {

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("Scheduled Notification");
		builder.setContentText(content);
		builder.setSmallIcon(R.drawable.abc);
		builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
		//builder.setWhen(setWhenTime);
		builder.setShowWhen(true);

		return builder.build();
	}
*/
	//START OF  TRY 2
	//long alertTime = SystemClock.elapsedRealtime()+3*60*1000;
/*
		long alertTime = System.currentTimeMillis() + 3*60*1000;
		Intent alertIntent = new Intent(this,
				NotificationPublisher.class);
		AlarmManager alarmManager = (AlarmManager)
				getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
*/
	//END OF TRY 2
		/*try {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pend = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notify = new NotificationCompat
					.Builder(getApplicationContext())
					.setContentTitle("11111")
					.setContentText("2222")
					.setSmallIcon(R.drawable.abc)
					.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
					.setContentIntent(pend)
					.setWhen(SystemClock.elapsedRealtime() + (long)(120*1000))
					.build();
			notify.flags |= Notification.FLAG_AUTO_CANCEL;
			notif.notify(0, notify);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
				/*
		Handler handler = new Handler(this.getMainLooper());
		Runnable runnable = new Runnable() {
			@Override
			public void run () {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						checkNsetTime();

					}
				});
			}
		};
		new Thread(runnable).start();
		*/
}
