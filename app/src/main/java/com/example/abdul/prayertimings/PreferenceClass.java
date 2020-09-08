package com.example.abdul.prayertimings;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

public class PreferenceClass extends PreferenceActivity implements OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private int id, minute, hour;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    Preference[] preferences = new Preference[5];
    ListPreference listPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_screen);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        listPreference = (ListPreference) findPreference("silence_duration");
        sharedPreferences = this.getSharedPreferences("silence_timings", Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.apply();
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences prefs = this.getSharedPreferences("silence_switch", Context.MODE_PRIVATE);
        //prefs.getBoolean("language", true);
        //sharedPreferences.getBoolean("silence_switch", true);
        preferences[0] = findPreference("fajr");
        preferences[1] = findPreference("zuhur");
        preferences[2] = findPreference("asr");
        preferences[3] = findPreference("maghrib");
        preferences[4] = findPreference("isha");
        for (int index = 0; index < 5; index++) {
            preferences[index].setOnPreferenceClickListener(this);
        }
        checkAndSetSilenceSwitch();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "fajr":
                showDialog(11);
                break;
            case "zuhur":
                showDialog(12);
                break;
            case "asr":
                showDialog(13);
                break;
            case "maghrib":
                showDialog(14);
                break;
            case "isha":
                showDialog(15);
                break;
            default:
                Toast.makeText(this, "default", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "notification_switch":
                Global.cancelAllScheduledNotificationsOfThisDay(this);
                if (sharedPreferences.getBoolean(key, false)) {
                    Global.scheduleNotificationsOfAllPrayerTimesForThisDay(this);
                }
                break;
            case "silence_switch":
                checkAndSetSilenceSwitch();
                break;

            case "silence_duration":
                sharedPreferencesEditor.putInt(key, Integer.parseInt(listPreference.getValue()));
                sharedPreferencesEditor.apply();
                listPreference.setSummary(listPreference.getEntry());
                Global.setToSilentMode(PreferenceClass.this);
        }
    }

    private void checkAndSetSilenceSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Preference silence_duration = getPreferenceManager().findPreference("silence_duration");
        Preference silence_category = getPreferenceManager().findPreference("silence_category");
        if (sharedPreferences.getBoolean("silence_switch", false)) {
            silence_duration.setEnabled(true);
            silence_category.setEnabled(true);
        } else {
            silence_duration.setEnabled(false);
            silence_category.setEnabled(false);
        }

        listPreference.setSummary(this.sharedPreferences.getInt("silence_duration", 10) + " minutes");
        for (int index = 0; index < 5; index++) {
            preferences[index].setSummary(Global.formatThisTime(this, loadLong(index + 11)));
        }
    }

    Dialog timePickerDialog() {
        return new TimePickerDialog(PreferenceClass.this, onTimePickerListener, hour, minute, DateFormat.is24HourFormat(this));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        this.id = id;
        return timePickerDialog();
    }

    protected TimePickerDialog.OnTimeSetListener onTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
            minute = minuteOfDay;
            hour = hourOfDay;
            saveLong(id);
            preferences[id - 11].setSummary(Global.formatThisTime(PreferenceClass.this, loadLong(id)));
            Global.setToSilentMode(PreferenceClass.this);
        }
    };

    private void saveLong(int longID) {
        minute = minute * 60 * 1000;
        hour = hour * 60 * 60 * 1000;
        //SharedPreferences prefs = PreferenceClass.this.getSharedPreferences("silence_timings", 0);
        //SharedPreferences.Editor editor = prefs.edit();
        sharedPreferencesEditor.putLong(Integer.toString(longID), (minute + hour));
		/*editor.putInt(longName+"_size", longArray.length);
		for(int i=0; i<longArray.length; i++)
			editor.putLong (longName +"_"+i, longArray[i]);*/
        sharedPreferencesEditor.apply();
    }

    private Long loadLong(int longID) {
        //SharedPreferences prefs = PreferenceClass.this.getSharedPreferences("silence_timings", 0);
        //prefs.getLong (longName, 0);
		/*int size = prefs.getInt(longName+ "_size", 0);
		String array[] = new String[size];
		for(int i=0;i<size;i++)
			array[i] = prefs.getString(longName+ "_" + i, null);*/
        return sharedPreferences.getLong(Integer.toString(longID), 0);
    }
}