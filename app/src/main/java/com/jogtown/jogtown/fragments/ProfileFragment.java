package com.jogtown.jogtown.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.activities.SecondaryAppActivity;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.adapters.HistoryRecyclerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.network.PathUtils;
import com.jogtown.jogtown.utils.network.S3Uploader;
import com.jogtown.jogtown.utils.ui.PicassoCircle;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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

    ImageView profilePicture;
    TextView nameText;
    static TextView coinsProfileText;
    TextView oneKmDoneText;
    TextView threeKmDoneText;
    TextView fiveKmDoneText;
    TextView tenKmDoneText;
    TextView profileStatsCalorieText;
    TextView profileStatsDistanceText;
    TextView profileStatsJogsText;
    TextView profileStatsTotalTimeText;
    Button seeAllJogsButton;

    FrameLayout oneKmRecordView;
    FrameLayout threeKmRecordView;
    FrameLayout fiveKmRecordView;
    FrameLayout tenKmRecordView;

    CardView statsContainer;

    LinearLayout adStatus;
    LinearLayout coinBalance;

    AdView mAdView;

    SharedPreferences authPref;
    SharedPreferences settingsPref;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter mAdapter;

    List<Object> jogs = new ArrayList<>();

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

        try {
            ActionBar actionBar =  ((AppCompatActivity) getActivity()).getSupportActionBar();

            actionBar.setTitle("");
            actionBar.setElevation(0);
        } catch (NullPointerException e) {
            //
        }

        authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        nameText = view.findViewById(R.id.metaNameText);

        coinsProfileText = view.findViewById(R.id.coinsProfileText);

        progressBar = view.findViewById(R.id.userKmStatsFragmentProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        profilePicture = view.findViewById(R.id.profile_picture);

        oneKmDoneText = view.findViewById(R.id.one_km_done_text);
        threeKmDoneText = view.findViewById(R.id.three_km_done_text);
        fiveKmDoneText = view.findViewById(R.id.five_km_done_text);
        tenKmDoneText = view.findViewById(R.id.ten_km_done_text);

        profileStatsCalorieText = view.findViewById(R.id.profile_stats_calorie_text);
        profileStatsDistanceText = view.findViewById(R.id.profile_stats_distance_text);
        profileStatsJogsText = view.findViewById(R.id.profile_stats_jogs_text);
        profileStatsTotalTimeText = view.findViewById(R.id.profile_stats_total_time_text);

        oneKmRecordView = view.findViewById(R.id.one_km_record_view);
        threeKmRecordView = view.findViewById(R.id.three_km_record_view);
        fiveKmRecordView = view.findViewById(R.id.five_km_record_view);
        tenKmRecordView = view.findViewById(R.id.ten_km_record_view);

        statsContainer = view.findViewById(R.id.profile_stats_container);

        coinBalance = view.findViewById(R.id.profile_coins_balance_linear_layout);
        adStatus = view.findViewById(R.id.profile_ad_status_linear_layout);

        seeAllJogsButton = view.findViewById(R.id.profile_see_all_jogs_button);


        ImageButton editButton = view.findViewById(R.id.editProfileButton);

        seeAllJogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_historyFragment);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        statsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_historyFragment);
            }
        });

        coinBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_purchaseCoinsFragment);
            }
        });

        adStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SecondaryAppActivity.class);
                startActivity(intent);
            }
        });


