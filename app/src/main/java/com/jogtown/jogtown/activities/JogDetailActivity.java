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
    TextView jogDate;
    ProgressBar progressBar;

    JSONObject jog;

    AppDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jog_detail);

        intent = getIntent();

        setUpJogObject();

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
        jogDate = findViewById(R.id.jogDate);

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

    public void setUpJogObject() {
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("jog"));
            jog = object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void drawCharts() {
        try {
            Gson gson = new Gson();
            List<Entry> paceEntries = new ArrayList<Entry>();
            List<Entry> speedEntries = new ArrayList<Entry>();

            List<GradientColor> gradientColors = new ArrayList<>();

            gradientColors.add(new GradientColor(R.color.mediumGreen, R.color.extraLightGreen));

            JSONArray spds = jog.getJSONArray("speeds");
            JSONArray pcs = jog.getJSONArray("paces");

            Type speedsType = new TypeToken<List<Float>>() {
            }.getType();
            Type pacesType = new TypeToken<List<Integer>>() {
            }.getType();

            List<Integer> paces = gson.fromJson(pcs.toString(), pacesType);
            List<Float> speeds = gson.fromJson(spds.toString(), speedsType);


            for (int i = 0; i < paces.size(); i++) {
                paceEntries.add(new Entry((i + 1) * 10, paces.get(i)));
            }

            for (int i = 0; i < speeds.size(); i++) {
                speedEntries.add(new Entry((i + 1) * 10, speeds.get(i)));
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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void setJogStats() {
        try {
            String date = Conversions.formatDateTime(jog.getString("created_at"));
            jogDate.setText(date);

            String distance = Conversions.displayKilometres(jog.getInt("distance")) + " km";
            distanceTextView.setText(distance);

            String duration = Conversions.formatToHHMMSS(jog.getInt("duration"));
            durationTextView.setText(duration);

            String averagePace = Conversions.formatToHHMMSS(jog.getInt("average_pace"));
            averagePaceTextView.setText(averagePace);

            String averageSpeed = new BigDecimal(jog.getDouble("average_speed"))
                    .setScale(2, RoundingMode.HALF_UP) + " m/s";
            averageSpeedTextView.setText(averageSpeed);

            String maxPace = Conversions.formatToHHMMSS(jog.getInt("max_pace"));
            maxPaceTextView.setText(maxPace);

            String maxSpeed = new BigDecimal(jog.getDouble("max_speed"))
                    .setScale(2, RoundingMode.HALF_UP) + " m/s";
            maxSpeedTextView.setText(maxSpeed);

            String calories = new BigDecimal(jog.getDouble("calories"))
                    .setScale(2, RoundingMode.HALF_UP)
                    + " kcal";
            caloriesTextView.setText(calories);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void saveJogStatsToBackend() {
        loading = true;
        showActivity();

        if (Auth.isSignedIn()) {
            String url = getString(R.string.root_url) + "v1/runs";
            String payload = jog.toString();

            MyUrlRequestCallback.OnFinishRequest onFinishRequest = createNetworkRequestsCallbackActions();
            MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);

            NetworkRequest.post(url, payload, requestCallback);

        }

    }




    public void saveJogToPhoneSqliteDB() {
        Log.i("Saving Jog to phone", "SQLITEDB");
//        Gson gson = new Gson();
//
//        Type coordType = new TypeToken<List<List<Double>>>() {}.getType();
//        Type pacesType = new TypeToken<List<Integer>>() {}.getType();
//        Type speedsType = new TypeToken<List<Float>>() {}.getType();

        try {
            String name = jog.getString("name");
            int duration = jog.getInt("duration");
            int distance = jog.getInt("distance");
            double calories = jog.getDouble("calories");
            double startLatitude = jog.getDouble("start_latitude");
            double startLongitude = jog.getDouble("start_longitude");
            double endLatitude = jog.getDouble("end_latitude");
            double endLongitude = jog.getDouble("end_longitude");
            double averageSpeed = jog.getDouble("average_speed");
            double maxSpeed = jog.getDouble("max_speed");
            int averagePace = jog.getInt("average_pace");
            int maxPace = jog.getInt("max_pace");
            int hydration = jog.getInt("hydration");
            double maxAltitude = jog.getDouble("max_altitude");
            double minAltitude = jog.getDouble("min_altitude");
            int totalAscent = jog.getInt("total_ascent");
            int totalDescent = jog.getInt("total_descent");
            String coords = jog.getString("coordinates");
            String spds = jog.getJSONArray("speeds").toString();
            String pcs = jog.getJSONArray("paces").toString();

    //        List<List<Double>> coordinates = gson.fromJson(coords, coordType);
    //        List<Integer> paces = gson.fromJson(pcs, pacesType);
    //        List<Float> speeds = gson.fromJson(spds, speedsType);

            Jog jog = new Jog.Builder()
                    .addName(name)
                    .addDuration(duration)
                    .addDistance(distance)
                    .addCalories((float) calories)
                    .addStartLatitude((float) startLatitude)
                    .addStartLongitude((float) startLongitude)
                    .addEndLatitude((float) endLatitude)
                    .addEndLongitude((float) endLongitude)
                    .addAverageSpeed((float) averageSpeed)
                    .addAveragePace(averagePace)
                    .addMaxSpeed((float) maxSpeed)
                    .addMaxPace(maxPace)
                    .addCoordinates(coords)
                    .addPaces(pcs)
                    .addSpeeds(spds)
                    .addHydration(hydration)
                    .addMaxAltitude((float) maxAltitude)
                    .addMinAltitude((float) minAltitude)
                    .addTotalAscent(totalAscent)
                    .addTotalDescent(totalDescent)
                    .build();


            JogDAO jogDAO = database.getJogDAO();
            jogDAO.insertJog(jog);
        } catch (SQLiteConstraintException s) {
            s.printStackTrace();
        } catch (SQLException s) {
            s.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
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
        String coords = null;
        try {
            coords = jog.getString("coordinates");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LatLng location = new LatLng(0,0);
        List<List<Double>> coordinates = gson.fromJson(coords, coordType);
        if (coordinates.size() > 0) {
            location = new LatLng(coordinates.get(0).get(0), coordinates.get(0).get(1));
        }
        if (mMap != null) {
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

    }

    public void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("JogPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
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
