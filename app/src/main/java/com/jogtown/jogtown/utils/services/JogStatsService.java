package com.jogtown.jogtown.utils.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.jogtown.jogtown.activities.MainActivity;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class JogStatsService extends Service {

    public static final String BROADCAST_ACTION = "Jog Stats Service";
    Intent intent;

    int duration = 0;

    SharedPreferences sharedPreferences;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateStats();
            handler.postDelayed(this, 1000);
        }
    };

    private static JogStatsService instance = null;

    public static boolean isServiceRunning() {
        try {
            // instance is not null and can ping
            return instance != null && instance.ping();
        } catch (NullPointerException e) {
            //instance cannot ping, so it is possible this has been removed
            //without calling onDestroy e.g when android kills services to save memory
            return false;
        }
    }

    public boolean ping() {
        // if instance is actually not null, it should be able to ping.
        return true;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        intent = new Intent(BROADCAST_ACTION);
        sharedPreferences = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);

        int previousDuration = sharedPreferences.getInt("duration", 0);
        duration += previousDuration;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("JogStatsService", "onStartCommand");
        handler.postDelayed(runnable, 1000);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("duration", duration);
        editor.apply();
        handler.removeCallbacks(runnable);
        instance = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void updateStats() {
        Log.i(BROADCAST_ACTION, "sending");
        duration += 1;
        intent.putExtra("jogStatsServiceDuration", duration);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }
}
