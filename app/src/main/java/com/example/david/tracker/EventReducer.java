package com.example.david.tracker;

/**
 * Created by david on 8/24/17.
 */

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.david.tracker.actions.PostAction;
import com.example.david.tracker.store.Store;

import java.util.HashMap;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by david on 04/01/17.
 */

public class EventReducer {
    private final static int LOCATION_TIME_THRESHOLD = 30; // IN SECONDS
    private final static int POST_TIME_THRESHOLD = 90; // IN SECONDS
    private static Integer last_loc_tstamp;
    private static Integer last_post_tstamp;
    public static Context ctx;

    public static HashMap<String, Object> reduce(HashMap<String, Object> oldState, HashMap<String, Object> action) {
        String type = (String) action.get("type");
        int tstamp = (int) (System.currentTimeMillis() / 1000);
        HashMap<String, Object> newState = new HashMap<>(oldState);
        //FIXME: Should reducer be part of the store?
        action.remove("type"); //Clear up the state
        switch (type) {
            case "CONFIG":
                newState.put(type, action.get(type));
                break;
            case "ID":
                newState.put("ID", action.get("ID"));
                break;
            case "location":
                // If the data point is the first, it's valid.
                if (last_loc_tstamp != null) {
                    // If it's not the first, only consider it if the accuracy is better.
                    if ((tstamp - last_loc_tstamp) < LOCATION_TIME_THRESHOLD) {
                        float newAcc = (float) action.get("ACC");
                        HashMap<String, Object> old_loc = (HashMap<String, Object>) oldState.get("location");
                        float oldAcc = (float) old_loc.get("ACC");
                        if (newAcc > oldAcc) { //Accuracy actually is better if it's LOWER
                            break;
                        }
                    }
                }
                // If we are here either:
                // * it's the first data point
                // * it's the first (valid) data point in LOCATION_TIME_THRESHOLD
                // * it's a point with better accuracy than the last one
                last_loc_tstamp = tstamp;
                newState.put("location", action);
                break;
            case Intent.ACTION_TIME_TICK:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
                if (last_post_tstamp != null) {
                    if (tstamp - last_post_tstamp < POST_TIME_THRESHOLD) {
                        break;
                    }
                }

                PostAction.post(Store.getJSON());
                last_post_tstamp = tstamp;
                break;
            case Intent.ACTION_SCREEN_OFF:
                break;
            case Intent.ACTION_SCREEN_ON:
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiManager.isWifiEnabled()) {
                    String ssid = wifiInfo.getSSID();
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                    }
                    action.put("SSID", ssid);
                    action.put("ENABLED", true);
                } else {
                    action.put("SSID", "");
                    action.put("ENABLED", false);
                }
                newState.put("wifi", action);
                break;
            default:
                Log.d("EventReducer", type);
        }

        return newState;
    }
}