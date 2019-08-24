package com.jogtown.jogtown.utils.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.LocationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "Location Service";
    private final int LOCATION_REQUEST_CODE = 101;

    private final int TEN_SECONDS = 1000 * 10;
    private final int TWENTY_SECONDS = 1000 * 20;
    private final int THIRTY_SECONDS = 1000 * 30;

    public LocationManager locationManager;
    public JogtownLocationListener locationListener;

    Intent intent;

    double totalDistance = 0;
    Location oldLocation = null;

    Double start_latitude = null;
    Double start_longitude = null;
    List<List<Double>> coordinates = new ArrayList<>();
    List<Integer> paces = new ArrayList<>();
    List<Float> speeds = new ArrayList<>();

    HashMap<Integer, Boolean> laps = new HashMap<>(); //in kilometres
    List<Double> lapDistances = new ArrayList<>();

    int numberOfLocationUpdateSent = 0;

    private static LocationService instance = null;

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




    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        intent = new Intent(BROADCAST_ACTION);

        for (int i = 0; i < 51; i++) {
            if (i == 0) {
                laps.put(i, true);
            } else {
                laps.put(i, false);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocationService", "onCommandStart");
        super.onStartCommand(intent, flags, startId);
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationListener = new JogtownLocationListener();

        //I actually already checked for permission before starting the service.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TWENTY_SECONDS, 20, locationListener);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastKnownLocation != null) {
            updateIntentWithLatLng(lastKnownLocation);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        instance = null;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public void updateIntentWithLatLng(Location location) {

        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        if (start_latitude == null || start_longitude == null) {
            start_latitude = latitude;
            start_longitude = longitude;
        }

        if (oldLocation == null) {
            oldLocation = location;
        }

        LatLng currentCoordinates = new LatLng(latitude, longitude);


        SharedPreferences sharedPreferences = MainActivity.appContext.getSharedPreferences("JogPreferences", MODE_PRIVATE);
        boolean jogIsOn = sharedPreferences.getBoolean("jogIsOn", false);
        if (jogIsOn) {

            float startLatitude = sharedPreferences.getFloat("startLatitude", 0.0f);
            float startLongitude = sharedPreferences.getFloat("startLongitude", 0.0f);

            if (startLatitude != 0.0f || startLongitude != 0.0f) {
                if (oldLocation == null) {
                    oldLocation = new Location(LocationManager.GPS_PROVIDER);
                    oldLocation.setLatitude((double) startLatitude);
                    oldLocation.setLatitude((double) startLongitude);

                } else {
                    totalDistance = LocationUtils.distance(
                            oldLocation.getLatitude(), oldLocation.getLongitude(),
                            location.getLatitude(), location.getLongitude());
                    double lap = totalDistance / 1000;
                    int currentLap = (int) lap;
                    if (laps.get(currentLap) == false) {
                        //Update old location every 1 km
                        oldLocation = location;
                        //save distance for that lap
                        lapDistances.add(totalDistance);
                        //update lap
                        laps.put(currentLap, true);
                    }
                    //add lap distances saved from prev old locations
                    if (lapDistances.size() > 0) {
                        for (Double lapDistance : lapDistances) {
                            totalDistance += lapDistance;
                        }
                    }

                }
            }
            //send all intents
            intent.putExtra("currentCoordinates", currentCoordinates);
            intent.putExtra("speed", location.getSpeed());
            intent.putExtra("totalDistance", totalDistance);

            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            //send only location intents
            intent.putExtra("currentCoordinates", currentCoordinates);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];
        results[0] = 0;
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return Math.abs(results[0]);
    }




    public class JogtownLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                updateIntentWithLatLng(location);
                numberOfLocationUpdateSent += 1;
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(), "GPS ENABLED", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText( getApplicationContext(), "GPS DISABLED", Toast.LENGTH_SHORT ).show();
        }

    }
}
