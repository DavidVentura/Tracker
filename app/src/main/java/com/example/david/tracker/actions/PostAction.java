package com.example.david.tracker.actions;

import android.util.Log;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by david on 8/25/17.
 */

public class PostAction {
    private static final String ENDPOINT = "https://tracker.davidventura.com.ar/";

    public static void post(final String data) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("HTTP-DATA", data);

                    HttpsURLConnection conn = (HttpsURLConnection) new URL(ENDPOINT).openConnection();
                    //HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    Log.d("HTTP-CODE", Integer.toString(conn.getResponseCode()));
                } catch (Exception e) {
                    Log.d("InputStream", e.toString());
                }
            }
        };
        t.start();
    }
}
