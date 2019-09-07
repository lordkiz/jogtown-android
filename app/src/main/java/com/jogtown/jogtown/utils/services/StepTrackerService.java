package com.jogtown.jogtown.utils.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.jogtown.jogtown.activities.MainActivity;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StepTrackerService extends Service implements SensorEventListener {

    int steps = 0;
    SensorManager sensorManager;
    Sensor sensor;

    Intent intent;
    public static final String BROADCAST_ACTION = "Step Tracker Service";
    private static StepTrackerService instance = null;



    public static boolean isServiceRunning() {
        try {
            // instance is not null and can ping
            return instance != null && instance.ping();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean ping() {
        // if instance is actually not null, it should be able to ping.
        return true;
    }


    // Service Methods

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        intent = new Intent(BROADCAST_ACTION);
        SharedPreferences jogPref = MainActivity.appContext.getSharedPreferences("JogPreferences", MODE_PRIVATE);
        steps = jogPref.getInt("steps", 0);


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        SharedPreferences jogPref = MainActivity.appContext.getSharedPreferences("JogPreferences", MODE_PRIVATE);
        steps = jogPref.getInt("steps", 0);
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

            sensorManager.registerListener(this, sensor,10 );
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, sensor);
        instance = null;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    //SensorEventListener Methods

    @Override
    public void onSensorChanged(SensorEvent event) {
        steps++;
        intent.putExtra("steps", steps);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
