package com.example.david.tracker.store;

/**
 * Created by david on 8/24/17.
 */


import android.content.Context;
import android.content.Intent;

import com.example.david.tracker.EventReducer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class Store {
    private static HashMap<String, Object> state = new HashMap<>();
    private static ArrayList<Runnable> listeners = new ArrayList<>();

    public static Object get(String key) {
        return state.get(key);
    }

    public static void dispatch(HashMap<String, Object> val) {
        String key = (String) val.get("type");
        if (key == null)
            return;

        state = EventReducer.reduce(state, val);

        for (Runnable r : listeners)
            r.run();

    }

    public static HashMap<String, Object> getState() {
        return state;
    }

    public static void subscribe(Runnable r) {
        listeners.add(r);
    }

    public static void unsuscribe(Runnable r) {
        listeners.remove(r);
    }

    public static String getJSON() {
        JSONObject j = new JSONObject(state);
        return j.toString();
    }
}
