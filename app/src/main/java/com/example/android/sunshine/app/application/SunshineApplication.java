package com.example.android.sunshine.app.application;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by chuanl on 9/9/14.
 */
public class SunshineApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        initSingletons();
    }

    private void initSingletons(){
        MySingleton.initInstance();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