//        buyCoinsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), PurchaseCoinsActivity.class);
//                startActivity(intent);
//                Navigation.findNavController(v).navigate(R.id.action_Profile_to_purchaseCoinsFragment);
//            }
//        });

        setUpMetaUI();

        getStats();

        setCoinText();

        settingsPref = MainActivity.appContext.getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
        boolean showAds = settingsPref.getBoolean("showAds", true);
        if (showAds) {
            mAdView = view.findViewById(R.id.profileAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            TextView adStatusProfileText = view.findViewById(R.id.adStatusProfileText);
            adStatusProfileText.setText("Ad free until 31 Aug 2020");
        }

        recyclerView = view.findViewById(R.id.profile_jogs_recycler_view);
        setUpRecyclerView();

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


    public static void setCoinText() {
        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        final String coins = Integer.toString(authPref.getInt("coins", 0)) + " coins";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                coinsProfileText.setText(coins);
            }
        });
    }


    private void setUpRecyclerView() {
        layoutManager = new LinearLayoutManager(MainActivity.appContext);
        mAdapter = new HistoryRecyclerAdapter(jogs);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private void setUpMetaUI() {
        String metaName = authPref.getString("name", "");
        Uri avatar = Uri.parse(authPref.getString("profilePicture", getString(R.string.default_profile_picture)));

        nameText.setText(metaName);
        try {
            Picasso.get().load(avatar)
                    .resize(400, 400)
                    .transform(new PicassoCircle())
                    .placeholder(R.drawable.progress_animation)
                    .into(profilePicture);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
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

                    JSONArray runs = data.getJSONArray("runs");
                    List<Object> lastThreeRuns = new ArrayList<>();
                    int maxFive = runs.length() <= 3 ? runs.length() : 3;
                    if (runs.length() > 0) {
                        for (int i = 0; i < maxFive; i++) {
                            lastThreeRuns.add(runs.get(i));
                        }
                        jogs.addAll(lastThreeRuns);
                    }

                    if (runs.length() > jogs.size()) {
                        seeAllJogsButton.setVisibility(View.VISIBLE);
                    }

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
                                        .setTitle("Error!")
                                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
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



    Uri newAvatarUri;
    String newAvatarStr;

    FrameLayout chooseAvatarLayout;
    ImageView avatarImageView;
    EditText editName;
    EditText editWeight;
    RadioButton maleGenderRadioButton;
    RadioButton femaleGenderRadioButton;
    Button saveButton;
    Button dismissButton;
    LinearLayout buttonContainer;
    ProgressBar editProfileProgressBar;
    Dialog editProfileDialog;


    public void showEditProfileDialog() {
        editProfileDialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        editProfileDialog.setContentView(R.layout.edit_profile_layout);
        editProfileDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        editProfileDialog.setCancelable(true);
        editProfileDialog.setCanceledOnTouchOutside(true);

        chooseAvatarLayout = editProfileDialog.findViewById(R.id.edit_choose_avatar_layout);
        avatarImageView = editProfileDialog.findViewById(R.id.edit_profile_selected_image_view);
        editName = editProfileDialog.findViewById(R.id.edit_name);
        editWeight = editProfileDialog.findViewById(R.id.edit_weight);
        maleGenderRadioButton = editProfileDialog.findViewById(R.id.edit_profile_male_radio_button);
        femaleGenderRadioButton = editProfileDialog.findViewById(R.id.edit_profile_female_radio_button);
        saveButton = editProfileDialog.findViewById(R.id.edit_profile_save_button);
        dismissButton = editProfileDialog.findViewById(R.id.edit_profile_dismiss_button);
        editProfileProgressBar = editProfileDialog.findViewById(R.id.edit_profile_progress_bar);
        buttonContainer = editProfileDialog.findViewById(R.id.edit_profile_buttons_container);

        String currName = authPref.getString("name", "");
        int currWeight = authPref.getInt("weight", 70);
        String currGender = authPref.getString("gender", "null");
        Uri profilePic = Uri.parse(authPref.getString("profilePicture", getActivity().getResources().getString(R.string.default_profile_picture)));

        editName.setText(currName);
        editWeight.setText(Integer.toString(currWeight));
        try {
            Picasso.get().load(profilePic)
                    .placeholder(R.drawable.progress_animation)
                    .fit().transform(new PicassoCircle()).into(avatarImageView);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (currGender.toLowerCase().equals("male") || currGender.toLowerCase().equals("null")) {
            maleGenderRadioButton.setChecked(true);
        } else {
            femaleGenderRadioButton.setChecked(true);
        }


        chooseAvatarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToGetImage();
            }
        });

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileDialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String weight = editWeight.getText().toString();

                if (isValidForm(name, weight)) {
                    if (newAvatarUri != null && !newAvatarUri.toString().isEmpty()) {
                        saveToS3();
                    } else {
                        saveEditedProfile();
                    }
                }
            }
        });

        //Will be using RxJava, RxAndroid and Jake Wharton's binding for form validation
        //Create all observables on the required form fields
        Observable<Boolean> observable;

        Observable<String> editNameObservable = RxTextView.textChanges(editName)
                .skip(1).map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });

        Observable<String> editWeightObservable = RxTextView.textChanges(editWeight)
                .skip(1).map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });

        observable = Observable.combineLatest(
                editNameObservable,
                editWeightObservable,
                new BiFunction<String, String, Boolean>() {
                    @Override
                    public Boolean apply(String s, String s2) throws Exception {
                        return isValidForm(s, s2);
                    }

                }
        );

        observable.subscribe(new DisposableObserver<Boolean>() {

            @Override
            public void onNext(Boolean aBoolean) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        editProfileDialog.show();

    }




    boolean isValidForm(String name, String weight) {

        boolean nameIsValid = !name.isEmpty() && name.trim().length() > 2;

        if (!nameIsValid) {
            try {
                editName.setError("Name should be at least 3 letters long");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        boolean weightIsValid = !weight.isEmpty() && Integer.parseInt(weight) >= 20;

        if (!weightIsValid) {
            try {
                editWeight.setError("Weight should be at least 20kg");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return nameIsValid && weightIsValid;
    }


    public void saveToS3() {
        editProfileProgressBar.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.GONE);

        final String avatarPath = getFilePathfromURI(newAvatarUri);

        S3Uploader.s3UploadInterface s3UploadInterface = new S3Uploader.s3UploadInterface() {
            @Override
            public void onUploadSuccess(String response) {
                String[] resArr = response.split(" ");
                if (resArr[0].equals("success")) {
                    newAvatarStr = resArr[1];
                    saveEditedProfile();
                }

            }

            @Override
            public void onUploadError(String response) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        editProfileProgressBar.setVisibility(View.GONE);
                        buttonContainer.setVisibility(View.VISIBLE);
                        editProfileDialog.dismiss();
                        Toast.makeText(getContext(), "Error uploading profile picture", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        S3Uploader s3Uploader = new S3Uploader(getContext(), "avatars/", s3UploadInterface);
        s3Uploader.upload(avatarPath);

    }




    public void saveEditedProfile() {
        editProfileProgressBar.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.GONE);

        String userId = Integer.toString(authPref.getInt("userId", 0));

        String url = getString(R.string.root_url) + "/v1/users/" + userId;
        JSONObject jsonObject = new JSONObject();

        try {

            String name = editName.getText().toString();
            int weight = Integer.parseInt(editWeight.getText().toString());
            String gender = maleGenderRadioButton.isChecked() ? "male" : "female";

            jsonObject.put("name", name);
            jsonObject.put("weight", weight);
            jsonObject.put("gender", gender);

            if (newAvatarStr != null) {
                jsonObject.put("profile_picture", newAvatarStr);
            }

            String payload = jsonObject.toString();

            MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
                @Override
                public void onFinishRequest(Object result) {
                    try {
                        final JSONObject resultsObj = new JSONObject(result.toString());
                        final JSONObject userObj = new JSONObject(resultsObj.getString("body"));
                        final JSONObject obj = new JSONObject();
                        obj.put("data", userObj);
                        final JSONObject headers = new JSONObject(resultsObj.getString("headers"));
                        int statusCode = resultsObj.getInt("statusCode");

                        if (statusCode == 200) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    editProfileProgressBar.setVisibility(View.GONE);
                                    buttonContainer.setVisibility(View.VISIBLE);
                                    if (Auth.login(obj.toString(), headers.toString())) { //Just to save results
                                        setUpMetaUI();
                                    }

                                    editProfileDialog.dismiss();
                                    Toast.makeText(getContext(), "Successfully edited profile", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else {
                            new  Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    editProfileProgressBar.setVisibility(View.GONE);
                                    buttonContainer.setVisibility(View.VISIBLE);

                                    editProfileDialog.dismiss();
                                    Toast.makeText(getContext(), "Failed to edit profile", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                editProfileProgressBar.setVisibility(View.GONE);
                                buttonContainer.setVisibility(View.VISIBLE);

                            }
                        });
                    }
                }
            };

            NetworkRequest.put(url, payload, new MyUrlRequestCallback(onFinishRequest));

        } catch (JSONException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    editProfileProgressBar.setVisibility(View.GONE);
                    buttonContainer.setVisibility(View.VISIBLE);

                }
            });
        } catch (NullPointerException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    editProfileProgressBar.setVisibility(View.GONE);
                    buttonContainer.setVisibility(View.VISIBLE);

                }
            });
        }

    }



    public void attemptToGetImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                chooseImage();
            } else {
                Log.v("", "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("", "Permission is granted");
            chooseImage();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pick A Picture"), 50);
    }

    public void onPictureSelect(Intent data) {
        Uri imageUri = data.getData();
        newAvatarUri = imageUri;
        try {
            Picasso.get().load(imageUri)
                    .placeholder(R.drawable.progress_animation)
                    .fit().transform(new PicassoCircle()).into(avatarImageView);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private String getFilePathfromURI(Uri selectedImageUri) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File file = new File(selectedImageUri.getPath());//create path from uri
            final String[] split = file.getPath().split(":");//split the path.
            String filePath = split[1];//assign it to a string(your choice).
            return filePath;
        } else {
            try {
                return PathUtils.getPath(getContext(), selectedImageUri);
            } catch (URISyntaxException e) {
                e.printStackTrace();

            }
        }*/
        //return selectedImageUri.toString();
        try {
            return PathUtils.getPath(getContext(), selectedImageUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return selectedImageUri.toString();
        }

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50) {
            if (resultCode == RESULT_OK) {
                onPictureSelect(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseImage();
        }
    }


}
