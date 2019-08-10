package com.jogtown.jogtown.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.NetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    ProgressBar progressBar;
    Boolean loading = false;

    TextView oneKmDoneText;
    TextView threeKmDoneText;
    TextView fiveKmDoneText;
    TextView tenKmDoneText;
    TextView profileStatsCalorieText;
    TextView profileStatsDistanceText;
    TextView profileStatsJogsText;
    TextView profileStatsTotalTimeText;

    FrameLayout oneKmRecordView;
    FrameLayout threeKmRecordView;
    FrameLayout fiveKmRecordView;
    FrameLayout tenKmRecordView;

    SharedPreferences sharedPreferences;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        String metaName = sharedPreferences.getString("name", "");

        TextView nameText = (TextView) view.findViewById(R.id.metaNameText);
        nameText.setText(metaName);

        progressBar = (ProgressBar) view.findViewById(R.id.userKmStatsFragmentProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        oneKmDoneText = (TextView) view.findViewById(R.id.one_km_done_text);
        threeKmDoneText = (TextView) view.findViewById(R.id.three_km_done_text);
        fiveKmDoneText = (TextView) view.findViewById(R.id.five_km_done_text);
        tenKmDoneText = (TextView) view.findViewById(R.id.ten_km_done_text);

        profileStatsCalorieText = (TextView) view.findViewById(R.id.profile_stats_calorie_text);
        profileStatsDistanceText = (TextView) view.findViewById(R.id.profile_stats_distance_text);
        profileStatsJogsText = (TextView) view.findViewById(R.id.profile_stats_jogs_text);
        profileStatsTotalTimeText = (TextView) view.findViewById(R.id.profile_stats_total_time_text);

        oneKmRecordView = (FrameLayout) view.findViewById(R.id.one_km_record_view);
        threeKmRecordView = (FrameLayout) view.findViewById(R.id.three_km_record_view);
        fiveKmRecordView = (FrameLayout) view.findViewById(R.id.five_km_record_view);
        tenKmRecordView = (FrameLayout) view.findViewById(R.id.ten_km_record_view);

        Button logoutButton = (Button) view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });


        getStats();


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


    public void getStats() {
        loading = true;
        showActivity();

        String url = getString(R.string.root_url) + "v1/user_stats";

        MyUrlRequestCallback.OnFinishRequest callbackActions = createNetworkRequestsCallbackActions();
        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(callbackActions);

        NetworkRequest.get(url, requestCallback);
    }



    public void updateStats(final JSONObject data) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    int totalDistance = data.getInt("total_distance");
                    int totalCalories = data.getInt("total_calories");
                    int totalTime = data.getInt("total_time");
                    int jogsCount = data.getInt("run_count");


                    profileStatsCalorieText.setText(Integer.toString(totalCalories));
                    profileStatsDistanceText.setText(Conversions.displayKilometres(totalDistance));
                    profileStatsJogsText.setText(Integer.toString(jogsCount));
                    profileStatsTotalTimeText.setText(Conversions.formatToHHMMSS(totalTime));


                    if (data.has("one_km") && !data.isNull("one_km")) {
                        String oneKmDate = Conversions.formatDateTime(data.getJSONObject("one_km").getString("created_at"));
                        oneKmDoneText.setText(oneKmDate);
                        oneKmRecordView.setBackgroundResource(R.drawable.stat_circle_green);
                    }
                    if (data.has("three_km") && !data.isNull("three_km")) {
                        String threeKmDate = Conversions.formatDateTime(data.getJSONObject("three_km").getString("created_at"));
                        threeKmDoneText.setText(threeKmDate);
                        threeKmRecordView.setBackgroundResource(R.drawable.stat_circle_green);
                    }
                    if (data.has("five_km") && !data.isNull("five_km")) {
                        String fiveKmDate = Conversions.formatDateTime(data.getJSONObject("five_km").getString("created_at"));
                        fiveKmDoneText.setText(fiveKmDate);
                        fiveKmRecordView.setBackgroundResource(R.drawable.stat_circle_green);
                    }
                    if (data.has("ten_km") && !data.isNull("ten_km")) {
                        String tenKmDate = Conversions.formatDateTime(data.getJSONObject("ten_km").getString("created_at"));
                        tenKmDoneText.setText(tenKmDate);
                        tenKmRecordView.setBackgroundResource(R.drawable.stat_circle_green);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);

        }
    }


    public void logout() {

        final Activity activity = this.getActivity();
        Auth.signOut(activity);
    }


    public MyUrlRequestCallback.OnFinishRequest createNetworkRequestsCallbackActions() {
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
                            }
                        });
                        JSONObject resBody = new JSONObject(responseBody);
                        updateStats(resBody);
                    } else if (statusCode > 399){ //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder
                                        .setCancelable(true)
                                        .setMessage(responseBody)
                                        .setTitle("Error!");
                                alertDialogBuilder.create().show();
                            }
                        });

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


}
