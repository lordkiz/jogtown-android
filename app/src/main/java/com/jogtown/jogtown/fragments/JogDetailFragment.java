package com.jogtown.jogtown.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.jogtown.jogtown.models.Jog;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.adapters.LapsRecyclerAdapter;
import com.jogtown.jogtown.utils.database.AppDatabase;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.ChartAxisFormatter;
import com.jogtown.jogtown.utils.ui.MyTypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JogDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JogDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JogDetailFragment extends Fragment implements OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;

    Boolean shouldSave = false;

    Boolean loading = false;

    GoogleMap mMap;
    LottieAnimationView jogDetailTreadmill;
    LineChart paceAnalysisChart;
    LineChart speedAnalysisChart;
    TextView durationTextView;
    TextView distanceTextView;
    TextView averagePaceTextView;
    TextView maxPaceTextView;
    TextView averageSpeedTextView;
    TextView maxSpeedTextView;
    TextView caloriesTextView;
    TextView hydrationTextView;
    TextView jogDate;
    ProgressBar progressBar;

    JSONObject jog;

    AppDatabase database;

    List<JSONObject> laps = new ArrayList<>();

    RecyclerView.LayoutManager lapsLayoutManager;
    RecyclerView.Adapter lapsAdapter;
    RecyclerView lapsRecyclerView;

    SharedPreferences settingsPref;
    SharedPreferences authPref;

    AdView mAdView;

    public JogDetailFragment() {
        // Required empty public constructor
    }

    public static JogDetailFragment newInstance(String param1, String param2) {
        JogDetailFragment fragment = new JogDetailFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jogString = getArguments().getString("jog");
            try {
                jog = new JSONObject(jogString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            shouldSave = getArguments().getBoolean("shouldSave");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jog_detail, container, false);

        try {
            ActionBar actionBar =  ((AppCompatActivity) getActivity()).getSupportActionBar();

            SpannableString spannableString = new SpannableString("Details");
            spannableString.setSpan(
                    new MyTypefaceSpan(getContext(), "fonts/baijamjuree_semi_bold.ttf"),
                    0,
                    spannableString.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            actionBar.setTitle(spannableString);

            actionBar.setDisplayHomeAsUpEnabled(true);

        } catch (NullPointerException e) {
            //
        }
        super.onCreate(savedInstanceState);

        settingsPref = getActivity().getSharedPreferences("SettingsPreferences", MODE_PRIVATE);

        authPref = getActivity().getSharedPreferences("AuthPreferences", MODE_PRIVATE);

        setUpJogObject();

        jogDetailTreadmill = view.findViewById(R.id.jog_details_treadmill);
        try {
            String jogType = jog.getString("jog_type");
            if (jogType == "treadmill") {
                jogDetailTreadmill.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {

        }
        progressBar = view.findViewById(R.id.jogDetailsProgressBar);
        paceAnalysisChart = view.findViewById(R.id.jogDetailsPaceAnalysisChart);
        speedAnalysisChart = view.findViewById(R.id.jogDetailsSpeedAnalysisChart);
        durationTextView = view.findViewById(R.id.jogDetailsDurationTextView);
        distanceTextView = view.findViewById(R.id.jogDetailsDistanceTextView);
        averagePaceTextView = view.findViewById(R.id.jogDetailsAveragePaceTextView);
        maxPaceTextView = view.findViewById(R.id.jogDetailsMaxPaceTextView);
        averageSpeedTextView = view.findViewById(R.id.jogDetailsAverageSpeedTextView);
        maxSpeedTextView = view.findViewById(R.id.jogDetailsMaxSpeedTextView);
        caloriesTextView = view.findViewById(R.id.jogDetailsCaloriesTextView);
        hydrationTextView = view.findViewById(R.id.jogDetailsHydrationTextView);
        jogDate = view.findViewById(R.id.jogDate);
        lapsRecyclerView = view.findViewById(R.id.jogDetailsLapsRecyclerView);

        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.jog_details_map_container, mMapFragment);
        fragmentTransaction.commit();

        database = Room.databaseBuilder(getContext(), AppDatabase.class, "jogtown_db")
                //.addMigrations(DBMigrations.MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        setJogStats();
        drawCharts();
        setUpRecyclerView();

        boolean showAds = authPref.getBoolean("premium", false);
        if (showAds) {
            mAdView = view.findViewById(R.id.jogDetailsAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        if (shouldSave) {
            saveJogStatsToBackend();
        }


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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void setUpJogObject() {
        try {
            JSONArray lapArr = jog.getJSONArray("laps");
            for (int i = 0; i < lapArr.length(); i++) {
                JSONObject lap = new JSONObject(lapArr.get(i).toString());
                laps.add(lap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUpRecyclerView() {
        lapsLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        lapsAdapter = new LapsRecyclerAdapter(laps);
        lapsRecyclerView.setLayoutManager(lapsLayoutManager);
        lapsRecyclerView.setAdapter(lapsAdapter);

    }



    private void drawCharts() {
        try {
            Gson gson = new Gson();
            List<Entry> paceEntries = new ArrayList<Entry>();
            List<Entry> speedEntries = new ArrayList<Entry>();

            JSONArray spds = jog.getJSONArray("speeds");
            JSONArray pcs = jog.getJSONArray("paces");

            Type speedsType = new TypeToken<List<Float>>() {
            }.getType();
            Type pacesType = new TypeToken<List<Integer>>() {
            }.getType();

            List<Integer> paces = gson.fromJson(pcs.toString(), pacesType);
            List<Float> speeds = gson.fromJson(spds.toString(), speedsType);

            int PACE_STEPPER = paces.size() / 10;
            if (PACE_STEPPER < 1) {
                PACE_STEPPER = 1;
            }
            for (int i = 0; i < paces.size(); i+=PACE_STEPPER) {
                paceEntries.add(new Entry(i / 10, paces.get(i)/60 )); //convert pace to min/km
            }

            int SPEED_STEPPER = speeds.size() / 10;
            if (SPEED_STEPPER < 1) {
                SPEED_STEPPER = 1;
            }
            for (int i = 0; i < speeds.size(); i+=SPEED_STEPPER) {
                speedEntries.add(new Entry(i / 10, speeds.get(i)));
            }

            LineDataSet paceDataSet = new LineDataSet(paceEntries, "Paces");
            LineDataSet speedDataSet = new LineDataSet(speedEntries, "Speeds");

            paceDataSet.setDrawValues(false);
            paceDataSet.setCircleRadius(0.2f);
            paceDataSet.setDrawCircles(false);
            paceDataSet.setLineWidth(0);
            paceDataSet.setDrawFilled(true);
            paceDataSet.setDrawHighlightIndicators(false);
            paceDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            try {
                paceDataSet.setFillDrawable(getActivity().getDrawable(R.drawable.green_linear_gradient));
            } catch (NullPointerException e) {
                //
            }

            speedDataSet.setDrawValues(false);
            speedDataSet.setCircleRadius(0.2f);
            speedDataSet.setDrawCircles(false);
            speedDataSet.setLineWidth(0);
            speedDataSet.setDrawFilled(true);
            speedDataSet.setDrawHighlightIndicators(false);
            speedDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            try {
                speedDataSet.setFillDrawable(getActivity().getDrawable(R.drawable.purple_linear_background));
            } catch (NullPointerException e) {
                //
            }

            LineData paceChartData = new LineData(paceDataSet);
            LineData speedChartData = new LineData(speedDataSet);

            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/baijamjuree_regular.ttf");

            paceAnalysisChart.setData(paceChartData);
            paceAnalysisChart.setDrawGridBackground(false);
            paceAnalysisChart.setNoDataTextTypeface(typeface);
            Description paceDesc = new Description();
            paceDesc.setText("");
            paceAnalysisChart.setDescription(paceDesc);
            paceAnalysisChart.getLegend().setEnabled(false);
            AxisBase paceAnalysisXAxis = paceAnalysisChart.getXAxis();
            AxisBase paceAnalysisYAxis = paceAnalysisChart.getAxisRight();
            paceAnalysisXAxis.setValueFormatter(new ChartAxisFormatter("", false));
            paceAnalysisYAxis.setValueFormatter(new ChartAxisFormatter(" min/km", true));

            paceAnalysisXAxis.setDrawGridLines(false);
            paceAnalysisXAxis.setDrawGridLinesBehindData(false);
            paceAnalysisXAxis.setDrawLimitLinesBehindData(false);
            paceAnalysisXAxis.setTypeface(typeface);
            //yaxis
            paceAnalysisYAxis.setDrawGridLines(false);
            paceAnalysisYAxis.setDrawGridLinesBehindData(false);
            paceAnalysisYAxis.setDrawLimitLinesBehindData(false);
            paceAnalysisYAxis.setTypeface(typeface);

            speedAnalysisChart.setData(speedChartData);
            speedAnalysisChart.setDrawGridBackground(false);
            speedAnalysisChart.setNoDataTextTypeface(typeface);
            Description speedDesc = new Description();
            speedDesc.setText("");
            speedAnalysisChart.setDescription(speedDesc);
            speedAnalysisChart.getLegend().setEnabled(false);
            AxisBase speedAnalysisXAxis = speedAnalysisChart.getXAxis();
            AxisBase speedAnalysisYAxis = speedAnalysisChart.getAxisRight();
            speedAnalysisXAxis.setValueFormatter(new ChartAxisFormatter("", false));
            speedAnalysisYAxis.setValueFormatter(new ChartAxisFormatter(" m/s", true));
            speedAnalysisXAxis.setDrawGridLines(false);
            speedAnalysisXAxis.setDrawGridLinesBehindData(false);
            speedAnalysisXAxis.setDrawLimitLinesBehindData(false);
            speedAnalysisXAxis.setTypeface(typeface);
            //yaxis
            speedAnalysisYAxis.setDrawGridLines(false);
            speedAnalysisYAxis.setDrawGridLinesBehindData(false);
            speedAnalysisYAxis.setDrawLimitLinesBehindData(false);
            speedAnalysisYAxis.setTypeface(typeface);

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

            String hydration = new BigDecimal(jog.getDouble("hydration"))
                    .setScale(2, RoundingMode.HALF_UP) + "L";
            hydrationTextView.setText(hydration);

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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("JogPreferences", MODE_PRIVATE);
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

                                try {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                    alertDialogBuilder
                                            .setCancelable(true)
                                            .setMessage(responseBody)
                                            .setTitle("Error!")
                                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialogBuilder.create().show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

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

//    @Override
//    public void onBackPressed() {
//        if (canGoBack) {
//            //if user can go back, then go back
//            super.onBackPressed();
//        } else {
//            //If you cannot go back
//            clearSharedPreferences();
//            Intent backIntent = new Intent(this, AppActivity.class);
//            startActivity(backIntent);
//            finish();
//        }
//    }
//
//
//    @Override
//    public boolean onSupportNavigateUp(){
//        onBackPressed();
//        return true;
//    }

}
