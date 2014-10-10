package com.example.android.sunshine.app.http;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Vector;

/**
 * Created by chuanl on 10/10/14.
 */
public class FetchWeather {

    private static final String TAG = FetchWeather.class.getSimpleName();

    private final Context mContext;
    private final SwipeRefreshLayout refreshLayout;

    public FetchWeather(Context context, SwipeRefreshLayout layout){
        mContext = context;
        refreshLayout = layout;
    }



    public void fetchWeather(final String location) {
        WeatherRequestClient.get(location, null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject forecastJson) {
//                super.onSuccess(statusCode, headers, forecastJson);

                // These are the names of the JSON objects that need to be extracted.

                // Location information
                final String OWM_CITY = "city";
                final String OWM_CITY_NAME = "name";
                final String OWM_COORD = "coord";
                final String OWM_COORD_LAT = "lat";
                final String OWM_COORD_LONG = "lon";

                // Weather information.  Each day's forecast info is an element of the "list" array.
                final String OWM_LIST = "list";

                final String OWM_DATETIME = "dt";
                final String OWM_PRESSURE = "pressure";
                final String OWM_HUMIDITY = "humidity";
                final String OWM_WINDSPEED = "speed";
                final String OWM_WIND_DIRECTION = "deg";

                // All temperatures are children of the "temp" object.
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";

                final String OWM_WEATHER = "weather";
                final String OWM_DESCRIPTION = "main";
                final String OWM_WEATHER_ID = "id";

                try {
                    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
                    JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
                    String cityName = cityJson.getString(OWM_CITY_NAME);
                    JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
                    double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
                    double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

                    // Insert the location into the database.
                    long locationID = addLocation(location, cityName, cityLatitude, cityLongitude);

                    // Get and insert the new weather information into the database
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

                    for(int i = 0; i < weatherArray.length(); i++) {
                        // These are the values that will be collected.

                        long dateTime;
                        double pressure;
                        int humidity;
                        double windSpeed;
                        double windDirection;

                        double high;
                        double low;

                        String description;
                        int weatherId;

                        // Get the JSON object representing the day
                        JSONObject dayForecast = weatherArray.getJSONObject(i);

                        // The date/time is returned as a long.  We need to convert that
                        // into something human-readable, since most people won't read "1400356800" as
                        // "this saturday".
                        dateTime = dayForecast.getLong(OWM_DATETIME);

                        pressure = dayForecast.getDouble(OWM_PRESSURE);
                        humidity = dayForecast.getInt(OWM_HUMIDITY);
                        windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                        windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                        // Description is in a child array called "weather", which is 1 element long.
                        // That element also contains a weather code.
                        JSONObject weatherObject =
                                dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                        description = weatherObject.getString(OWM_DESCRIPTION);
                        weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                        // Temperatures are in a child object called "temp".  Try not to name variables
                        // "temp" when working with temperature.  It confuses everybody.
                        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                        high = temperatureObject.getDouble(OWM_MAX);
                        low = temperatureObject.getDouble(OWM_MIN);

                        ContentValues weatherValues = new ContentValues();

                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationID);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                                WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                        cVVector.add(weatherValues);
                    }
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
                    }
                    Log.d(TAG, "FetchWeather finished");
                    refreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }


        });
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    private long addLocation(String locationSetting, String cityName, double lat, double lon) {

        // First, check if the location with this city name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri locationInsertUri = mContext.getContentResolver()
                    .insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }
}
