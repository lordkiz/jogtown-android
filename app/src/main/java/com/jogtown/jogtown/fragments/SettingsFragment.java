package com.jogtown.jogtown.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.MyTypefaceSpan;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    SharedPreferences settingsPref;
    SharedPreferences authPref;

    boolean allowNotification;
    boolean useKM;
    boolean useKG;
    boolean showAds;
    int settingsId;

    Switch disableNotificationsSwitch;

    Switch weightUnitSwitch;

    Switch distanceUnitSwitch;

    Button logoutButton;

    Button deleteAccountButton;

    TextView versionText;

    View thisView;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        thisView = view;
        getActivity().setTheme(R.style.AppTheme);
        Context context = getContext();
        try {
            ActionBar actionBar =  ((AppCompatActivity) getActivity()).getSupportActionBar();

            SpannableString spannableString = new SpannableString("Settings");
            spannableString.setSpan(
                    new MyTypefaceSpan(context, "fonts/baijamjuree_semi_bold.ttf"),
                    0,
                    spannableString.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            actionBar.setTitle(spannableString);
        } catch (NullPointerException e) {
            //
        }
        settingsPref = context.getSharedPreferences("SettingsPreferences", MODE_PRIVATE);
        authPref = context.getSharedPreferences("AuthPreferences", MODE_PRIVATE);

        allowNotification = settingsPref.getBoolean("allowNotification", true);
        useKM = settingsPref.getBoolean("km", true);
        useKG = settingsPref.getBoolean("kg", true);
        showAds = settingsPref.getBoolean("showAds", true);
        settingsId = settingsPref.getInt("settingsId", 0);

        disableNotificationsSwitch = view.findViewById(R.id.disableNotificationsSwitch);
        weightUnitSwitch = view.findViewById(R.id.weightUnitSwitch);
        distanceUnitSwitch = view.findViewById(R.id.distanceUnitSwitch);

        showAds = authPref.getBoolean("premium", false);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Settings");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        disableNotificationsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = disableNotificationsSwitch.isChecked();
                updateUserSettings("allow_notification", !isChecked);
            }
        });

        weightUnitSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = weightUnitSwitch.isChecked();
                updateUserSettings("kg", isChecked);
            }
        });

        distanceUnitSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = distanceUnitSwitch.isChecked();
                updateUserSettings("km", isChecked);
            }
        });


        logoutButton = view.findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDeleteAccount();
            }
        });

        versionText = view.findViewById(R.id.versionText);

        setUpUI();

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


    private void setUpUI() {
        disableNotificationsSwitch.setChecked(!allowNotification);
        weightUnitSwitch.setChecked(useKG);
        distanceUnitSwitch.setChecked(useKM);
        try {
            PackageManager manager = getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo("com.jogtown.jogtown", 0);

            String versionName = info.versionName;
            String versionNumber;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionNumber = Long.toString(info.getLongVersionCode());
            } else {
                versionNumber = Integer.toString(info.versionCode);
            }

            String vText = "version: " + versionName + " (" + versionNumber + ")";
            versionText.setText(vText);

        } catch (NullPointerException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void logout() {
        final Activity activity = this.getActivity();
        Auth.signOut(activity);
    }


    private void updateUserSettings(String key, Boolean value) {
        SharedPreferences.Editor editor = settingsPref.edit();
        editor.putBoolean(key, value);
        editor.apply();

        String url = getString(R.string.root_url) + "v1/settings/" + Integer.toString(settingsId);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String payload = jsonObject.toString();

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject jsonObj = new JSONObject(result.toString());
                    final int statusCode = jsonObj.getInt("statusCode");

                    if (statusCode < 300) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Settings Updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        NetworkRequest.put(url, payload, new MyUrlRequestCallback(onFinishRequest));

    }

    private void startDeleteAccount() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
                .setCancelable(true)
                .setMessage("Are you sure you want to delete your account? If you proceed all your data will be deleted forever.")
                .setTitle("DELETE ACCOUNT!")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteAccount();
                    }
                });
        alertDialogBuilder.create().show();

    }


    private void deleteAccount() {

        int userId = authPref.getInt("userId", 0);
        String url = getString(R.string.root_url) + "v1/users/" + userId;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String payload = jsonObject.toString();

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                logout();
                try {
                    JSONObject jsonObj = new JSONObject(result.toString());
                    final int statusCode = jsonObj.getInt("statusCode");

                    if (statusCode < 300) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                                logout();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        NetworkRequest.delete(url, payload, new MyUrlRequestCallback(onFinishRequest));
    }

}
