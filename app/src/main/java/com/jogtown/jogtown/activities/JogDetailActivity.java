package com.jogtown.jogtown.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jogtown.jogtown.DAO.JogDAO;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.database.AppDatabase;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.models.Jog;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class JogDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    Intent intent;

    Boolean loading = false;

    GoogleMap mMap;
    LineChart paceAnalysisChart;
    LineChart speedAnalysisChart;
    TextView durationTextView;
    TextView distanceTextView;
    TextView averagePaceTextView;
    TextView maxPaceTextView;
    TextView averageSpeedTextView;
    TextView maxSpeedTextView;
    TextView caloriesTextView;
    ProgressBar progressBar;


    AppDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jog_detail);

        intent = getIntent();

        progressBar = findViewById(R.id.jogDetailsProgressBar);
        paceAnalysisChart = findViewById(R.id.jogDetailsPaceAnalysisChart);
        speedAnalysisChart = findViewById(R.id.jogDetailsSpeedAnalysisChart);
        durationTextView = findViewById(R.id.jogDetailsDurationTextView);
        distanceTextView = findViewById(R.id.jogDetailsDistanceTextView);
        averagePaceTextView = findViewById(R.id.jogDetailsAveragePaceTextView);
        maxPaceTextView = findViewById(R.id.jogDetailsMaxPaceTextView);
        averageSpeedTextView = findViewById(R.id.jogDetailsAverageSpeedTextView);
        maxSpeedTextView = findViewById(R.id.jogDetailsMaxSpeedTextView);
        caloriesTextView = findViewById(R.id.jogDetailsCaloriesTextView);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setTitle("Jog Details");
        }

        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.jog_details_map_container, mMapFragment);
        fragmentTransaction.commit();

        database = Room.databaseBuilder(this, AppDatabase.class, "jogtown_db")
                //.addMigrations(DBMigrations.MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        setJogStats();
        drawCharts();

        if (intent.getBooleanExtra("shouldSave", false)) {
            saveJogStatsToBackend();
        }


    }



    private void drawCharts() {
        Gson gson = new Gson();
        List<Entry> paceEntries = new ArrayList<Entry>();
        List<Entry> speedEntries = new ArrayList<Entry>();
        List<GradientColor> gradientColors = new ArrayList<>();

        gradientColors.add(new GradientColor(R.color.mediumGreen, R.color.extraLightGreen));

        String spds = intent.getStringExtra("speeds");
        String pcs = intent.getStringExtra("paces");

        Type speedsType = new TypeToken<List<Float>>() {}.getType();
        Type pacesType = new TypeToken<List<Integer>>() {}.getType();

        List<Integer> paces = gson.fromJson(pcs, pacesType);
        List<Float> speeds = gson.fromJson(spds, speedsType);


        for (int i = 0; i < paces.size(); i++) {
            paceEntries.add(new Entry((i+1)*10, paces.get(i)));
        }

        for (int i = 0; i < speeds.size(); i++) {
            speedEntries.add(new Entry((i+1)*10, speeds.get(i)));
        }

        LineDataSet paceDataSet = new LineDataSet(paceEntries, "Paces");
        LineDataSet speedDataSet = new LineDataSet(speedEntries, "Speeds");

        paceDataSet.setGradientColors(gradientColors);
        paceDataSet.setCircleRadius(0.2f);
        paceDataSet.setDrawCircles(true);
        paceDataSet.setLineWidth(2);
        paceDataSet.setDrawFilled(true);
        paceDataSet.setDrawHighlightIndicators(false);
        paceDataSet.setFillDrawable(getDrawable(R.drawable.green_linear_gradient));
        //paceDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        speedDataSet.setGradientColors(gradientColors);
        speedDataSet.setCircleRadius(0.2f);
        speedDataSet.setDrawCircles(true);
        speedDataSet.setLineWidth(2);
        speedDataSet.setDrawFilled(true);
        speedDataSet.setDrawHighlightIndicators(false);
        speedDataSet.setFillDrawable(getDrawable(R.drawable.purple_linear_background));
        //speedDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        LineData paceChartData = new LineData(paceDataSet);
        LineData speedChartData = new LineData(speedDataSet);

        paceAnalysisChart.setData(paceChartData);
        paceAnalysisChart.setDrawGridBackground(false);
        paceAnalysisChart.getAxisLeft().setDrawGridLines(false);
        paceAnalysisChart.getAxisRight().setDrawGridLines(false);

        speedAnalysisChart.setData(speedChartData);
        speedAnalysisChart.setDrawGridBackground(false);
        speedAnalysisChart.getAxisLeft().setDrawGridLines(false);
        speedAnalysisChart.getAxisRight().setDrawGridLines(false);
    }



    private void setJogStats() {
        String distance = Conversions.displayKilometres(intent.getIntExtra("distance", 0)) + " km";
        distanceTextView.setText(distance);

        String duration = Conversions.formatToHHMMSS(intent.getIntExtra("duration", 0));
        durationTextView.setText(duration);

        String averagePace = Conversions.formatToHHMMSS(intent.getIntExtra("averagePace", 0));
        averagePaceTextView.setText(averagePace);

        String averageSpeed = new BigDecimal(intent.getFloatExtra("averageSpeed", 0))
                .setScale(2, RoundingMode.HALF_UP) + " m/s";
        averageSpeedTextView.setText(averageSpeed);

        String maxPace = Conversions.formatToHHMMSS(intent.getIntExtra("maxPace", 0));
        maxPaceTextView.setText(maxPace);

        String maxSpeed = new BigDecimal(intent.getFloatExtra("maxSpeed", 0))
                .setScale(2, RoundingMode.HALF_UP) + " m/s";
        maxSpeedTextView.setText(maxSpeed);

        String calories = new BigDecimal(intent.getFloatExtra("calories", 0))
                .setScale(2, RoundingMode.HALF_UP)
                + " kcal";
        caloriesTextView.setText(calories);

    }


    public void saveJogStatsToBackend() {

        Log.i("response String", "saving to jogtown");
        loading = true;
        showActivity();

        Gson gson = new Gson();

        Type coordType = new TypeToken<List<List<Double>>>() {}.getType();
        Type pacesType = new TypeToken<List<Integer>>() {}.getType();
        Type speedsType = new TypeToken<List<Float>>() {}.getType();

        String name = makeName();
        int duration = intent.getIntExtra("duration", 0);
        int distance = intent.getIntExtra("distance", 0);
        float calories = intent.getFloatExtra("calories", 0);
        float startLatitude = intent.getFloatExtra("startLatitude", 0);
        float startLongitude = intent.getFloatExtra("startLongitude", 0);
        float endLatitude = intent.getFloatExtra("endLatitude", 0);
        float endLongitude = intent.getFloatExtra("endLongitude", 0);
        float averageSpeed = intent.getFloatExtra("averageSpeed", 0);
        float maxSpeed = intent.getFloatExtra("maxSpeed", 0);
        int averagePace = intent.getIntExtra("averagePace", 0);
        int maxPace = intent.getIntExtra("maxPace", 0);
        int hydration = intent.getIntExtra("hydration", 0);
        float maxAltitude = intent.getFloatExtra("maxAltitude", 0);
        float minAltitude = intent.getFloatExtra("minAltitude", 0);
        int totalAscent = intent.getIntExtra("hydration", 0);
        int totalDescent = intent.getIntExtra("hydration", 0);
        String coordinates = intent.getStringExtra("coordinates");
        String spds = intent.getStringExtra("speeds");
        String pcs = intent.getStringExtra("paces");

        //List<List<Double>> coordinates = gson.fromJson(coords, coordType);
        List<Integer> paces = gson.fromJson(pcs, pacesType);
        List<Float> speeds = gson.fromJson(spds, speedsType);

        if (Auth.isSignedIn()) {
            SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
            String uid = authPref.getString("uid", "");
            String client = authPref.getString("client", "");
            String accessToken = authPref.getString("accessToken", "");
            long expiry = authPref.getLong("expiry", 0);
            int userId = authPref.getInt("userId", 0);

            String url = getString(R.string.root_url) + "v1/runs";
            JSONObject payload = new JSONObject();
            String payloadString;
            try {

                payload.put("user_id", userId);
                payload.put("name", name);
                payload.put("duration", duration);
                payload.put("distance", distance);
                payload.put("calories", calories);
                payload.put("start_latitude", startLatitude);
                payload.put("end_latitude", endLatitude);
                payload.put("start_longitude", startLongitude);
                payload.put("end_longitude", endLongitude);
                payload.put("average_speed", averageSpeed);
                payload.put("average_pace", averagePace);
                payload.put("max_pace", maxPace);
                payload.put("max_speed", maxSpeed);
                payload.put("hydration", hydration);
                payload.put("max_altitude", maxAltitude);
                payload.put("min_altitude", minAltitude);
                payload.put("total_ascent", totalAscent);
                payload.put("total_descent", totalDescent);
                payload.put("coordinates", coordinates);

                JSONArray speedArr = new JSONArray(speeds);
                payload.put("speeds", speedArr);
                JSONArray paceArr = new JSONArray(paces);
                payload.put("paces", paceArr);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            payloadString = payload.toString();

            MyUrlRequestCallback.OnFinishRequest onFinishRequest = createNetworkRequestsCallbackActions();
            MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);

            NetworkRequest.post(url, payloadString, requestCallback);

        }

    }




    public void saveJogToPhoneSqliteDB() {
        Log.i("Saving Jog to phone", "SQLITEDB");
//        Gson gson = new Gson();
//
//        Type coordType = new TypeToken<List<List<Double>>>() {}.getType();
//        Type pacesType = new TypeToken<List<Integer>>() {}.getType();
//        Type speedsType = new TypeToken<List<Float>>() {}.getType();

        String name = makeName();
        int duration = intent.getIntExtra("duration", 0);
        int distance = intent.getIntExtra("distance", 0);
        float calories = intent.getFloatExtra("calories", 0);
        float startLatitude = intent.getFloatExtra("startLatitude", 0);
        float startLongitude = intent.getFloatExtra("startLongitude", 0);
        float endLatitude = intent.getFloatExtra("endLatitude", 0);
        float endLongitude = intent.getFloatExtra("endLongitude", 0);
        float averageSpeed = intent.getFloatExtra("averageSpeed", 0);
        float maxSpeed = intent.getFloatExtra("maxSpeed", 0);
        int averagePace = intent.getIntExtra("averagePace", 0);
        int maxPace = intent.getIntExtra("maxPace", 0);
        int hydration = intent.getIntExtra("hydration", 0);
        float maxAltitude = intent.getFloatExtra("maxAltitude", 0);
        float minAltitude = intent.getFloatExtra("minAltitude", 0);
        int totalAscent = intent.getIntExtra("totalAscent", 0);
        int totalDescent = intent.getIntExtra("totalDescent", 0);
        String coords = intent.getStringExtra("coordinates");
        String spds = intent.getStringExtra("speeds");
        String pcs = intent.getStringExtra("paces");

//        List<List<Double>> coordinates = gson.fromJson(coords, coordType);
//        List<Integer> paces = gson.fromJson(pcs, pacesType);
//        List<Float> speeds = gson.fromJson(spds, speedsType);

        Jog jog = new Jog.Builder()
                .addName(name)
                .addDuration(duration)
                .addDistance(distance)
                .addCalories(calories)
                .addStartLatitude(startLatitude)
                .addStartLongitude(startLongitude)
                .addEndLatitude(endLatitude)
                .addEndLongitude(endLongitude)
                .addAverageSpeed(averageSpeed)
                .addAveragePace(averagePace)
                .addMaxSpeed(maxSpeed)
                .addMaxPace(maxPace)
                .addCoordinates(coords)
                .addPaces(pcs)
                .addSpeeds(spds)
                .addHydration(hydration)
                .addMaxAltitude(maxAltitude)
                .addMinAltitude(minAltitude)
                .addTotalAscent(totalAscent)
                .addTotalDescent(totalDescent)
                .build();

        try {
            JogDAO jogDAO = database.getJogDAO();
            jogDAO.insertJog(jog);
        } catch (SQLiteConstraintException s) {
            s.printStackTrace();
        } catch (SQLException s) {
            s.printStackTrace();
        }

        clearSharedPreferences();

        /*List<Jog> jogs = jogDAO.getAllJogs();
        for (Jog j : jogs) {
            String jo = j.coordinates;
            System.out.println(j);
        }*/

    }


    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);

        }
    }

    public void updateMap() {
        Gson gson = new Gson();
        Type coordType = new TypeToken<List<List<Double>>>() {}.getType();
        String coords = intent.getStringExtra("coordinates");

        LatLng location = new LatLng(0,0);
        List<List<Double>> coordinates = gson.fromJson(coords, coordType);
        if (coordinates.size() > 0) {
            location = new LatLng(coordinates.get(0).get(0), coordinates.get(0).get(1));
        }
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

        if (coordinates.size() > 0) {
            PolylineOptions polylineOptions = new PolylineOptions();
            for (List<Double> latlng : coordinates) {
                polylineOptions.add(new LatLng(latlng.get(0), latlng.get(1)));
            }
            polylineOptions.color(Color.GREEN);
            polylineOptions.width(5);
            mMap.addPolyline(polylineOptions);
        }

    }

    public void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("JogPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public String makeName() {
        String[] adjectives = {"Beautiful", "Serene", "Cool", "Exciting", "Fine", "Lovely", "Splendid", "Great", "Pleasant", "Nice"};
        int index = (int) Math.floor(Math.random() * adjectives.length);
        return adjectives[index] + " Jog";
    }


    MyUrlRequestCallback.OnFinishRequest createNetworkRequestsCallbackActions() {
        return new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode == 200)  { //Some kind of success
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                try {
                                    JSONObject jsonObject = new JSONObject(responseBody);
                                    if (jsonObject.has("id")) {
                                        clearSharedPreferences();
                                    } else {
                                        saveJogToPhoneSqliteDB();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (statusCode > 399){ //400 and above errors

                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(JogDetailActivity.this);
                                alertDialogBuilder
                                        .setCancelable(true)
                                        .setMessage(responseBody)
                                        .setTitle("Error!");
                                alertDialogBuilder.create().show();

                            }
                        });
                        //Silently Save to Phone SQLITE DB for saving later
                        saveJogToPhoneSqliteDB();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loading = false;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showActivity();
                        }
                    });
                }
            }
        };
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            updateMap();
        }
    }

    @Override
    public void onBackPressed() {
        boolean canGoBack = intent.getBooleanExtra("canGoBack", false);
        if (canGoBack) {
            //if user can go back, then go back
            super.onBackPressed();
        } else {
            //If you cannot go back
            clearSharedPreferences();
            Intent backIntent = new Intent(this, AppActivity.class);
            startActivity(backIntent);
            finish();
        }
    }
}
