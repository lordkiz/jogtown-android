package com.jogtown.jogtown.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.subfragments.MyGroupsListFragment;
import com.jogtown.jogtown.subfragments.SearchGroupsListFragment;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.adapters.GroupInfoMemberListRecyclerViewAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.network.PathUtils;
import com.jogtown.jogtown.utils.network.S3Uploader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    LinearLayout groupInfoButtonGroupLayout;
    ProgressBar groupInfoButtonGroupProgressBar;

    Button joinGroupButton;
    Button editGroupButton;
    Button leaveGroupButton;

    ImageView groupBackgroundImage;
    ImageView groupAvatar;
    TextView groupNameText;
    TextView groupTaglineText;
    TextView groupInfoInviteCodeText;
    LinearLayout ratingsPlaceholder;
    LinearLayout groupInfoShareContainer;
    Button shareInviteCodeButton;

    TextView groupInfoJoggersCount;
    TextView groupInfoDistanceCount;
    TextView groupInfoDurationCount;
    TextView groupInfoMembersPrivateNotAMemberText;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    JSONObject groupObject = GroupActivity.groupObject;
    List<JSONObject> groupMembers;

    AdView mAdView;
    SharedPreferences settingsPref;

    public GroupInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupInfoFragment newInstance(String param1, String param2) {
        GroupInfoFragment fragment = new GroupInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_group_info, container, false);

        groupInfoButtonGroupLayout = view.findViewById(R.id.groupInfoButtonGroupLayout);
        groupInfoButtonGroupProgressBar = view.findViewById(R.id.groupInfoButtonGroupProgressBar);

        joinGroupButton = view.findViewById(R.id.joinGroupButton);
        try {
            if (GroupActivity.groupObject.getInt("required_coins") > 0) {
                String joinText = "  JOIN (" + Integer.toString(groupObject.getInt("required_coins")) + " coins)  ";
                joinGroupButton.setText(joinText);
            }
        } catch ( JSONException e) { }
        leaveGroupButton = view.findViewById(R.id.leaveGroupButton);
        editGroupButton = view.findViewById(R.id.editGroupButton);

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });

        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });

        editGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditGroupDialog(v);
            }
        });

        groupBackgroundImage = view.findViewById(R.id.group_background_image);
        groupAvatar = view.findViewById(R.id.group_avatar);
        groupNameText = view.findViewById(R.id.groupNameText);
        groupTaglineText = view.findViewById(R.id.groupTaglineText);
        groupInfoInviteCodeText = view.findViewById(R.id.groupInfoInviteCodeText);
        ratingsPlaceholder = view.findViewById(R.id.groupInfoBigRatingPlaceholder);

        groupInfoShareContainer = view.findViewById(R.id.groupInfoShareContainer);
        groupInfoShareContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInviteCode();
            }
        });

        shareInviteCodeButton = view.findViewById(R.id.shareInviteCodeButton);
        shareInviteCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInviteCode();
            }
        });

        groupInfoJoggersCount = view.findViewById(R.id.groupInfoJoggersCount);
        groupInfoDistanceCount = view.findViewById(R.id.groupInfoDistanceCount);
        groupInfoDurationCount = view.findViewById(R.id.groupInfoDurationCount);
        groupInfoMembersPrivateNotAMemberText = view.findViewById(R.id.groupInfoMembersPrivateNotAMemberText);

        recyclerView = view.findViewById(R.id.groupInfoMembersRecyclerView);


        setUpUI();

        if (GroupActivity.userIsAMember() & !GroupActivity.userIsOwner()) {
            leaveGroupButton.setVisibility(View.VISIBLE);
        }
        if (GroupActivity.userIsOwner()) {
            editGroupButton.setVisibility(View.VISIBLE);
        }
        if (!GroupActivity.userIsAMember()) {
            joinGroupButton.setVisibility(View.VISIBLE);
        }


        setUpMemberList(GroupActivity.groupObject);
        setUpMemberListRecyclerView();

        settingsPref = MainActivity.appContext.getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
        boolean showAds = settingsPref.getBoolean("showAds", true);
        if (showAds) {
            mAdView = view.findViewById(R.id.groupInfoAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        return view;
    }



    public void setUpUI() {
        try {
            groupNameText.setText(groupObject.getString("name"));
            groupTaglineText.setText(groupObject.getString("tagline"));
            groupInfoJoggersCount.setText(Integer.toString(groupObject.getInt("members_count")));
            groupInfoDistanceCount.setText(Conversions.displayKilometres(groupObject.getInt("total_distance")));
            groupInfoDurationCount.setText(Conversions.formatToHHMMSS(groupObject.getInt("total_duration")));
            groupInfoInviteCodeText.setText("Invite Code: " + groupObject.getString("invite_code"));

            Picasso.get().load(Uri.parse(groupObject.getString("group_avatar")))
                    .resize(200, 200)
                    .placeholder(R.drawable.progress_animation)
                    .into(groupAvatar);

            if (!GroupActivity.groupObject.isNull("background_image")) {
                Picasso.get().load(Uri.parse(groupObject.getString("background_image")))
                        .fit()
                        .placeholder(R.drawable.progress_animation)
                        .centerCrop()
                        .into(groupBackgroundImage);
            }

            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            if (ratingsPlaceholder.getChildCount() == 0) {
                switch (groupObject.getInt("rating")) {
                    case 5:
                        layoutInflater.inflate(R.layout.five_star_large_layout, ratingsPlaceholder);
                        break;
                    case 4:
                        layoutInflater.inflate(R.layout.four_star_large_layout, ratingsPlaceholder);
                        break;
                    case 3:
                        layoutInflater.inflate(R.layout.three_star_large_layout, ratingsPlaceholder);
                        break;
                    case 2:
                        layoutInflater.inflate(R.layout.two_star_large_layout, ratingsPlaceholder);
                        break;
                    case 1:
                        layoutInflater.inflate(R.layout.one_star_large_layout, ratingsPlaceholder);
                        break;
                    default:
                        layoutInflater.inflate(R.layout.zero_star_large_layout, ratingsPlaceholder);

                }
            }

            if (groupObject.getBoolean("public") || GroupActivity.userIsAMember()) {
                groupInfoShareContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                groupInfoMembersPrivateNotAMemberText.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {

        }
    }

    public void shareInviteCode() {
        try {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Come jog with " +
                    groupObject.getString("name") +
                    " on Jogtown using this invite code: "
                    + groupObject.getString("invite_code");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invite Code");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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


    public void joinGroup() {
        try {
            groupInfoButtonGroupProgressBar.setVisibility(View.VISIBLE);
            groupInfoButtonGroupLayout.setVisibility(View.GONE);

            final SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
            final int requiredCoins = GroupActivity.groupObject.getInt("required_coins");
            final int userCoins = authPref.getInt("coins", 0);
            int userId = authPref.getInt("userId", 0);

            if (userCoins >= requiredCoins) {
                JSONObject groupMembershipObj = new JSONObject();
                groupMembershipObj.put("user_id", userId);
                groupMembershipObj.put("group_id", GroupActivity.groupId);

                String payload = groupMembershipObj.toString();
                String url = getString(R.string.root_url) + "/v1/group_memberships";

                MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
                    @Override
                    public void onFinishRequest(Object result) {
                        try {
                            JSONObject resultObj = new JSONObject(result.toString());
                            int statusCode = resultObj.getInt("statusCode");
                            final JSONObject membership = new JSONObject(resultObj.getString("body"));
                            if (statusCode == 200) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        joinGroupButton.setVisibility(View.GONE);
                                        groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
                                        groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);
                                        leaveGroupButton.setVisibility(View.VISIBLE);

                                        int currentCoinsForUser = userCoins - requiredCoins;
                                        SharedPreferences.Editor editor = authPref.edit();
                                        editor.putInt("coins", currentCoinsForUser);
                                        editor.apply();

                                        SearchGroupsListFragment.getGroups("");
                                        MyGroupsListFragment.getMyGroups();

                                        groupObject = GroupActivity.groupObjectAfterUserJoined(membership);
                                        setUpUI();
                                        setUpMemberList(groupObject);
                                        adapter.notifyDataSetChanged();

                                        Toast.makeText(getContext(), "Successfully joined group", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else { Toast.makeText(getContext(), "Error occurred during operation", Toast.LENGTH_SHORT).show();}
                        } catch (JSONException e) {
                            e.printStackTrace();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
                                    groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), "JSONException Error occurred during operation", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                };

                NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));

            } else {
                groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
                groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setCancelable(true)
                        .setMessage("You do not have enough coins to join this group")
                        .setPositiveButton("Get Coins", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO = Get coins
                            }
                        })
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();

            }
        } catch (JSONException e) {
            groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
            groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);

        } catch (NullPointerException e) {
            groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
            groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);

        }
    }






    public void leaveGroup() {
        groupInfoButtonGroupProgressBar.setVisibility(View.VISIBLE);
        groupInfoButtonGroupLayout.setVisibility(View.GONE);

        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        final int userId = authPref.getInt("userId", 0);

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject resultObj = new JSONObject(result.toString());
                    int statusCode = resultObj.getInt("statusCode");
                    if (statusCode < 399) {
                        //Whatever
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                GroupActivity.refreshGroup();
                                leaveGroupButton.setVisibility(View.GONE);
                                joinGroupButton.setVisibility(View.VISIBLE);
                                groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
                                groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);
                                SearchGroupsListFragment.getGroups("");
                                MyGroupsListFragment.getMyGroups();

                                JSONObject grpObj = GroupActivity.groupObjectAfterUserLeft(userId);
                                setUpMemberList(grpObj);
                                adapter.notifyDataSetChanged();

                                try {
                                    getActivity().onBackPressed();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
                                groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(), "Error occurred during operation", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            String url = getString(R.string.root_url) + "/v1/leave_group";
            JSONObject payloadObj = new JSONObject();
            payloadObj.put("group_id", GroupActivity.groupId);
            payloadObj.put("user_id", userId);

            String payload = payloadObj.toString();

            NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));
        } catch (JSONException e) {
            groupInfoButtonGroupProgressBar.setVisibility(View.GONE);
            groupInfoButtonGroupLayout.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Error occurred during operation", Toast.LENGTH_SHORT).show();

        }

    }


    public void setUpMemberList(JSONObject groupObject) {
        groupMembers = new ArrayList<>();
        try {
            JSONArray jsonArray = groupObject.getJSONArray("group_memberships");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                groupMembers.add(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setUpMemberListRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupInfoMemberListRecyclerViewAdapter(this.groupMembers, false);

        recyclerView.setAdapter(adapter);
    }





    /*
    *
    *
    *
    *
    *
    *
    * EDITING OF GROUP BELOW. BASICALLY SIMILAR STUFF FROM MYGROUPSLISTFRAGMENT
    *
    *
    *
    *
    *
    *
    * */



    String editGroupAvatarStr;
    Uri editGroupAvatarUri;
    String editBackgroundImageStr;
    Uri editBackgroundImageUri;
    int totalFilesToUpload = 0;
    int uploaded = 0;

    Dialog editGroupDialog;

    FrameLayout editBackgroundImageLayout;
    FrameLayout editGroupAvatarLayout;
    ImageView editBackgroundImageView;
    ImageView editGroupAvatarImageView;
    EditText editGroupNameEditText;
    EditText editGroupTagLineEditText;
    EditText editRequiredCoinsEditText;
    RadioGroup editRadioGroup;
    RadioButton editPublicRadioButton;
    RadioButton editPrivateRadioButton;
    Button editSaveGroupButton;
    ProgressBar editProgressBar;

    boolean editGroupAvatarClicked = false; //Just to track with picture field was clicked




    public void showEditGroupDialog(View v) {
        Dialog dialog = new Dialog(v.getContext(), android.R.style.Theme_NoTitleBar);
        dialog.setContentView(R.layout.edit_group_layout);
        //Find them all!
        editBackgroundImageLayout = dialog.findViewById(R.id.edit_group_choose_background_image_layout);
        editGroupAvatarLayout = dialog.findViewById(R.id.edit_group_choose_group_avatar_layout);
        editGroupNameEditText = dialog.findViewById(R.id.edit_group_name);
        editGroupTagLineEditText = dialog.findViewById(R.id.edit_group_tagline);
        editRequiredCoinsEditText = dialog.findViewById(R.id.edit_group_required_coins);
        editRadioGroup = dialog.findViewById(R.id.edit_group_radio_group);
        editPublicRadioButton = dialog.findViewById(R.id.edit_group_public_radio_button);
        editPrivateRadioButton = dialog.findViewById(R.id.edit_group_private_radio_button);
        editSaveGroupButton = dialog.findViewById(R.id.edit_save_group_button);
        editProgressBar = dialog.findViewById(R.id.edit_group_progress_bar);

        editBackgroundImageView = dialog.findViewById(R.id.edit_background_selected_image_view);
        editGroupAvatarImageView = dialog.findViewById(R.id.edit_group_avatar_selected_image_view);

        //Set them all
        try {
            Picasso.get()
                    .load(groupObject.getString("background_image"))
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .into(editBackgroundImageView);
            Picasso.get()
                    .load(groupObject.getString("group_avatar"))
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .into(editGroupAvatarImageView);
            editGroupNameEditText.setText(groupObject.getString("name"));
            editGroupTagLineEditText.setText(groupObject.getString("tagline"));
            boolean isPublicGroup = groupObject.getBoolean("public");
            if (isPublicGroup) {
                editPublicRadioButton.setChecked(true);
                editPrivateRadioButton.setChecked(false);
            } else {
                editPublicRadioButton.setChecked(false);
                editPrivateRadioButton.setChecked(true);
            }
            editRequiredCoinsEditText.setText(Integer.toString(groupObject.getInt("required_coins")));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        editBackgroundImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editGroupAvatarClicked = false;
                attemptToGetImage("backgroundImage");
            }
        });

        editGroupAvatarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGroupAvatarClicked = true;
                attemptToGetImage("groupAvatar");

            }
        });

        editSaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupName = editGroupNameEditText.getText().toString();
                String tagline = editGroupTagLineEditText.getText().toString();
                String reqCoins = editRequiredCoinsEditText.getText().toString();

                if (isValidForm(groupName, tagline, reqCoins)) {
                    HashMap<String, Uri> forS3 = new HashMap<>();
                    if (editBackgroundImageUri != null) {
                        forS3.put("background_image_uri", editBackgroundImageUri);
                    }
                    if (editGroupAvatarUri != null) {
                        forS3.put("group_avatar_uri", editGroupAvatarUri);
                    }

                    if (forS3.size() > 0) {
                        totalFilesToUpload = forS3.size();
                        saveToS3(forS3);
                    } else {
                        //just save to backend directly
                        saveEditedGroup();
                    }

                }
            }
        });

        //Will be using RxJava, RxAndroid and Jake Wharton's binding for form validation
        //Create all observables on the required form fields
        Observable<Boolean> observable;

        Observable<String> groupNameObservable = RxTextView.textChanges(editGroupNameEditText)
                .skip(1).map(new Function<CharSequence, String>() {

                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });

        Observable<String> groupTaglineObservable = RxTextView.textChanges(editGroupTagLineEditText)
                .skip(1).map(new Function<CharSequence, String>() {

                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });

        Observable<String> requiredCoinsObservable = RxTextView.textChanges(editRequiredCoinsEditText)
                .map(new Function<CharSequence, String>() {

                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });


        observable = Observable.combineLatest(
                groupNameObservable, groupTaglineObservable, requiredCoinsObservable,
                new Function3<String, String, String, Boolean>() {
                    @Override
                    public Boolean apply(String s, String s2, String s3) throws Exception {

                        return isValidForm(s, s2, s3);
                    }
                });

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


        dialog.setCancelable(true);
        dialog.show();

        editGroupDialog = dialog;

    }




    boolean isValidForm(String groupName, String tagline, String requiredCoinsStr) {

        boolean groupNameIsValid = !groupName.isEmpty() && groupName.trim().length() > 2;

        if (!groupNameIsValid) {
            try {
                editGroupNameEditText.setError("Group name should be atleast 3 letters long");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        boolean taglineIsValid = !tagline.isEmpty() && tagline.trim().length() > 5;

        if (!taglineIsValid) {
            try {
                editGroupTagLineEditText.setError("Group tagline should be at least 5 letters long");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        int reqCoins = -1;
        try {
            reqCoins = Integer.parseInt(requiredCoinsStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        boolean requiredCoinsIsValid = reqCoins >= 0;
        if (!requiredCoinsIsValid) {
            try {
                editRequiredCoinsEditText.setError("Fee should be a number, atleast 0");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return groupNameIsValid && taglineIsValid && requiredCoinsIsValid;
    }





    public void saveToS3(HashMap<String, Uri> hashMap) {
        editProgressBar.setVisibility(View.VISIBLE);
        editSaveGroupButton.setVisibility(View.GONE);


        if (hashMap.containsKey("background_image_uri")) {
            final String bgImagePath = getFilePathfromURI(hashMap.get("background_image_uri"));

            S3Uploader.s3UploadInterface bgUploadInterface = new S3Uploader.s3UploadInterface() {
                @Override
                public void onUploadSuccess(String response) {
                    String[] resArr = response.split(" ");
                    if (resArr[0].equals("success")) {
                        editBackgroundImageStr = resArr[1];
                        uploaded++;
                        if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                            saveEditedGroup();
                        } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                            saveEditedGroup();
                        }


                    }
                }

                @Override
                public void onUploadError(String response) {
                    uploaded++;
                    if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                        saveEditedGroup();
                    } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                        saveEditedGroup();
                    }

                }
            };

            S3Uploader bgImageUploader = new S3Uploader(getContext(), "group-background-images/", bgUploadInterface);
            bgImageUploader.upload(bgImagePath);
        }

        if (hashMap.containsKey("group_avatar_uri")) {
            final String grpImagePath = getFilePathfromURI(hashMap.get("group_avatar_uri"));

            S3Uploader.s3UploadInterface groupAvatarUploadInterface = new S3Uploader.s3UploadInterface() {
                @Override
                public void onUploadSuccess(String response) {
                    String[] resArr = response.split(" ");
                    if (resArr[0].equals("success")) {
                        editGroupAvatarStr = resArr[1];
                        uploaded++;
                        if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                            saveEditedGroup();
                        } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                            saveEditedGroup();
                        }

                    }
                }

                @Override
                public void onUploadError(String response) {
                    uploaded++;
                    if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                        saveEditedGroup();
                    } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                        saveEditedGroup();
                    }
                }
            };

            S3Uploader groupAvatarUploader = new S3Uploader(getContext(), "group-avatars/", groupAvatarUploadInterface);
            groupAvatarUploader.upload(grpImagePath);
        }

    }






    public void saveEditedGroup() {
        editProgressBar.setVisibility(View.VISIBLE);
        editSaveGroupButton.setVisibility(View.GONE);

        String url = getString(R.string.root_url) + "/v1/groups/" + Integer.toString(GroupActivity.groupId);
        JSONObject jsonObject = new JSONObject();

        try {
            SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences",Context.MODE_PRIVATE);

            String groupName = editGroupNameEditText.getText().toString();
            String tagline = editGroupTagLineEditText.getText().toString();
            int reqCoins = Integer.parseInt(editRequiredCoinsEditText.getText().toString());
            boolean isPublic = editPublicRadioButton.isChecked();
            int userId = authPref.getInt("userId", 0);


            jsonObject.put("name", groupName);
            jsonObject.put("tagline", tagline);
            jsonObject.put("required_coins", reqCoins);
            jsonObject.put("public", isPublic);
            jsonObject.put("user_id", userId);
            if (editGroupAvatarStr != null) {
                jsonObject.put("group_avatar", editGroupAvatarStr);
            }
            if (editBackgroundImageStr != null) {
                jsonObject.put("background_image", editBackgroundImageStr);
            }

            String payload = jsonObject.toString();

            MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
                @Override
                public void onFinishRequest(Object result) {
                    try {
                        JSONObject resultsObj = new JSONObject(result.toString());
                        final JSONObject groupObj = new JSONObject(resultsObj.getString("body"));
                        int statusCode = resultsObj.getInt("statusCode");

                        if (statusCode == 200) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    editProgressBar.setVisibility(View.GONE);
                                    editSaveGroupButton.setVisibility(View.VISIBLE);

                                    groupObject = groupObj;
                                    setUpUI();
                                    MyGroupsListFragment.getMyGroups();
                                    SearchGroupsListFragment.getGroups("");

                                    editGroupDialog.dismiss();
                                    Toast.makeText(getContext(), "Successfully edited Group", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else {
                            new  Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    editProgressBar.setVisibility(View.GONE);
                                    editSaveGroupButton.setVisibility(View.VISIBLE);
                                    editGroupDialog.dismiss();
                                    Toast.makeText(getContext(), "Failed to edit group", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                editProgressBar.setVisibility(View.GONE);
                                editSaveGroupButton.setVisibility(View.VISIBLE);
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
                    editProgressBar.setVisibility(View.GONE);
                    editSaveGroupButton.setVisibility(View.VISIBLE);
                }
            });
        } catch (NullPointerException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    editProgressBar.setVisibility(View.GONE);
                    editSaveGroupButton.setVisibility(View.VISIBLE);
                }
            });
        }

    }





    public void attemptToGetImage(String container) {
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
        if (editGroupAvatarClicked) {
            //load image into group avatar
            editGroupAvatarUri = imageUri;
            Picasso.get().load(imageUri)
                    .placeholder(R.drawable.progress_animation)
                    .fit().into(editGroupAvatarImageView);
        } else {
            editBackgroundImageUri = imageUri;
            Picasso.get().load(imageUri)
                    .placeholder(R.drawable.progress_animation)
                    .fit().into(editBackgroundImageView);
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
