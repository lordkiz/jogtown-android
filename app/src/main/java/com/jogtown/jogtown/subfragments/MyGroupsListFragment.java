package com.jogtown.jogtown.subfragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

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
import com.jogtown.jogtown.utils.adapters.MyGroupsListRecyclerAdapter;
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
 * {@link MyGroupsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyGroupsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyGroupsListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static Activity instance;

    private OnFragmentInteractionListener mListener;

    TextView myGroupsHeaderText;
    private static Button createGroupButton;


    private static ProgressBar progressBar;
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;

    private static List<Object> myGroups;

    private static boolean loading;

    static LinearLayout myGroupsListFragmentEmptyLayout;

    AdView mAdView;
    SharedPreferences settingsPref;

    public MyGroupsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyGroupsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyGroupsListFragment newInstance(String param1, String param2) {
        MyGroupsListFragment fragment = new MyGroupsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this.getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_groups_list, container, false);

        settingsPref = MainActivity.appContext.getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
        boolean showAds = settingsPref.getBoolean("showAds", true);
        if (showAds) {
            mAdView = view.findViewById(R.id.myGroupsListAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        createGroupButton = view.findViewById(R.id.createGroupButton);

        myGroupsListFragmentEmptyLayout = view.findViewById(R.id.my_groups_list_fragment_empty_layout);


        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupDialog(v);

            }
        });

        progressBar = view.findViewById(R.id.my_groups_fragment_progressbar);
        recyclerView = view.findViewById(R.id.my_groups_fragment_recyclerview);
        myGroups = new ArrayList<>();
        setUpRecyclerView();
        getMyGroups();
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

    public void setUpRecyclerView() {

        layoutManager = new LinearLayoutManager(getContext());

        //Need the layout in MyGroupsListRecyclerAdapter know which activity to navigate to
        //when clicked.
        //There are two possible Activities: GroupJogActivity or GroupActivity

        adapter = new MyGroupsListRecyclerAdapter(new GroupActivity(), myGroups);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    private static void showActivity() {

        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            createGroupButton.setVisibility(View.GONE);

        } else {
            progressBar.setVisibility(View.GONE);
            createGroupButton.setVisibility(View.VISIBLE);
        }

    }


    private static void showEmptyLayout() {
        if (myGroups.size() > 0) {
            myGroupsListFragmentEmptyLayout.setVisibility(View.GONE);
        } else {
            myGroupsListFragmentEmptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private static void hideEmptyLayout() {
        myGroupsListFragmentEmptyLayout.setVisibility(View.GONE);
    }




    public static void getMyGroups() {
        myGroups.clear();
        loading = true;
        showActivity();
        hideEmptyLayout();

        String url = MainActivity.appContext.getResources().getString(R.string.root_url) + "v1/user_groups";
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
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
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() > 0) {

                            for (int i = 0; i < jsonArray.length(); i++) {
                                myGroups.add(new JSONObject(jsonArray.get(i).toString()));
                            }
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //showButton();
                                adapter.notifyDataSetChanged();
                                showEmptyLayout();
                            }
                        });

                    } else if (statusCode > 399){ //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                showEmptyLayout();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(instance);
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
                            showEmptyLayout();
                        }
                    });
                }
            }
        };

        NetworkRequest.get(url, new MyUrlRequestCallback(onFinishRequest));
    }

    /*

    Everything beneath has to do with creating a group.
     */


    String groupAvatarStr;
    Uri groupAvatarUri;
    String backgroundImageStr;
    Uri backgroundImageUri;
    int totalFilesToUpload = 0;
    int uploaded = 0;

    Dialog createGroupDialog;

    FrameLayout backgroundImageLayout;
    FrameLayout groupAvatarLayout;
    ImageView backgroundImageView;
    ImageView groupAvatarImageView;
    EditText groupNameEditText;
    EditText groupTagLineEditText;
    EditText requiredCoinsEditText;
    RadioGroup radioGroup;
    RadioButton publicRadioButton;
    RadioButton privateRadioButton;
    Button saveGroupButton;

    boolean groupAvatarClicked = false; //Just to track with picture field was clicked




    public void showCreateGroupDialog(View v) {
        Dialog dialog = new Dialog(v.getContext(), android.R.style.Theme_NoTitleBar);
        dialog.setContentView(R.layout.create_group_layout);
        backgroundImageLayout = dialog.findViewById(R.id.create_group_choose_background_image_layout);
        groupAvatarLayout = dialog.findViewById(R.id.create_group_choose_group_avatar_layout);
        groupNameEditText = dialog.findViewById(R.id.create_group_name);
        groupTagLineEditText = dialog.findViewById(R.id.create_group_tagline);
        requiredCoinsEditText = dialog.findViewById(R.id.create_group_required_coins);
        radioGroup = dialog.findViewById(R.id.create_group_radio_group);
        publicRadioButton = dialog.findViewById(R.id.create_group_public_radio_button);
        privateRadioButton = dialog.findViewById(R.id.create_group_private_radio_button);
        saveGroupButton = dialog.findViewById(R.id.save_group_button);
        progressBar = dialog.findViewById(R.id.create_group_progress_bar);

        backgroundImageView = dialog.findViewById(R.id.background_selected_image_view);
        groupAvatarImageView = dialog.findViewById(R.id.group_avatar_selected_image_view);



        backgroundImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                groupAvatarClicked = false;
                attemptToGetImage("backgroundImage");
            }
        });

        groupAvatarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupAvatarClicked = true;
                attemptToGetImage("groupAvatar");

            }
        });

        saveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupName = groupNameEditText.getText().toString();
                String tagline = groupTagLineEditText.getText().toString();
                String reqCoins = requiredCoinsEditText.getText().toString();

                if (isValidForm(groupName, tagline, reqCoins)) {
                    HashMap<String, Uri> forS3 = new HashMap<>();
                    if (backgroundImageUri != null) {
                        forS3.put("background_image_uri", backgroundImageUri);
                    }
                    if (groupAvatarUri != null) {
                        forS3.put("group_avatar_uri", groupAvatarUri);
                    }

                    if (forS3.size() > 0) {
                        totalFilesToUpload = forS3.size();
                        saveToS3(forS3);
                    } else {
                        //just save to backend directly
                        saveCreatedGroup();
                    }

                }
            }
        });

        //Will be using RxJava, RxAndroid and Jake Wharton's binding for form validation
        //Create all observables on the required form fields
        Observable<Boolean> observable;

        Observable<String> groupNameObservable = RxTextView.textChanges(groupNameEditText)
                .skip(1).map(new Function<CharSequence, String>() {

                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });

        Observable<String> groupTaglineObservable = RxTextView.textChanges(groupTagLineEditText)
                .skip(1).map(new Function<CharSequence, String>() {

                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                });

        Observable<String> requiredCoinsObservable = RxTextView.textChanges(requiredCoinsEditText)
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

        createGroupDialog = dialog;

    }




    boolean isValidForm(String groupName, String tagline, String requiredCoinsStr) {

        boolean groupNameIsValid = !groupName.isEmpty() && groupName.trim().length() > 2;

        if (!groupNameIsValid) {
            try {
                groupNameEditText.setError("Group name should be atleast 3 letters long");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        boolean taglineIsValid = !tagline.isEmpty() && tagline.trim().length() > 5;

        if (!taglineIsValid) {
            try {
                groupTagLineEditText.setError("Group tagline should be at least 5 letters long");
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
                requiredCoinsEditText.setError("Fee should be a number, atleast 0");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return groupNameIsValid && taglineIsValid && requiredCoinsIsValid;
    }





    public void saveToS3(HashMap<String, Uri> hashMap) {
        progressBar.setVisibility(View.VISIBLE);
        saveGroupButton.setVisibility(View.GONE);


        if (hashMap.containsKey("background_image_uri")) {
            final String bgImagePath = getFilePathfromURI(hashMap.get("background_image_uri"));

            S3Uploader.s3UploadInterface bgUploadInterface = new S3Uploader.s3UploadInterface() {
                @Override
                public void onUploadSuccess(String response) {
                    String[] resArr = response.split(" ");
                    if (resArr[0].equals("success")) {
                        backgroundImageStr = resArr[1];
                        Log.i("backgroundImageStr str", backgroundImageStr);
                        uploaded++;
                        if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                            saveCreatedGroup();
                        } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                            saveCreatedGroup();
                        }


                    }
                }

                @Override
                public void onUploadError(String response) {
                    uploaded++;
                    if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                        saveCreatedGroup();
                    } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                        saveCreatedGroup();
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
                        groupAvatarStr = resArr[1];
                        uploaded++;
                        if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                            saveCreatedGroup();
                        } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                            saveCreatedGroup();
                        }

                    }
                }

                @Override
                public void onUploadError(String response) {
                    uploaded++;
                    if (totalFilesToUpload == 1 && totalFilesToUpload % uploaded == 0) {
                        saveCreatedGroup();
                    } else if (totalFilesToUpload > 1 && uploaded > 1 && totalFilesToUpload % uploaded == 0)  {
                        saveCreatedGroup();
                    }
                }
            };

            S3Uploader groupAvatarUploader = new S3Uploader(getContext(), "group-avatars/", groupAvatarUploadInterface);
            groupAvatarUploader.upload(grpImagePath);
        }

    }






    public void saveCreatedGroup() {
        progressBar.setVisibility(View.VISIBLE);
        saveGroupButton.setVisibility(View.GONE);

        String url = getString(R.string.root_url) + "/v1/groups";
        JSONObject jsonObject = new JSONObject();

        try {
            SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences",Context.MODE_PRIVATE);

            String groupName = groupNameEditText.getText().toString();
            String tagline = groupTagLineEditText.getText().toString();
            int reqCoins = Integer.parseInt(requiredCoinsEditText.getText().toString());
            boolean isPublic = publicRadioButton.isChecked();
            int userId = authPref.getInt("userId", 0);
            String groupAvatarString = null;
            String backgroundImageString = null;

            if (backgroundImageStr != null)  { backgroundImageString = backgroundImageStr; }
            if (groupAvatarStr == null) {
                String countryFlagRootDir = "https://jogtown-s3.s3.amazonaws.com/group-avatars/country/";
                String displayCountry = getActivity().getResources().getConfiguration().locale.getDisplayCountry();
                String[] countryArr = displayCountry.split(" ");
                String country = "";
                for (int i = 0; i < countryArr.length; i++) {
                    //This can easily be done with String.join. But hey we are targeting low APIs.
                    if (i == 0) {
                        //start of arr
                        country += countryArr[i].toLowerCase();
                    } else {
                        country += "-" + countryArr[i].toLowerCase();
                    }
                }
                groupAvatarString = countryFlagRootDir + country + ".png";

            } else { groupAvatarString = groupAvatarStr; }

            jsonObject.put("name", groupName);
            jsonObject.put("tagline", tagline);
            jsonObject.put("required_coins", reqCoins);
            jsonObject.put("public", isPublic);
            jsonObject.put("user_id", userId);
            jsonObject.put("group_avatar", groupAvatarString);
            jsonObject.put("background_image", backgroundImageString);

            String payload = jsonObject.toString();

            MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
                @Override
                public void onFinishRequest(Object result) {
                    try {
                        JSONObject resultsObj = new JSONObject(result.toString());
                        final JSONObject groupObj = resultsObj.getJSONObject("body");
                        int statusCode = resultsObj.getInt("statusCode");

                        if (statusCode == 200) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    saveGroupButton.setVisibility(View.VISIBLE);

                                    createGroupDialog.dismiss();
                                    Toast.makeText(getContext(), "Successfully created Group", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getContext(), GroupActivity.class);
                                    intent.putExtra("group", groupObj.toString());
                                    startActivity(intent);
                                }
                            });

                        } else {
                            new  Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    saveGroupButton.setVisibility(View.VISIBLE);
                                    createGroupDialog.dismiss();
                                    Toast.makeText(getContext(), "Failed to create group", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                saveGroupButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            };

            NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));

        } catch (JSONException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    saveGroupButton.setVisibility(View.VISIBLE);
                }
            });
        } catch (NullPointerException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    saveGroupButton.setVisibility(View.VISIBLE);
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
        if (groupAvatarClicked) {
            //load image into group avatar
            groupAvatarUri = imageUri;
            Picasso.get().load(imageUri)
                    .placeholder(R.drawable.progress_animation)
                    .fit().into(groupAvatarImageView);
        } else {
            backgroundImageUri = imageUri;
            Picasso.get().load(imageUri).fit()
                    .placeholder(R.drawable.progress_animation)
                    .into(backgroundImageView);
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
