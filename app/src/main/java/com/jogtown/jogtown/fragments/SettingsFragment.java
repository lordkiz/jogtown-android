package com.jogtown.jogtown.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.PurchaseCoinsActivity;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

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
    boolean showAds;
    int settingsId;

    Switch disableNotificationsSwitch;
    LinearLayout removeAdsLayout;
    LinearLayout loadingRemoveAdsLayout;
    Button removeAdsButton;
    Button getCoinsButton;

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
        getActivity().setTheme(R.style.AppTheme);
        Context context = getContext();
        settingsPref = context.getSharedPreferences("SettingsPreferences", MODE_PRIVATE);
        authPref = context.getSharedPreferences("AuthPreferences", MODE_PRIVATE);

        allowNotification = settingsPref.getBoolean("allowNotification", true);
        showAds = settingsPref.getBoolean("showAds", true);
        settingsId = settingsPref.getInt("settingsId", 0);

        disableNotificationsSwitch = view.findViewById(R.id.disableNotificationsSwitch);

        removeAdsLayout = view.findViewById(R.id.removeAdsLayout);
        loadingRemoveAdsLayout = view.findViewById(R.id.loadingRemoveAdsLayout);

        removeAdsButton = view.findViewById(R.id.removeAdsButton);
        getCoinsButton = view.findViewById(R.id.getCoinsButton);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Settings");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        disableNotificationsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = disableNotificationsSwitch.isChecked();
                disableNotificationUpdate(isChecked);
            }
        });

        removeAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptRemoveAds();
            }
        });

        getCoinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PurchaseCoinsActivity.class);

                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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


    public void setUpUI() {
        if (!showAds) {
            removeAdsLayout.setVisibility(View.GONE);
        }

        disableNotificationsSwitch.setChecked(!allowNotification);
    }

    private void promptRemoveAds() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Remove Ads");
        alertDialogBuilder.setMessage("Remove Ads forever! This will cost you 100 coins.");

        alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeAds();
                dialog.dismiss();
            }
        });

        alertDialogBuilder.show();
    }


    private void removeAds() {
        final int userCoins = authPref.getInt("coins", 0);

        if (userCoins < 100) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle("Insufficient Coins");
            alertDialogBuilder.setMessage("You need 100 coins to remove ads forever.");

            alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.setPositiveButton("Get Coins", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), PurchaseCoinsActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.show();
        } else {
            removeAdsButton.setVisibility(View.GONE);
            loadingRemoveAdsLayout.setVisibility(View.VISIBLE);
            removeAdsLayout.setVisibility(View.VISIBLE);

            String url = getString(R.string.root_url) + "v1/settings/" + Integer.toString(settingsId);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("show_ads", false);
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
                                    Toast.makeText(getApplicationContext(), "Settings Updated", Toast.LENGTH_SHORT).show();
                                    removeAdsLayout.setVisibility(View.GONE);
                                    loadingRemoveAdsLayout.setVisibility(View.GONE);
                                    SharedPreferences.Editor settingsEditor = settingsPref.edit();
                                    settingsEditor.putBoolean("showAds", false);

                                    SharedPreferences.Editor authPrefEditor = authPref.edit();
                                    authPrefEditor.putInt("coins", userCoins - 100);

                                    settingsEditor.apply();
                                    authPrefEditor.apply();

                                    setUpUI();
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
    }

    public void disableNotificationUpdate(Boolean isChecked) {
        allowNotification = !isChecked;
        SharedPreferences.Editor editor = settingsPref.edit();
        editor.putBoolean("allowNotification", !isChecked);
        editor.apply();

        String url = getString(R.string.root_url) + "v1/settings/" + Integer.toString(settingsId);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_notification", !isChecked);
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
                                setUpUI();
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

}
