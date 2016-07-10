package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;

public class WatchUpdaterReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private GoogleApiClient mGoogleApiClient;
    private DataMap config;
    public static final String KEY_MAX_TEMP = "MAX_TEMP";
    public static final String KEY_MIN_TEMP = "MIN_TEMP";
    public static final String KEY_WEATHER_TYPE = "WEATHER_TYPE";
    public static final String KEY_BATTERY_PERCENATGE = "BATTERY_PERCENATGE";
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";


    public WatchUpdaterReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        if (intent.getAction().equals(SunshineSyncAdapter.ACTION_DATA_UPDATED)) {
            String locationQuery = Utility.getPreferredLocation(context);
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, SunshineSyncAdapter.NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                //String desc = cursor.getString(INDEX_SHORT_DESC);

                //int iconId = Utility.getIconResourceForWeatherCondition(weatherId);

                config = new DataMap();
                config.putString(KEY_WEATHER_TYPE, weatherId+"");
                config.putString(KEY_MAX_TEMP, high+"");
                config.putString(KEY_MIN_TEMP, low+"");
                config.putString(KEY_BATTERY_PERCENATGE, "49");

            }
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("WatchUpdaterReceiver", "onConnected: " + bundle);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(config!=null && config.size()>0) {
                        PutDataMapRequest putDMR = PutDataMapRequest.create(PATH_WITH_FEATURE);
                        putDMR.getDataMap().putAll(config);
                        PutDataRequest request = putDMR.asPutDataRequest().setUrgent();
                        DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
                        if (result.getStatus().isSuccess()) {
                            Log.v("WatchUpdaterReceiver", "DataMap: " + config + " sent successfully to data layer ");
                        }
                        else {
                            // Log an error
                            Log.v("WatchUpdaterReceiver", "ERROR: failed to send DataMap to data layer");
                        }
                }
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable("WatchUpdaterReceiver", Log.DEBUG)) {
            Log.d("WatchUpdaterReceiver", "onConnectionSuspended: " + i);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (Log.isLoggable("WatchUpdaterReceiver", Log.DEBUG)) {
            Log.d("WatchUpdaterReceiver", "onConnectionFailed: " + connectionResult);
        }
    }
}
