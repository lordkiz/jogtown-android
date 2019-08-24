package com.jogtown.jogtown.subfragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.fragments.GroupJogMembersFragment;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.services.JogStatsService;
import com.jogtown.jogtown.utils.services.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JogStatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JogStatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JogStatsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    TextView durationText;
    TextView distanceText;
    TextView paceText;
    TextView caloriesText;

    int totalDistance = 0;
    int duration = 0;
    float calories = 0.0f;
    float averageSpeed = 0;
    int averagePace = 0;
    float maxSpeed = 0;
    int maxPace = 0;
    int weight;

    List<List<Double>> coordinates = new ArrayList<>();
    List<Integer> paces = new ArrayList<>();
    List<Float> speeds = new ArrayList<>();
    List<JSONObject> laps = new ArrayList<>();

    SharedPreferences jogPref;
    public Boolean jogIsOn = false;
    public Boolean jogIsPaused = false;

    String jogType;

    Subscription subscription;

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("update Stats received", "true");
            int seconds = intent.getIntExtra("jogStatsServiceDuration", 0);
            if (seconds > 0) {
                duration = seconds;
            }
            int distance = (int) Math.round(intent.getDoubleExtra("totalDistance", 0));
            Double latitude = intent.getDoubleExtra("latitude", 0);
            Double longitude = intent.getDoubleExtra("longitude", 0);
            Float speed = intent.getFloatExtra("speed", 0);

            boolean jogIsOn = jogPref.getBoolean("jogIsOn", false);

            if (jogIsOn) {
                // I only want these updates when jog is On
                if (distance > 0) {
                    //we always get secs but rarely distance. So when distance changes we do major calculations.
                    totalDistance += distance;
                    Float sp = Conversions.calculateSpeed(totalDistance, duration);
                    int pa = Conversions.calculatePace(totalDistance, duration);
                    speeds.add(sp);
                    paces.add(pa);
                    maxSpeed = Collections.max(speeds);
                    maxPace = Collections.min(paces);
                    try {
                        int currentLap = totalDistance / 1000;
                        if (currentLap >= 1) {
                            JSONObject lapObj = new JSONObject();
                            lapObj.put("distance", currentLap*1000);
                            lapObj.put("duration", duration);

                            if (laps.size() < currentLap) {
                                // add that lap object
                                laps.add(lapObj);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (latitude != 0 && longitude != 0) {
                    List<Double> coord = new ArrayList<>();
                    coord.add(latitude);
                    coord.add(longitude);
                    coordinates.add(coord);
                }

                calories = Conversions.calculateCalories(totalDistance, duration, weight);
                saveJogStats();

                //current Pace and calories
                String paceString = Conversions.displayPace(totalDistance, duration);
                String caloriesString = Conversions.displayCalories(totalDistance, duration, weight);

                durationText.setText(Conversions.formatToHHMMSS(duration));

                if (duration % 10 == 0) {
                    //set every 10 seconds
                    distanceText.setText(Conversions.displayKilometres(totalDistance));
                    paceText.setText(paceString);
                    caloriesText.setText(caloriesString);
                }

                if (jogType.equals("group")) {
                    if (duration % 60 == 0) {
                        //broadcast every min - whenever we update groupMembership, it will be
                        //broadcast to the group
                        saveGroupMembershipStats();
                    }
                }
            }
        }
    };

    public JogStatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JogStatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JogStatsFragment newInstance(String param1, String param2) {
        JogStatsFragment fragment = new JogStatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_jog_stats, container, false);

        durationText = (TextView) view.findViewById(R.id.jogStatsDuration);
        distanceText = (TextView) view.findViewById(R.id.jogStatsDistance);
        paceText = (TextView) view.findViewById(R.id.jogStatsPace);
        caloriesText = (TextView) view.findViewById(R.id.jogStatsCalories);

        registerJogStatsBroadcastReceiver();
        registerLocationBroadcastReceiver();

        jogPref = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);
        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        jogIsOn = jogPref.getBoolean("jogIsOn", false);
        jogIsPaused = jogPref.getBoolean("jogIsPaused", false);
        jogType = jogPref.getString("jogType", "");
        weight = authPref.getInt("weight", 70);

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

        if (!jogIsOn) {
            //if jog is not on
            unRegisterJogStatsBroadcastReceiver();
            unRegisterLocationBroadcastReceiver();
        } else {
        }
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


    public void registerJogStatsBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(
                broadcastReceiver,
                new IntentFilter(JogStatsService.BROADCAST_ACTION)
            );
    }

    public void registerLocationBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(
                broadcastReceiver,
                new IntentFilter(LocationService.BROADCAST_ACTION)
        );
    }

    public void unRegisterJogStatsBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(broadcastReceiver);
    }

    public void unRegisterLocationBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(broadcastReceiver);
    }


    public void saveGroupMembershipStats() {
        GroupJogMembersFragment.saveGroupMembershipStats(totalDistance, duration, true);
    }


    public void saveJogStats() {
        SharedPreferences.Editor editor = jogPref.edit();
        Gson gson = new Gson();

        averagePace = getIntAverage(paces);
        averageSpeed = getFloatAverage(speeds);

        String coords = gson.toJson(coordinates);
        String speedsJson = gson.toJson(speeds);
        String pacesJson = gson.toJson(paces);

        Log.i("coordinates saved", coords);
        Log.i("speeds saved", speedsJson);
        Log.i("paces saved", pacesJson);
        Log.i("paces average", Integer.toString(averagePace));
        Log.i("paces max", Integer.toString(maxPace));

        editor.putString("coordinates", coords);
        editor.putString("speeds", speedsJson);
        editor.putString("paces", pacesJson);
        editor.putInt("duration", duration);
        editor.putInt("distance", totalDistance);
        editor.putFloat("calories", calories);
        editor.putFloat("averageSpeed", averageSpeed);
        editor.putInt("averagePace", averagePace);
        editor.putFloat("maxSpeed", maxSpeed);
        editor.putInt("maxPace", maxPace);
        editor.putString("laps", laps.toString());
        editor.apply();
    }

    private int getIntAverage(List<Integer> list) {
        int sum = 0;
        for (Integer number : list) {
            sum += number;
        }
        if (list.size() > 0) {
            return sum / list.size();
        }
        return sum;
    }

    private Float getFloatAverage(List<Float> list) {
        float sum = 0.0f;
        for (Float num : list) {
            sum += num;
        }
        if (list.size() > 0) {
            return sum / list.size();
        }
        return sum;
    }

}
