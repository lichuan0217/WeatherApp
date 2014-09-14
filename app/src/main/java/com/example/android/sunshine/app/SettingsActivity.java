/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.app.application.MySingleton;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.preference.LocationPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    public static final int SELECT_LOCATION_REQUEST = 0;

    // since we use the preference change initially to populate the summary
    // field, we'll ignore that change at start of the activity
    boolean mBindingPreference;

    // Used to store the previous location
    String location_previous;
    LocationPreference locationPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        location_previous = Utility.getPreferredLocation(this);

        locationPreference = (LocationPreference)findPreference("location_select");
        locationPreference.setActivity(this);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));

        // Load location data from raw file
        MySingleton instance = MySingleton.getInstance();
        if(!instance.isLocationSelectionDataLoaded()){
            ContentResolver cr = getContentResolver();
            InputStream inputStream = getResources().openRawResource(R.raw.location_map);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String cityName = null;
            ContentValues cv = null;
            try {
                while((cityName = reader.readLine()) != null){
                    cv = new ContentValues();
                    cv.put(WeatherContract.LocationSelectionEntry.COLUMN_CITY_NAME, cityName);
                    cr.insert(WeatherContract.LocationSelectionEntry.CONTENT_URI,cv);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            instance.setLocationSelectionDataLoaded(true);
        }
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        mBindingPreference = true;

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

        mBindingPreference = false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        // are we starting the preference activity?
        if (!mBindingPreference) {
            if (preference.getKey().equals(getString(R.string.pref_location_key))) {
//                FetchWeatherTask weatherTask = new FetchWeatherTask(this);
//                String location = value.toString();
//                weatherTask.execute(location);
                if (!location_previous.equalsIgnoreCase(value.toString())) {
                    MySingleton instance = MySingleton.getInstance();
                    instance.setLocationChanged(true);
                }
            } else {
                // notify code that weather may be impacted
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            }
        }


        if (preference instanceof ListPreference) {
            Log.d(LOG_TAG, "ListPreference");
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == SELECT_LOCATION_REQUEST){
            String location = data.getStringExtra(LocationSelectActivity.LOCATION_SELECTED);
            locationPreference.setSummary(location);
        }
    }
}