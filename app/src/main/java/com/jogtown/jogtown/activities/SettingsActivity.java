package com.jogtown.jogtown.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class SettingsActivity extends AppCompatActivity {

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsPref = getSharedPreferences("SettingsPreferences", MODE_PRIVATE);
        authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);

        allowNotification = settingsPref.getBoolean("allowNotification", true);
        showAds = settingsPref.getBoolean("showAds", true);
        settingsId = settingsPref.getInt("settingsId", 0);

        disableNotificationsSwitch = findViewById(R.id.disableNotificationsSwitch);

        removeAdsLayout = findViewById(R.id.removeAdsLayout);
        loadingRemoveAdsLayout = findViewById(R.id.loadingRemoveAdsLayout);

        removeAdsButton = findViewById(R.id.removeAdsButton);
        getCoinsButton = findViewById(R.id.getCoinsButton);

        ActionBar actionBar = getSupportActionBar();
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

    }

    public void setUpUI() {
        if (!showAds) {
            removeAdsLayout.setVisibility(View.GONE);
        }

        disableNotificationsSwitch.setChecked(!allowNotification);
    }

    private void promptRemoveAds() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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
                                Toast.makeText(getApplicationContext(), "Settings Updated", Toast.LENGTH_SHORT).show();
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


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
