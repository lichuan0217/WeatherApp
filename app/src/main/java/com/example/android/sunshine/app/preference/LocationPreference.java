package com.example.android.sunshine.app.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.sunshine.app.LocationSelectActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.SettingsActivity;

/**
 * Created by lee on 9/9/14.
 */
public class LocationPreference extends Preference {

    private static final String LOG_TAG = LocationPreference.class.getSimpleName();

    private Context mContext;
    private PreferenceActivity parent;

    public LocationPreference(Context context) {
        this(context, null, 0);
    }

    public LocationPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public LocationPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater inflater =
                (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.location_preference, parent, false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
    }

    @Override
    protected void onClick() {
        Intent intent = new Intent(parent, LocationSelectActivity.class);
        parent.startActivityForResult(intent, SettingsActivity.SELECT_LOCATION_REQUEST);
    }

    public void setActivity(PreferenceActivity parent){
        this.parent = parent;
    }

}
