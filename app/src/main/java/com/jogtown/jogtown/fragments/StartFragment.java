package com.jogtown.jogtown.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.subfragments.JogStatsFragment;
import com.jogtown.jogtown.utils.services.JogStatsService;
import com.jogtown.jogtown.utils.services.LocationService;
import com.jogtown.jogtown.utils.services.StepTrackerService;
import com.jogtown.jogtown.utils.ui.MyTypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment implements OnMapReadyCallback,
        JogStatsFragment.OnFragmentInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    GoogleMap mMap;
    private MapView mMapView;
    LocationManager locationManager;

    PolylineOptions polylineOptions = new PolylineOptions();

    LottieAnimationView jogTreadmillView;

    private final int LOCATION_REQUEST_CODE = 101;

    final int JOG_NOTIFICATION_ID = 115;

    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            buttonPressedDurationProgressBar.incrementProgressBy(100);
            handler.postDelayed(this, 100);
        }
    };


    AlertDialog eventDurationDialog;

    ProgressBar buttonPressedDurationProgressBar;

    ImageButton stopButton;
    ImageButton pauseButton;
    ImageButton playButton;

    Switch jogTypeSwitch; //From JogStatsFragment

    private static final String MAPVIEW_BUNDLE_KEY = "SingleRunMapViewBundleKey";

    SharedPreferences jogPref;
    Intent locationServiceIntent;
    Intent jogStatsServiceIntent;
    Intent stepTrackerServiceIntent;



    LinearLayout jogStatsContainer;
    TextView jogStatsDuration;
    TextView jogStatsDurationTitle;

    TextView jogStatsDistance;
    TextView jogStatsDistanceTitle;

    TextView jogStatsPace;
    TextView jogStatsPaceTitle;

    TextView jogStatsCalories;
    TextView jogStatsCaloriesTitle;

    ConstraintLayout jogStatsConstraintLayout;
    View treadmillOuterContainer;


    public StartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartFragment newInstance(String param1, String param2) {
        StartFragment fragment = new StartFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        view.getContext().setTheme(R.style.AppTheme);
        try {
            ActionBar actionBar =  ((AppCompatActivity) getActivity()).getSupportActionBar();

            SpannableString spannableString = new SpannableString("Start");
            spannableString.setSpan(
                    new MyTypefaceSpan(getContext(), "fonts/baijamjuree_semi_bold.ttf"),
                    0,
                    spannableString.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            actionBar.setTitle(spannableString);
        } catch (NullPointerException e) {
            //
        }
        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        Bundle mapViewBundle = null;

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        jogPref = getContext().getSharedPreferences("JogPreferences", MODE_PRIVATE);


        pauseButton = view.findViewById(R.id.jogPauseButton);
        stopButton = view.findViewById(R.id.jogStopButton);
        playButton = view.findViewById(R.id.jogPlayButton);

        jogTreadmillView = view.findViewById(R.id.jog_treadmill_view);

        jogStatsContainer = view.findViewById(R.id.jog_stats_fragment);
        jogStatsDuration = view.findViewById(R.id.jogStatsDuration);
        jogStatsDurationTitle = view.findViewById(R.id.jogStatsDurationTitle);

        jogStatsDistance = view.findViewById(R.id.jogStatsDistance);
        jogStatsDistanceTitle = view.findViewById(R.id.jogStatsDistanceTitle);

        jogStatsPace = view.findViewById(R.id.jogStatsPace);
        jogStatsPaceTitle = view.findViewById(R.id.jogStatsPaceTitle);

        jogStatsCalories = view.findViewById(R.id.jogStatsCalories);
        jogStatsCaloriesTitle = view.findViewById(R.id.jogStatsCaloriesTitle);

        jogStatsConstraintLayout = view.findViewById(R.id.jog_stats_constraint_layout);
        treadmillOuterContainer = view.findViewById(R.id.treadmill_outer_container);

        //Initially keep Jog Control Buttons hidden until we are certain we can start Services
        //Because a user can start and stop services with those buttons. For eg starting a
        //location service before getting permissions
        pauseButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);

        mMapView = view.findViewById(R.id.jog_map_view);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        locationServiceIntent = new Intent(getContext(), LocationService.class);
        jogStatsServiceIntent = new Intent(getContext(), JogStatsService.class);
        stepTrackerServiceIntent = new Intent(getContext(), StepTrackerService.class);

        registerLocationBroadcastReceiver();

        buttonPressedDurationProgressBar = createProgressBar();

        //important that this is called after you already have a progressBar
        eventDurationDialog = eventDurationDialog();

        handleButtonVisibilities();

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseJog();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrResumeJog();
            }
        });

        stopButton.setOnTouchListener(new View.OnTouchListener() {
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

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                    notificationManager.cancel(JOG_NOTIFICATION_ID);

                    redirectToJogDetail();
                } else if (event.getAction() == MotionEvent.ACTION_UP && eventDuration < 1000) {
                    stopProgress();
                    eventDurationDialog.dismiss();
                    Toast.makeText(getContext(), "Long press to end jog", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });


        jogTypeSwitch = view.findViewById(R.id.jogTypeSwitch);

        jogTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onSwitchChanged(isChecked);
            }
        });


        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //Because I implemented Jogstats.OnfragmentInteration
    @Override
    public void onFragmentInteraction(Uri uri) {
        String message = uri.toString();
        if (message.equals("JogSwitch=true")) {
            //i.e switched to treadmill
            if (jogTreadmillView.getVisibility() == View.GONE) {
                ObjectAnimator animator = ObjectAnimator.ofInt(jogTreadmillView, "visibility", View.GONE, View.VISIBLE);
                animator.start();
            }

        } else {
            if (jogTreadmillView.getVisibility() == View.GONE) {
                ObjectAnimator animator = ObjectAnimator.ofInt(jogTreadmillView, "visibility", View.VISIBLE, View.GONE);
                animator.start();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

                //It is a bit different how get OnRequestPermissionResult to work in fragments
                //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            } else {
                zoomIntoMap();
                handleButtonVisibilities();
                Boolean jogIsOn = jogPref.getBoolean("jogIsOn", false);

                if (jogIsOn) {
                    startAllServices();
                }
            }

            mMapView.onResume();
        }
    }

    private LatLng extractLastKnownLatLng(Location lastKnownLocation) {
        LatLng latlng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        Float startLatitude = jogPref.getFloat("startLatitude", 0.0f);
        Float startLongitude = jogPref.getFloat("startLongitude", 0.0f);

        if (startLatitude == 0.0f || startLongitude == 0.0f) {
            SharedPreferences.Editor editor = jogPref.edit();
            editor.putFloat("startLatitude", (float)lastKnownLocation.getLatitude());
            editor.putFloat("startLongitude", (float)lastKnownLocation.getLongitude());
            editor.apply();
        }
        return latlng;
    }

    @SuppressLint("MissingPermission")
    private void zoomIntoMap() {
        //func only called after checking permissions
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            LatLng latlng = extractLastKnownLatLng(lastKnownLocation);
            updateMap(latlng);
        } else {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                LatLng latlng = extractLastKnownLatLng(lastKnownLocation);
                updateMap(latlng);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        zoomIntoMap();

                    }

                } else {
                    Toast.makeText(getContext(),"Location permission missing",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    void handleButtonVisibilities() {
        Boolean jogIsOn = jogPref.getBoolean("jogIsOn", false);
        Boolean jogIsPaused = jogPref.getBoolean("jogIsPaused", false);
        if (jogIsOn && !jogIsPaused) {
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
        } else if (jogIsOn && jogIsPaused) {
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.VISIBLE);
        } else {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.GONE);
        }
    }


    public void updateMap(LatLng coordinates) {
        if (mMap != null) {
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
            polylineOptions.add(coordinates);
            polylineOptions.color(Color.GREEN);
            polylineOptions.width(5);
            mMap.addPolyline(polylineOptions);
        }
    }

    public void registerLocationBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double latitude = intent.getDoubleExtra("latitude", 0);
                        double longitude = intent.getDoubleExtra("longitude", 0);

                        LatLng loc = new LatLng(latitude, longitude);
                        updateMap(loc);
                        Float startLatitude = jogPref.getFloat("startLatitude", 0.0f);
                        Float startLongitude = jogPref.getFloat("startLongitude", 0.0f);

                        if (startLatitude == 0.0f || startLongitude == 0.0f) {
                            SharedPreferences.Editor editor = jogPref.edit();
                            editor.putFloat("startLatitude", (float) latitude);
                            editor.putFloat("startLongitude", (float) longitude);
                            editor.apply();
                        }

                    }
                }, new IntentFilter(LocationService.BROADCAST_ACTION)
        );
    }


    @SuppressLint("ClickableViewAccessibility")
    public void pauseJog() {

        stopAllServices();
        SharedPreferences.Editor sharedPrefEditor = jogPref.edit();

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
                        handleButtonVisibilities();
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

        handleButtonVisibilities();

    }

    public void startOrResumeJog() {
        startAllServices();
        SharedPreferences.Editor sharedPrefEditor = jogPref.edit();

        sharedPrefEditor.putBoolean("jogIsPaused", false);
        sharedPrefEditor.putBoolean("jogIsOn", true);
        sharedPrefEditor.apply();

        handleButtonVisibilities();

        ObjectAnimator animator = ObjectAnimator.ofFloat(pauseButton, "translationX", 0f);
        animator.setDuration(500);

        animator.start();


    }


    public void stopAllServices() {
        getActivity().stopService(locationServiceIntent);
        getActivity().stopService(jogStatsServiceIntent);
        getActivity().stopService(stepTrackerServiceIntent);

    }

    public void startAllServices() {
        //Only start the services if they are not already running
        Context context = getContext();
        if (!LocationService.isServiceRunning() && context != null) {
            context.startService(locationServiceIntent);
        }
        if (!JogStatsService.isServiceRunning()) {
            context.startService(jogStatsServiceIntent);
        }
        if (!StepTrackerService.isServiceRunning()) {
            context.startService(stepTrackerServiceIntent);
        }

    }



    public AlertDialog eventDurationDialog() {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(getContext());
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
        TextView tvText = new TextView(getContext());
        tvText.setText("Hold To End Jog");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        Drawable runIcon = getActivity().getDrawable(R.drawable.ic_walk);
        TextView topText = new TextView(getContext());
        topText.setText("");
        topText.setCompoundDrawablesWithIntrinsicBounds(null, runIcon, null, null);
        if (Build.VERSION.SDK_INT > 22) {
            topText.setCompoundDrawableTintList(ColorStateList.valueOf(Color.parseColor("#592DEA")));
        }
        topText.setScaleX(2);
        topText.setScaleY(2);
        topText.setPadding(0, 50, 0, 0);
        topText.setLayoutParams(llParam);

        if (ll.getParent() != null) {
            ((ViewGroup) ll.getParent()).removeView(ll);
        }
        ll.addView(topText);
        ll.addView(buttonPressedDurationProgressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        return dialog;
    }


    public ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
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

        SharedPreferences.Editor sharedPrefEditor = jogPref.edit();
        sharedPrefEditor.putBoolean("jogIsPaused", false);
        sharedPrefEditor.putBoolean("jogIsOn", false);
        sharedPrefEditor.apply();

        handleButtonVisibilities();

        SharedPreferences authPref = getContext().getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        int userId = authPref.getInt("userId", 0);
        int duration = jogPref.getInt("duration", 0);
        int distance = jogPref.getInt("distance", 0);
        Float calories = jogPref.getFloat("calories", 0);
        Float startLatitude = jogPref.getFloat("startLatitude", 0.0f);
        Float endLatitude = jogPref.getFloat("endLatitude", 0.0f);
        Float startLongitude = jogPref.getFloat("startLongitude", 0.0f);
        Float endLongitude = jogPref.getFloat("endLongitude", 0.0f);
        Float averageSpeed = jogPref.getFloat("averageSpeed", 0);
        Float maxSpeed = jogPref.getFloat("maxSpeed", 0);
        int averagePace = jogPref.getInt("averagePace", 0);
        int maxPace = jogPref.getInt("maxPace", 0);
        float hydration = jogPref.getFloat("hydration", 0f);
        float maxAltitude = jogPref.getFloat("maxAltitude", 0f);
        float minAltitude = jogPref.getFloat("minAltitude", 0f);
        int totalAscent = jogPref.getInt("totalAscent", 0);
        int totalDescent = jogPref.getInt("totalDescent", 0);

        String coordinates = jogPref.getString("coordinates", new ArrayList<>().toString());
        String spds = jogPref.getString("speeds", new ArrayList<>().toString());
        String pcs = jogPref.getString("paces", new ArrayList<>().toString());
        String lapsString = jogPref.getString("laps", new ArrayList<>().toString());

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
        Date now = new Date();
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
            jog.put("created_at", new SimpleDateFormat(DATE_FORMAT_PATTERN).format(now));

            String jogStr = jog.toString();

            Bundle bundle = new Bundle();
            bundle.putString("jog", jogStr);
            bundle.putBoolean("canGoBack", false);
            bundle.putBoolean("shouldSave", true);

            Navigation.findNavController(getActivity(), R.id.main_content)
                    .navigate(R.id.action_startFragment_to_jogDetailFragment, bundle);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private String makeName() {
        String[] adjectives = {"Beautiful", "Serene", "Cool", "Exciting", "Fine", "Lovely", "Splendid", "Great", "Pleasant", "Nice"};
        int index = (int) Math.floor(Math.random() * adjectives.length);
        return adjectives[index] + " Jog";
    }


    private void onSwitchChanged(boolean isChecked) {
        //Send to Start fragment to change map to treadmill & vice-versa
        String jogType = isChecked ? "treadmill" : "outdoor";
        SharedPreferences.Editor editor = jogPref.edit();
        editor.putString("jogType", jogType);
        editor.apply();
        if (isChecked) {
            if (jogTreadmillView.getVisibility() == View.GONE) {
                ObjectAnimator animator = ObjectAnimator.ofInt(jogTreadmillView, "visibility", View.VISIBLE);
                animator.setDuration(500);
                animator.start();
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                jogStatsContainer.setBackground(getResources().getDrawable(R.drawable.treadmill_display_top));
            } else {
                jogStatsContainer.setBackground(getResources().getDrawable(R.drawable.treadmill_display_top, null));
            }

            jogStatsDuration.setTextColor(getResources().getColor(R.color.red));
            jogStatsDuration.setPadding(25, 10, 25,10);
            jogStatsDuration.setBackgroundColor(getResources().getColor(R.color.jogBlack));
            jogStatsDurationTitle.setTextColor(getResources().getColor(R.color.ghostWhite));

            jogStatsDistance.setTextColor(getResources().getColor(R.color.red));
            jogStatsDistance.setPadding(25, 10, 25,10);
            jogStatsDistance.setBackgroundColor(getResources().getColor(R.color.jogBlack));
            jogStatsDistanceTitle.setTextColor(getResources().getColor(R.color.ghostWhite));

            jogStatsPace.setTextColor(getResources().getColor(R.color.red));
            jogStatsPace.setPadding(25, 10, 25,10);
            jogStatsPace.setBackgroundColor(getResources().getColor(R.color.jogBlack));
            jogStatsPaceTitle.setTextColor(getResources().getColor(R.color.ghostWhite));

            jogStatsCalories.setTextColor(getResources().getColor(R.color.red));
            jogStatsCalories.setPadding(25, 10, 25,10);
            jogStatsCalories.setBackgroundColor(getResources().getColor(R.color.jogBlack));
            jogStatsCaloriesTitle.setTextColor(getResources().getColor(R.color.ghostWhite));


            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(40, 0, 40, 0);
            jogStatsConstraintLayout.setBackgroundColor(getResources().getColor(R.color.slate));
            jogStatsConstraintLayout.setLayoutParams(layoutParams);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                jogStatsConstraintLayout.setBackground(getResources().getDrawable(R.drawable.treadmill_display_top));
            } else {
                jogStatsConstraintLayout.setBackground(getResources().getDrawable(R.drawable.treadmill_display_top, null));
            }
            treadmillOuterContainer.setVisibility(View.VISIBLE);

        } else {
            if (jogTreadmillView.getVisibility() == View.VISIBLE) {
                ObjectAnimator animator = ObjectAnimator.ofInt(jogTreadmillView, "visibility", View.GONE);
                animator.setDuration(500);
                animator.start();
            }

            jogStatsContainer.setBackgroundColor(getResources().getColor(R.color.lightGhostWhite));

            jogStatsDuration.setTextColor(getResources().getColor(R.color.black));
            jogStatsDuration.setPadding(0, 0, 0,0);
            jogStatsDuration.setBackgroundColor(getResources().getColor(R.color.transparent));
            jogStatsDurationTitle.setTextColor(getResources().getColor(R.color.gray));

            jogStatsDistance.setTextColor(getResources().getColor(R.color.black));
            jogStatsDistance.setPadding(0, 0, 0,0);
            jogStatsDistance.setBackgroundColor(getResources().getColor(R.color.transparent));
            jogStatsDistanceTitle.setTextColor(getResources().getColor(R.color.gray));

            jogStatsPace.setTextColor(getResources().getColor(R.color.black));
            jogStatsPace.setPadding(0, 0, 0,0);
            jogStatsPace.setBackgroundColor(getResources().getColor(R.color.transparent));
            jogStatsPaceTitle.setTextColor(getResources().getColor(R.color.gray));

            jogStatsCalories.setTextColor(getResources().getColor(R.color.black));
            jogStatsCalories.setPadding(0, 0, 0,0);
            jogStatsCalories.setBackgroundColor(getResources().getColor(R.color.transparent));
            jogStatsCaloriesTitle.setTextColor(getResources().getColor(R.color.gray));


            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 0);
            jogStatsConstraintLayout.setBackgroundColor(getResources().getColor(R.color.lightGhostWhite));
            jogStatsConstraintLayout.setLayoutParams(layoutParams);
            jogStatsConstraintLayout.setBackgroundColor(getResources().getColor(R.color.lightGhostWhite));
            treadmillOuterContainer.setVisibility(View.GONE);

        }
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



}
