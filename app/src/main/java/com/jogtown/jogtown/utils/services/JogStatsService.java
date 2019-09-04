package com.jogtown.jogtown.utils.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.AppActivity;
import com.jogtown.jogtown.activities.GroupJogActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.activities.SingleJogActivity;
import com.jogtown.jogtown.utils.Conversions;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class JogStatsService extends Service {

    public static final String BROADCAST_ACTION = "Jog Stats Service";

    final String JOG_NOTIFICATION_CHANNEL_ID = "JOG_NOTIFICATION";
    final int JOG_NOTIFICATION_ID = 115;
    NotificationCompat.Builder notificationBuilder;

    Intent intent;

    int duration = 0;

    SharedPreferences sharedPreferences;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateStats();
            sendJogStatsNotification();
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
        int previousDuration = sharedPreferences.getInt("duration", 0);
        duration = previousDuration;
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
        notificationBuilder.setAutoCancel(true).setOngoing(false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void updateStats() {
        Log.i(BROADCAST_ACTION, "sending");
        duration += 1;
        intent.putExtra("duration", duration);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }

    /// JOG NOTIFICATION

    public void sendJogStatsNotification() {

        //Keep sending notification while user is jogging
        Intent intent = new Intent(this, SingleJogActivity.class);
        String jogType = sharedPreferences.getString("jogType", "single");
        if (jogType.equals("group")) {
            intent = new Intent(this, GroupJogActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        int duration = sharedPreferences.getInt("duration", 0);
        int distance = sharedPreferences.getInt("distance", 0);
        int weight = sharedPreferences.getInt("weight", 70);

        String durationText = Conversions.formatToHHMMSS(duration);
        String calories = Conversions.displayCalories(distance, duration, weight);
        String distanceText = Conversions.displayKilometres(distance) + " km";
        String notificationText = "Duration: " + durationText + "\n" +
                "Calories: " + calories + " kcal" + "\n" +
                "Distance: " + distanceText;

        if (notificationBuilder == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, JOG_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_paw)
                    .setContentTitle("Jogging")
                    .setContentText(notificationText)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notificationText)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent);

            notificationBuilder = builder;

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(JOG_NOTIFICATION_ID, notificationBuilder.build());

        } else {

            notificationBuilder
                    .setContentText(notificationText)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notificationText));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(JOG_NOTIFICATION_ID, notificationBuilder.build());

        }
    }

}
