package com.jogtown.jogtown.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.material.tabs.TabLayout;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.GroupRunActiveFragment;
import com.jogtown.jogtown.fragments.GroupRunMembersFragment;
import com.jogtown.jogtown.subfragments.SingleRunStatsFragment;
import com.jogtown.jogtown.utils.services.JogStatsService;
import com.jogtown.jogtown.utils.services.LocationService;
import com.jogtown.jogtown.utils.adapters.ViewPagerAdapter;
import com.jogtown.jogtown.utils.ui.ZoomOutPageTransformer;

import org.json.JSONException;
import org.json.JSONObject;

public class GroupRunActivity extends AppCompatActivity implements
        GroupRunActiveFragment.OnFragmentInteractionListener,
        GroupRunMembersFragment.OnFragmentInteractionListener,
        SingleRunStatsFragment.OnFragmentInteractionListener
{

    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            buttonPressedDurationProgressBar.incrementProgressBy(100);
            handler.postDelayed(this, 100);
        }
    };

    TabLayout tabLayout;
    ViewPager viewPager;

    public static int groupId; //Group you are running with
    public static JSONObject groupObject; //Group you are running with

    Boolean mapIsReady = false; //We need to know the map in GroupRunActiveFragment is ready before starting services.
    private final int LOCATION_REQUEST_CODE = 101;

    private FrameLayout playStopLayout;
    ImageButton pauseButton;
    ImageButton stopJogButton;


    SharedPreferences sharedPreferences;
    Intent locationServiceIntent;
    Intent jogStatsServiceIntent;

    boolean jogIsOn;
    boolean jogIsPaused;

    ProgressBar buttonPressedDurationProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_run);

        Intent intent = getIntent();
        try {
            groupObject = new JSONObject(intent.getStringExtra("group"));
            groupId = groupObject.getInt("id");
        } catch (JSONException e) {
            groupId = 0;
            e.printStackTrace();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setElevation(0);

        tabLayout = (TabLayout) findViewById(R.id.group_run_activity_tabs);
        viewPager = (ViewPager) findViewById(R.id.group_run_activity_view_pager);

        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        tabLayout.setupWithViewPager(viewPager);

        sharedPreferences = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);

        //We dont want to start reading duration in GroupRunActivity Or SingleRunActivity
        //So we keep intents separate
        locationServiceIntent = new Intent(this, LocationService.class);
        jogStatsServiceIntent = new Intent(this, JogStatsService.class);

        //Update Jog Status;
        //Indicate a jog is ongoing or not
        updateJogStatus();

        buttonPressedDurationProgressBar = createProgressBar();

        addTabsToTabLayout(viewPager);

    }

    public void addTabsToTabLayout(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragmentAndTitle(new GroupRunActiveFragment(), "Your Jog");
        viewPagerAdapter.addFragmentAndTitle(new GroupRunMembersFragment(), "Group Info");
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        String msg = uri.toString();
        Log.i("msg received", msg);
        switch (msg) {
            case "map is ready":
                //GroupRunActiveFragment must tell this parent activity that map is ready
                mapIsReady = true;
                startAllServices();
                setUpControlJogButtons();

                break;
        }
    }

    public void setUpControlJogButtons() {
        pauseButton = findViewById(R.id.groupRunPauseButton);
        playStopLayout = findViewById(R.id.group_play_stop_layout);

        pauseButton.setVisibility(View.VISIBLE);
        playStopLayout.setVisibility(View.GONE);

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
            stopJogButton = findViewById(R.id.groupRunStopButton);
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
            sharedPrefEditor.putString("jogType", "group");
            sharedPrefEditor.apply();
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

}
