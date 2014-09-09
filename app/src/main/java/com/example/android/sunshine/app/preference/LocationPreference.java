package com.example.android.sunshine.app.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.android.sunshine.app.R;

/**
 * Created by lee on 9/9/14.
 */
public class LocationPreference extends Preference {

    private Context mContext;
    private SearchView mSearchView;

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
        EditText editText = (EditText)view.findViewById(R.id.location_edit_view);
        String str = editText.getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        setSummary(str);
    }
}
