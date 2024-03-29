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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;
import me.imid.swipebacklayout.lib.app.SwipeBackPreferenceActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends SwipeBackPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    public static final int SELECT_LOCATION_REQUEST = 0;

    // Used to Load the location name from raw file
    private static final String PRE_LOCATION_KEY = "LOCATION_LOAD_KEY";
    private SharedPreferences sharedPreferences;

    // since we use the preference change initially to populate the summary
    // field, we'll ignore that change at start of the activity
    boolean mBindingPreference;

    // Used to store the previous location
    String location_previous;
    LocationPreference locationPreference;

    private SwipeBackLayout swipeBackLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        location_previous = Utility.getPreferredLocation(this);

        locationPreference = (LocationPreference) findPreference(
                getString(R.string.pref_location_select_key));
        locationPreference.setActivity(this);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
        bindPreferenceSummaryToValue(locationPreference);

        // Load location data from raw file
        if (!isLocationLoaded()) {
            ContentResolver cr = getContentResolver();
            InputStream inputStream = getResources().openRawResource(R.raw.location_map);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String cityName = null;
            ContentValues cv = null;
            try {
                while ((cityName = reader.readLine()) != null) {
                    cv = new ContentValues();
                    cv.put(WeatherContract.LocationSelectionEntry.COLUMN_CITY_NAME, cityName);
                    cr.insert(WeatherContract.LocationSelectionEntry.CONTENT_URI, cv);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            setLocationLoaded();
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
        Log.d(LOG_TAG, "onPreferenceChange");
        String stringValue = value.toString();

        // are we starting the preference activity?
        if (!mBindingPreference) {
            if (preference.getKey().equals(getString(R.string.pref_location_key))) {
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
        if (resultCode == RESULT_OK && requestCode == SELECT_LOCATION_REQUEST) {
            String location = data.getStringExtra(LocationSelectActivity.LOCATION_SELECTED);
//            locationPreference.setSummary(location);

            SharedPreferences.Editor editor = locationPreference.getEditor();
            editor.putString(getString(R.string.pref_location_select_key), location);
            editor.commit();
            onPreferenceChange(locationPreference, location);
            Log.d(LOG_TAG, sharedPreferences.getString("location_select", "foo"));
        }
    }

    // Check the preference whether the location is loaded
    private boolean isLocationLoaded() {
        int defaultValue = 0;
        int value = sharedPreferences.getInt(PRE_LOCATION_KEY, defaultValue);
        if (value == defaultValue)
            return false;
        return true;
    }

    // Set the preference
    private void setLocationLoaded() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PRE_LOCATION_KEY, 1);
        editor.commit();
    }

}