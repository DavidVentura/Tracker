package com.example.david.tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.david.tracker.store.Store;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView textGPS;
    private TextView textSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textGPS = (TextView) findViewById(R.id.gps_value);
        textSSID = (TextView) findViewById(R.id.ssid_value);
        EventReducer.ctx = getApplicationContext();
        startService(new Intent(this, MainService.class));
        setupLocation();
        Store.subscribe(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });
    }

    private void updateUI() {
        HashMap<String, Object> state = Store.getState();
        HashMap<String, Object> location = (HashMap<String, Object>) state.get("location");
        HashMap<String, Object> wifi = (HashMap<String, Object>) state.get("wifi");
        if (wifi != null) {
            String SSID = wifi.get("SSID") == null ? "" : wifi.get("SSID").toString();
            textSSID.setText(SSID);
        }
        if (location != null) {
            String gps = String.format("Lat: %s\nLong: %s\nAccuracy: %s\nProvider: %s",
                    location.get("LAT"),
                    location.get("LONG"),
                    location.get("ACC"),
                    location.get("PROV"));
            textGPS.setText(gps);
        }
    }

    private void setupLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            } else {
                getCoordinates();
            }
        } else {
            Log.d("GPS", "Getting coords < m");
            getCoordinates();
        }
    }

    private void getCoordinates() {
        try {
            final int timeInterval = 1000 * 30; // in msec
            final float minDistance = 10; // in meters
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeInterval, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterval, minDistance, locationListener);
        } catch (SecurityException e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getCoordinates();
        }
    }
}
