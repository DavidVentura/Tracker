package com.example.david.tracker;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.david.tracker.store.Store;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by david on 01/01/17.
 */

public class MainService extends Service {
    private static final String TAG = "Tracker";
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    private Context mContext;
    private EventReceiver receiver = new EventReceiver();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        mContext = this;
        receiver = new EventReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(BCAST_CONFIGCHANGED);

        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        //startForeground(); //TODO: ??

        this.registerReceiver(receiver, filter);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        //Unregister receiver to avoid memory leaks
        mContext.unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class EventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            HashMap<String, Object> action = new HashMap<>();
            action.put("type", intent.getAction());
            Store.dispatch(action);
        }
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void setupLocation(final Context ctx) {
        locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String s, int i, Bundle b) {

            }

            @Override
            public void onProviderEnabled(String p) {

            }

            @Override
            public void onLocationChanged(Location location) {
                HashMap<String, Object> action = new HashMap<>();
                action.put("type", "location");

                action.put("LAT", location.getLatitude());
                action.put("LONG", location.getLongitude());
                action.put("ACC", location.getAccuracy());
                action.put("PROV", location.getProvider());
                Store.dispatch(action);
            }

            @Override
            public void onProviderDisabled(String provider) {
                ctx.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };
    }
    public static void setLocationCallback() {
        try {
            final int timeInterval = 1000 * 30; // in msec
            final float minDistance = 10; // in meters
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeInterval, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterval, minDistance, locationListener);
        } catch (SecurityException e) {

        }
    }

    public static void setupDevice(String android_id) {
        HashMap<String, Object> config_a = new HashMap<String, Object>();
        HashMap<String, Object> config = new HashMap<String, Object>();

        config.put("ENDPOINT", "https://tracker.davidventura.com.ar/");

        config_a.put("CONFIG", config);
        config_a.put("type", "CONFIG");
        Store.dispatch(config_a);

        HashMap<String, Object> idhm = new HashMap<String, Object>();
        idhm.put("ID", android_id);
        idhm.put("type", "ID");
        Store.dispatch(idhm);
    }

    public static String getUUID(Context ctx) {
        final String FILENAME = "UUID_FILE";
        String uuid;

        try {
            FileInputStream fis = ctx.openFileInput(FILENAME);
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            uuid = fileContent.toString();
            fis.close();
        } catch (Exception e) {
            uuid = UUID.randomUUID().toString();
            try {
                FileOutputStream fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write(uuid.getBytes());
                fos.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return uuid;
    }


}
