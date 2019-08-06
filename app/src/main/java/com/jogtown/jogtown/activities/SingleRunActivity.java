package com.jogtown.jogtown.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.subfragments.SingleRunStatsFragment;
import com.jogtown.jogtown.utils.JogStatsService;
import com.jogtown.jogtown.utils.LocationService;

import java.util.List;


public class SingleRunActivity extends AppCompatActivity implements
        SingleRunStatsFragment.OnFragmentInteractionListener,
        OnMapReadyCallback
{

    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            buttonPressedDurationProgressBar.incrementProgressBy(100);
            handler.postDelayed(this, 100);
        }
    };


    ProgressBar buttonPressedDurationProgressBar;

    private FrameLayout playStopLayout;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "SingleRunMapViewBundleKey";
    GoogleMap mMap;
    LocationManager locationManager;
    PolylineOptions polylineOptions = new PolylineOptions();


    ImageButton stopJogButton;
    ImageButton pauseButton;



    private final int LOCATION_REQUEST_CODE = 101;

    SharedPreferences sharedPreferences;
    Intent locationServiceIntent;
    Intent jogStatsServiceIntent;
    boolean jogIsOn;
    boolean jogIsPaused;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_run);

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.

        Bundle mapViewBundle = null;

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        sharedPreferences = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);

        playStopLayout = (FrameLayout) findViewById(R.id.single_run_play_stop_layout);
        pauseButton = (ImageButton) findViewById(R.id.singleRunPauseButton);

        //Initially keep Jog Control Buttons hidden until we are certain we can start Services
        //Because a user can start and stop services with those buttons. For eg starting a
        //location service before getting permissions
        pauseButton.setVisibility(View.GONE);
        playStopLayout.setVisibility(View.GONE);

        mMapView = (MapView) findViewById(R.id.single_run_activity_map_view);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        locationServiceIntent = new Intent(this, LocationService.class);
        jogStatsServiceIntent = new Intent(this, JogStatsService.class);

        registerLocationBroadcastReceiver();

        //Indicate a jog is ongoing or not
        updateJogStatus();

        buttonPressedDurationProgressBar = createProgressBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Single Jog");
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                startAllServices();
                pauseButton.setVisibility(View.VISIBLE);
                playStopLayout.setVisibility(View.GONE);

            }
            mMapView.onResume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mMap != null) {
                            mMap.setMyLocationEnabled(true);
                        }
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownLocation != null) {
                            Log.i("location", lastKnownLocation.toString());
                            LatLng latlng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            Float startLatitude = sharedPreferences.getFloat("startLatitude", 0.0f);
                            Float startLongitude = sharedPreferences.getFloat("startLongitude", 0.0f);

                            if (startLatitude == 0.0f || startLongitude == 0.0f) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putFloat("startLatitude", (float)lastKnownLocation.getLatitude());
                                editor.putFloat("startLongitude", (float)lastKnownLocation.getLongitude());
                                editor.apply();
                            }
                            updateMap(latlng);
                        }
                        startAllServices();
                        pauseButton.setVisibility(View.VISIBLE);
                        playStopLayout.setVisibility(View.GONE);

                    }

                } else {
                    Toast.makeText(this,"Location permission missing",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    public void updateMap(LatLng coordinates) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
        polylineOptions.add(coordinates);
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(5);
        mMap.addPolyline(polylineOptions);
    }

    public void registerLocationBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double latitude = intent.getDoubleExtra("latitude", 0);
                        double longitude = intent.getDoubleExtra("longitude", 0);

                        LatLng loc = new LatLng(latitude, longitude);
                        updateMap(loc);
                        Float startLatitude = sharedPreferences.getFloat("startLatitude", 0.0f);
                        Float startLongitude = sharedPreferences.getFloat("startLongitude", 0.0f);

                        if (startLatitude == 0.0f || startLongitude == 0.0f) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putFloat("startLatitude", (float) latitude);
                            editor.putFloat("startLongitude", (float) longitude);
                            editor.apply();
                        }

                    }
                }, new IntentFilter(LocationService.BROADCAST_ACTION)
        );
    }



    @SuppressLint("ClickableViewAccessibility")
    public void pauseJog(View view) {

        stopAllServices();
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

        sharedPrefEditor.putBoolean("jogIsPaused", true);
        sharedPrefEditor.apply();

        pauseButton.setVisibility(View.GONE);
        playStopLayout.setVisibility(View.VISIBLE);


        if (stopJogButton == null) {
            final AlertDialog eventDurationDialog = eventDurationDialog();
            stopJogButton = findViewById(R.id.singleRunStopButton);
            stopJogButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    long eventDuration = event.getEventTime() - event.getDownTime();
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        incrementProgress();
                        eventDurationDialog.show();
                        v.setTag(true);
                        //} else if (v.isPressed() && (boolean) v.getTag()) {
                    } else if (event.getAction() == MotionEvent.ACTION_UP && eventDuration >= 1000) {
                        eventDurationDialog.dismiss();
                        stopProgress();
                        v.setTag(false);
                        stopAllServices();
                        redirectToJogDetail();
                    } else if (event.getAction() == MotionEvent.ACTION_UP && eventDuration < 1000) {
                        stopProgress();
                        eventDurationDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Long press to end jog", Toast.LENGTH_SHORT).show();
                    }

                    return false;
                }
            });
        }

    }


    public void resumeJog(View view) {

        startAllServices();
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

        sharedPrefEditor.putBoolean("jogIsPaused", false);
        sharedPrefEditor.apply();


        pauseButton.setVisibility(View.VISIBLE);
        playStopLayout.setVisibility(View.GONE);

    }


    public void updateJogStatus() {
        jogIsOn = sharedPreferences.getBoolean("jogIsOn", false);
        jogIsPaused = sharedPreferences.getBoolean("jogIsPaused", false);

        if (!jogIsOn && !jogIsPaused) {
            // Start jog only when it is not paused or not on
            //Every other global control should be from the Jog Buttons

            SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

            sharedPrefEditor.putBoolean("jogIsOn", true);
            sharedPrefEditor.putString("jogType", "single");
            sharedPrefEditor.apply();
        }

    }


    public void stopAllServices() {
        this.stopService(locationServiceIntent);
        this.stopService(jogStatsServiceIntent);

    }

    public void startAllServices() {
        //Only start the services if they are not already running
        if (!LocationService.isServiceRunning()) {
            this.startService(locationServiceIntent);
        }
        if (!JogStatsService.isServiceRunning()) {
            this.startService(jogStatsServiceIntent);
        }

    }



    public AlertDialog eventDurationDialog() {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        buttonPressedDurationProgressBar.setPadding(0, 0, 0, 10);
        buttonPressedDurationProgressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this);
        tvText.setText("Hold To End Jog");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        Drawable runIcon = getDrawable(R.drawable.ic_walk);
        TextView topText = new TextView(this);
        topText.setText("");
        topText.setCompoundDrawablesWithIntrinsicBounds(null, runIcon, null, null);
        topText.setCompoundDrawableTintList(ColorStateList.valueOf(Color.parseColor(getString(R.color.colorPrimary))));
        topText.setScaleX(2);
        topText.setScaleY(2);
        topText.setPadding(0, 50, 0, 0);
        topText.setLayoutParams(llParam);

        ll.addView(topText);
        ll.addView(buttonPressedDurationProgressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    public ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMinimumHeight(30);
        progressBar.setMinimumWidth(200);
        progressBar.setMax(1000);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(getString(R.color.colorPrimary))));

        return progressBar;
    }


    public void incrementProgress() {
        handler.postDelayed(runnable, 100);
    }

    public void stopProgress() {
        buttonPressedDurationProgressBar.setProgress(0);
        handler.removeCallbacks(runnable);
    }

    public void redirectToJogDetail() {
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

        sharedPrefEditor.putBoolean("jogIsPaused", false);
        sharedPrefEditor.putBoolean("jogIsOn", false);
        sharedPrefEditor.apply();

        int duration = sharedPreferences.getInt("duration", 0);
        int distance = sharedPreferences.getInt("distance", 0);
        Float calories = sharedPreferences.getFloat("calories", 0);
        Float startLatitude = sharedPreferences.getFloat("startLatitude", 0.0f);
        Float endLatitude = sharedPreferences.getFloat("endLatitude", 0.0f);
        Float startLongitude = sharedPreferences.getFloat("startLongitude", 0.0f);
        Float endLongitude = sharedPreferences.getFloat("endLongitude", 0.0f);
        Float averageSpeed = sharedPreferences.getFloat("averageSpeed", 0);
        Float maxSpeed = sharedPreferences.getFloat("maxSpeed", 0);
        int averagePace = sharedPreferences.getInt("averagePace", 0);
        int maxPace = sharedPreferences.getInt("maxPace", 0);
        int hydration = sharedPreferences.getInt("hydration", 0);
        float maxAltitude = sharedPreferences.getFloat("maxAltitude", 0);
        float minAltitude = sharedPreferences.getFloat("minAltitude", 0);
        int totalAscent = sharedPreferences.getInt("totalAscent", 0);
        int totalDescent = sharedPreferences.getInt("totalDescent", 0);

        String coordinates = sharedPreferences.getString("coordinates", "[]");
        String speeds = sharedPreferences.getString("speeds", "");
        String paces = sharedPreferences.getString("paces", "");

        Intent intent = new Intent(this, JogDetailActivity.class);
        intent.putExtra("duration", duration);
        intent.putExtra("distance", distance);
        intent.putExtra("calories", calories);
        intent.putExtra("startLatitude", startLatitude);
        intent.putExtra("endLatitude", endLatitude);
        intent.putExtra("startLongitude", startLongitude);
        intent.putExtra("endLongitude", endLongitude);
        intent.putExtra("averageSpeed", averageSpeed);
        intent.putExtra("maxSpeed", maxSpeed);
        intent.putExtra("averagePace", averagePace);
        intent.putExtra("maxPace", maxPace);
        intent.putExtra("coordinates", coordinates);
        intent.putExtra("speeds", speeds);
        intent.putExtra("paces", paces);
        intent.putExtra("hydration", hydration);
        intent.putExtra("maxAltitude", maxAltitude);
        intent.putExtra("minAltitude", minAltitude);
        intent.putExtra("totalAscent", totalAscent);
        intent.putExtra("totalDescent", totalDescent);

        intent.putExtra("canGoBack", false);
        intent.putExtra("shouldSave", true);
        startActivity(intent);
        finish();
    }








    /*
     * Because we are using MapView instead on MapFragment,
     * You have to manually override all its methods which are:
     * onStart()
     * onStop()
     * onPause()
     * onDestroy()
     * onLowMemory()
     * onMapReady()
     * */

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }
}
