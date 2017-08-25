package com.example.david.tracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.david.tracker.store.Store;

import java.util.HashMap;

/**
 * Created by david on 01/01/17.
 */

public class MainService extends Service {
    private static final String TAG = "Tracker";

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

}
