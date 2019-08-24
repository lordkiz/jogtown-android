package com.jogtown.jogtown.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.GroupJogActiveFragment;
import com.jogtown.jogtown.fragments.GroupJogMembersFragment;
import com.jogtown.jogtown.subfragments.JogStatsFragment;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.services.JogStatsService;
import com.jogtown.jogtown.utils.services.LocationService;
import com.jogtown.jogtown.utils.adapters.ViewPagerAdapter;
import com.jogtown.jogtown.utils.ui.ZoomOutPageTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GroupJogActivity extends AppCompatActivity implements
        GroupJogActiveFragment.OnFragmentInteractionListener,
        GroupJogMembersFragment.OnFragmentInteractionListener,
        JogStatsFragment.OnFragmentInteractionListener
{
    final int JOG_NOTIFICATION_ID = 115;


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

    Boolean mapIsReady = false; //We need to know the map in GroupJogActiveFragment is ready before starting services.
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
        setContentView(R.layout.activity_group_jog);

        Intent intent = getIntent();
        try {
            groupObject = new JSONObject(intent.getStringExtra("group"));
            groupId = groupObject.getInt("id");
        } catch (JSONException e) {
            groupId = 0;
            e.printStackTrace();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tabLayout = (TabLayout) findViewById(R.id.group_run_activity_tabs);
        viewPager = (ViewPager) findViewById(R.id.group_run_activity_view_pager);

        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        tabLayout.setupWithViewPager(viewPager);

        sharedPreferences = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);

        //We dont want to start reading duration in GroupJogActivity Or SingleJogActivity
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
        viewPagerAdapter.addFragmentAndTitle(new GroupJogActiveFragment(), "Your Jog");
        viewPagerAdapter.addFragmentAndTitle(new GroupJogMembersFragment(), "Group Info");
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        String msg = uri.toString();
        Log.i("msg received", msg);
        switch (msg) {
            case "map is ready":
                //GroupJogActiveFragment must tell this parent activity that map is ready
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
        ObjectAnimator animator = ObjectAnimator.ofFloat(pauseButton, "translationX", -205f);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        pauseButton.setVisibility(View.GONE);
                        playStopLayout.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();


        //pauseButton.setVisibility(View.GONE);
        //playStopLayout.setVisibility(View.VISIBLE);

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
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.cancel(JOG_NOTIFICATION_ID);
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

        ObjectAnimator animator = ObjectAnimator.ofFloat(pauseButton, "translationX", 0f);
        animator.setDuration(500);

        animator.start();


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
        if (Build.VERSION.SDK_INT > 22) {
            topText.setCompoundDrawableTintList(ColorStateList.valueOf(Color.parseColor("#592DEA")));
        }
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
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#592DEA")));

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
        Gson gson = new Gson();
        Type coordType = new TypeToken<List<List<Double>>>() {}.getType();
        Type pacesType = new TypeToken<List<Integer>>() {}.getType();
        Type speedsType = new TypeToken<List<Float>>() {}.getType();
        Type lapsType = new TypeToken<List<JSONObject>>() {}.getType();

        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putBoolean("jogIsPaused", false);
        sharedPrefEditor.putBoolean("jogIsOn", false);
        sharedPrefEditor.apply();

        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", MODE_PRIVATE);

        int userId = authPref.getInt("userId", 0);
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
        String spds = sharedPreferences.getString("speeds", "");
        String pcs = sharedPreferences.getString("paces", "");
        String lapsString = sharedPreferences.getString("laps", "[]");

        List<JSONObject> laps = gson.fromJson(lapsString, lapsType);

        JSONObject lap = new JSONObject();
        try {
            lap.put("distance", distance);
            lap.put("duration", duration);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (lap.length() == 2) {
            laps.add(lap);
        }

        List<Integer> paces = gson.fromJson(pcs, pacesType);
        List<Float> speeds = gson.fromJson(spds, speedsType);


        String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

        //This next line should be done only when it is a group jog. Don't add elsewhere you are using this block of code.
        GroupJogMembersFragment.saveGroupMembershipStats(distance, duration, false);

        try {
            JSONObject jog = new JSONObject();
            jog.put("name", makeName());
            jog.put("user_id", userId);
            jog.put("duration", duration);
            jog.put("distance", distance);
            jog.put("calories", calories);
            jog.put("start_latitude", startLatitude);
            jog.put("end_latitude", endLatitude);
            jog.put("start_longitude", startLongitude);
            jog.put("end_longitude", endLongitude);
            jog.put("average_speed", averageSpeed);
            jog.put("max_speed", maxSpeed);
            jog.put("average_pace", averagePace);
            jog.put("max_pace", maxPace);
            jog.put("coordinates", coordinates);
            jog.put("speeds", new JSONArray(speeds));
            jog.put("paces", new JSONArray(paces));
            jog.put("hydration", hydration);
            jog.put("max_altitude", maxAltitude);
            jog.put("min_altitude", minAltitude);
            jog.put("total_ascent", totalAscent);
            jog.put("total_descent", totalDescent);
            jog.put("laps", new JSONArray(laps));
            jog.put("created_at", new SimpleDateFormat(DATE_FORMAT_PATTERN).format(new Date()));


            Intent intent = new Intent(this, JogDetailActivity.class);
            intent.putExtra("jog", jog.toString());
            intent.putExtra("canGoBack", false);
            intent.putExtra("shouldSave", true);
            startActivity(intent);
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public String makeName() {
        String[] adjectives = {"Beautiful", "Serene", "Cool", "Exciting", "Fine", "Lovely", "Splendid", "Great", "Pleasant", "Nice"};
        int index = (int) Math.floor(Math.random() * adjectives.length);
        return adjectives[index] + " Jog";
    }



    /// JOG NOTIFICATION IS IN JogStatsService (/services/JogStatsService);


    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String groupObj = groupObject.toString();
        editor.putString("group", groupObj);
        editor.apply();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }


}
