package com.example.david.tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.david.tracker.store.Store;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView textGPS;
    private TextView textID;
    private TextView textSSID;
    private TextView textEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventReducer.ctx = getApplicationContext();
        MainService.setupLocation(getApplicationContext());
        if (!MainService.checkPermission(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
        } else {
            MainService.setLocationCallback();
        }


        String android_id = MainService.getUUID(getApplicationContext());
        MainService.setupDevice(android_id);


        textGPS = (TextView) findViewById(R.id.gps_value);
        textSSID = (TextView) findViewById(R.id.ssid_value);
        textID = (TextView) findViewById(R.id.id_value);
        textEndpoint = (TextView) findViewById(R.id.endpoint_value);


        startService(new Intent(this, MainService.class));


        Store.subscribe(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    private void updateUI() {
        HashMap<String, Object> state = Store.getState();
        HashMap<String, Object> location = (HashMap<String, Object>) state.get("location");
        HashMap<String, Object> wifi = (HashMap<String, Object>) state.get("wifi");
        String ENDPOINT = (String) (((HashMap<String, Object>) state.get("CONFIG")).get("ENDPOINT"));

        String id = (String) state.get("ID");
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
        textID.setText(id);
        textEndpoint.setText(ENDPOINT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    MainService.setLocationCallback();
        }
    }
}
