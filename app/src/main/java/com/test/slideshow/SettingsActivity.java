package com.test.slideshow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.test.slideshow.receivers.AlarmReceiver;
import com.test.slideshow.utilities.preferences.IntEditTextPreference;

/**
 * Created by Nikita on 17.10.2014.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int mLastIntervalValue = -1;
    private boolean mStorageTypeChanged = false;
    private final static String INTERVAL_KEY = "com.test.slideshow.intervalkey";
    private final static String STORAGE_KEY = "com.test.slideshow.storagekey";

    public final static String INTERVAL_CHANGE_KEY = "com.test.slideshow.intervalchangekey";
    public final static String STORAGE_CHANGE_KEY = "com.test.slideshow.storagechangekey";

    SharedPreferences mPrefs;
    IntEditTextPreference mEditPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mLastIntervalValue = savedInstanceState.getInt(INTERVAL_KEY);
            mStorageTypeChanged = savedInstanceState.getInt(STORAGE_KEY) == 1 ? true : false;
        }

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSaveInstanceState(Bundle out){
        out.putInt(INTERVAL_KEY, mLastIntervalValue);
        out.putInt(STORAGE_KEY, mStorageTypeChanged ? 1 : 0);
        super.onSaveInstanceState(out);
    }

    @Override
    public void onPause(){
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        mEditPref.setOnPreferenceChangeListener(null);
    }

    @Override
    public void onResume(){
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        if (mEditPref == null)
            mEditPref = (IntEditTextPreference) getPreferenceScreen().findPreference(getString(R.string.interval_key));

        mEditPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String value = (String) o;
                if (value == null) return false;
                int intVal = Integer.valueOf(value);
                if (intVal < 1 || intVal > 60) {
                    Toast.makeText(MyApplication.getContext(), "Значение должно быть 1..60", Toast.LENGTH_SHORT).show();
                    mEditPref.setText("5");
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.interval_key))){
            mLastIntervalValue = sharedPreferences.getInt(s,mLastIntervalValue);
        }
        else if (s.equals(getString(R.string.ext_dir_key)))
            mStorageTypeChanged = !mStorageTypeChanged;


        else if (s.equals(getString(R.string.start_time_key))){
            AlarmReceiver.cancelAlarm(SettingsActivity.this, true);
            AlarmReceiver.setAlarmBegin(SettingsActivity.this);
        }
        else if (s.equals(getString(R.string.end_time_key))){
            AlarmReceiver.cancelAlarm(SettingsActivity.this, false);
            AlarmReceiver.setAlarmEnd(SettingsActivity.this);
        }
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        if (mLastIntervalValue != -1)
            returnIntent.putExtra(INTERVAL_CHANGE_KEY,mLastIntervalValue);
        if (mStorageTypeChanged)
            returnIntent.putExtra(STORAGE_CHANGE_KEY, mStorageTypeChanged);
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}