package com.example.sunshinewatchface;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public final class DigitalWatchFaceUtil {
    private static final String TAG = "DigitalWatchFaceUtil";

    /**
     * The {@link DataMap} key for { WatchFace} background color name.
     * The color name must be a {@link String} recognized by {@link Color#parseColor}.
     */
    public static final String KEY_MAX_TEMP = "MAX_TEMP";

    /**
     * The {@link DataMap} key for { WatchFace} hour digits color name.
     * The color name must be a {@link String} recognized by {@link Color#parseColor}.
     */
    public static final String KEY_MIN_TEMP = "MIN_TEMP";

    /**
     * The {@link DataMap} key for { WatchFace} minute digits color name.
     * The color name must be a {@link String} recognized by {@link Color#parseColor}.
     */
    public static final String KEY_WEATHER_TYPE = "WEATHER_TYPE";

    /**
     * The {@link DataMap} key for { WatchFace} second digits color name.
     * The color name must be a {@link String} recognized by {@link Color#parseColor}.
     */
    public static final String KEY_BATTERY_PERCENATGE = "BATTERY_PERCENATGE";

    /**
     * The path for the {@link DataItem} containing { WatchFace} configuration.
     */
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";


    /**
     * Callback interface to perform an action with the current config {@link DataMap} for
     * { WatchFace}.
     */
    public interface FetchConfigDataMapCallback {
        /**
         * Callback invoked with the current config {@link DataMap} for
         * { WatchFace}.
         */
        void onConfigDataMapFetched(DataMap config);
    }

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName.toLowerCase());
    }

    /**
     * Asynchronously fetches the current config {@link DataMap} for { WatchFace}
     * and passes it to the given callback.
     * <p>
     * If the current config {@link DataItem} doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
    public static void fetchConfigDataMap(final GoogleApiClient client,
            final FetchConfigDataMapCallback callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(DigitalWatchFaceUtil.PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build();
                        Wearable.DataApi.getDataItem(client, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                }
        );
    }


    /**
     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
     * If the config DataItem doesn't exist, it's created.
     */
    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        putDataMapRequest.setUrgent();
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
                        }
                    }
                });
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallback;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResult(DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }

    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.icon_11d;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.icon_10d;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.icon_09d;
        } else if (weatherId == 511) {
            return R.drawable.icon_13d;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.icon_09d;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.icon_13d;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.icon_03d;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.icon_11d;
        } else if (weatherId == 800) {
            return R.drawable.icon_01d;
        } else if (weatherId == 801) {
            return R.drawable.icon_02d;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.icon_04d;
        }
        return -1;
    }

    private DigitalWatchFaceUtil() { }
}
