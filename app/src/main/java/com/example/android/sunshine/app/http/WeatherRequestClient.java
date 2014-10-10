package com.example.android.sunshine.app.http;

import android.net.Uri;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by chuanl on 10/10/14.
 */
public class WeatherRequestClient {

    private static final String FORECAST_BASE_URL =
            "http://api.openweathermap.org/data/2.5/forecast/daily?";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get
            (String location, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.get(getAbsoluteUrl(location), params, responseHandler);
    }

    public static void post
            (String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String location){
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";

        String format = "json";
        String units = "metric";
        int numDays = 14;

        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();

        return builtUri.toString();
    }
}
