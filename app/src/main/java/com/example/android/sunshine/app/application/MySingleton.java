package com.example.android.sunshine.app.application;

/**
 * Created by chuanl on 9/9/14.
 */
public class MySingleton {

    private static MySingleton instance;

    private boolean isDataLoaded;
    private boolean isFirstRun;
    private boolean isLocationChanged;
    private boolean isLocationSelectionDataLoaded;

    public static void initInstance(){

        if(instance == null){
            instance = new MySingleton();
        }
    }

    public static MySingleton getInstance(){
        return instance;
    }

    private MySingleton(){
        isDataLoaded = false;
        isFirstRun = true;
        isLocationChanged = false;
        isLocationSelectionDataLoaded = false;
    }

    public boolean isFirstRun() {
        return isFirstRun;
    }

    public void setFirstRun(boolean isFirstRun) {
        this.isFirstRun = isFirstRun;
    }

    public boolean isLocationChanged() {
        return isLocationChanged;
    }

    public void setLocationChanged(boolean isLocationChanged) {
        this.isLocationChanged = isLocationChanged;
    }

    public boolean isLocationSelectionDataLoaded() {
        return isLocationSelectionDataLoaded;
    }

    public void setLocationSelectionDataLoaded(boolean isLocationSelectionDataLoaded) {
        this.isLocationSelectionDataLoaded = isLocationSelectionDataLoaded;
    }
}
