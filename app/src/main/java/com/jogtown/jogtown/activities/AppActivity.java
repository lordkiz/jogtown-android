package com.jogtown.jogtown.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.HistoryFragment;
import com.jogtown.jogtown.fragments.InboxFragment;
import com.jogtown.jogtown.fragments.JogDetailFragment;
import com.jogtown.jogtown.fragments.ProfileFragment;
import com.jogtown.jogtown.fragments.PurchaseCoinsFragment;
import com.jogtown.jogtown.subfragments.RemoveAdFragment;
import com.jogtown.jogtown.fragments.StartFragment;
import com.jogtown.jogtown.subfragments.JogStatsFragment;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.services.JogStatsService;
import com.jogtown.jogtown.utils.services.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AppActivity extends AppCompatActivity implements

        //FRAGMENTS THAT FORM THE BOTTOM TAB NAVIGATION
        ProfileFragment.OnFragmentInteractionListener,
        StartFragment.OnFragmentInteractionListener,
        InboxFragment.OnFragmentInteractionListener,
        PurchaseCoinsFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        JogDetailFragment.OnFragmentInteractionListener,
        RemoveAdFragment.OnFragmentInteractionListener,
        //SUBFRAGMENTS THAT MAKE UP THE FRAGMENTS ABOVE
        JogStatsFragment.OnFragmentInteractionListener
{


    private static BottomNavigationView bottomNavigation;
    AppBarConfiguration appBarConfiguration;
    SharedPreferences jogPref;
    SharedPreferences authPref;
    SharedPreferences settingsPref;
    boolean canClose = false;

    int currentDestinationId;

    int [] topLevelDestinationIds =  {R.id.startFragment, R.id.profileFragment, R.id.historyFragment};

    ObjectAnimator bottomNavigationBarVisibilityAnimator;

    FrameLayout appActivityFragmentScreenContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        appActivityFragmentScreenContainer = findViewById(R.id.fragmentScreenContainer);

        canClose = false;

        jogPref = getSharedPreferences("JogPreferences", MODE_PRIVATE);
        authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        settingsPref = getSharedPreferences("SettingsPreferences", MODE_PRIVATE);

        boolean jogIsOn = jogPref.getBoolean("jogIsOn", false);
        String jogType = jogPref.getString("jogType", "n/a");

        int weight = authPref.getInt("weight", 0);
        String gender = authPref.getString("gender", "null");

        if (jogIsOn) {
            //jog is more important
            Toast.makeText(this, "You have a jog ongoing", Toast.LENGTH_SHORT).show();
        } else if (weight == 0) {
            //Weight is second in priority
            if (settingsPref.getBoolean("promptForWeight", true)) {
                selectWeight();
            }
        } else if (gender.equals("null")) {
            if (settingsPref.getBoolean("promptForGender", true)) {
                selectGender();
            }
        }

        bottomNavigation = findViewById(R.id.bottomBarNavigation);

        NavController navController = Navigation.findNavController(this, R.id.main_content);


        appBarConfiguration = new AppBarConfiguration.Builder(R.id.startFragment, R.id.profileFragment, R.id.historyFragment)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(bottomNavigation, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int destinationId = destination.getId();

                currentDestinationId = destinationId;

                if (ArrayUtils.contains(topLevelDestinationIds, destinationId)) {
                    //showBottomBar
                    showBottomBar(true);
                } else {
                    showBottomBar(false);
                }
            }
        });

        silentlyRetrieveAndSaveSettings();
    }


    public static void switchToMyGroupsTab() {
        bottomNavigation.setSelectedItemId(R.id.Inbox);
    }

    public static void switchToInboxTab() {
        bottomNavigation.setSelectedItemId(R.id.Inbox);
    }




    public void selectWeight() {
        final Dialog weightDialog = new Dialog(this, android.R.style.Theme_NoTitleBar);
        weightDialog.setContentView(R.layout.weight_select_layout);
        weightDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        weightDialog.setCanceledOnTouchOutside(true);

        final NumberPicker weightPicker = weightDialog.findViewById(R.id.weightNumberPicker);

        final CheckBox doNotAskAgain = weightDialog.findViewById(R.id.weightDoNotAskAgainCheckBox);
        Button negativeButton = weightDialog.findViewById(R.id.weightDialogDismissButton);
        Button positiveButton = weightDialog.findViewById(R.id.weightDialogSetButton);

        final SharedPreferences settingsPref = getSharedPreferences("SettingsPreferences", MODE_PRIVATE);
        SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        final int userId = authPref.getInt("userId", 0);
        final SharedPreferences.Editor settingsEditor = settingsPref.edit();
        final SharedPreferences.Editor authEditor = authPref.edit();

        weightPicker.setMaxValue(250);
        weightPicker.setMinValue(20);
        weightPicker.setValue(70);

        String[] displayedValues = new String[232];
        for (int i = 0; i < 232; i++) {
            displayedValues[i] = Integer.toString(i + 20) + " kg";
        }

        //weightPicker.setDisplayedValues(displayedValues);

        weightDialog.setCancelable(true);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doNotAskAgain.isChecked()) {
                    settingsEditor.putBoolean("promptForWeight", false);
                    settingsEditor.apply();
                }
                weightDialog.dismiss();
                if (settingsPref.getBoolean("promptForGender", true)) {
                    selectGender();
                }
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = getString(R.string.root_url) + "v1/users/" + Integer.toString(userId);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("weight", weightPicker.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String payload = jsonObject.toString();

                authEditor.putInt("weight", weightPicker.getValue());
                authEditor.apply();

                NetworkRequest.put(url, payload, new MyUrlRequestCallback(new MyUrlRequestCallback.OnFinishRequest<JSONObject>() {
                    @Override
                    public void onFinishRequest(JSONObject result) {
                        try {
                            if (result.getInt("statusCode") < 300) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Weight Saved", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }));
                weightDialog.dismiss();
                if (settingsPref.getBoolean("promptForGender", true)) {
                    selectGender();
                }
            }
        });

        weightDialog.show();

    }





    public void selectGender() {
        final Dialog genderDialog = new Dialog(this, android.R.style.Theme_NoTitleBar);
        genderDialog.setContentView(R.layout.gender_select_layout);
        genderDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        genderDialog.setCanceledOnTouchOutside(true);
        genderDialog.setCancelable(true);

        final NumberPicker genderPicker = genderDialog.findViewById(R.id.genderPicker);

        final CheckBox doNotAskAgain = genderDialog.findViewById(R.id.genderDoNotAskAgainCheckBox);
        Button negativeButton = genderDialog.findViewById(R.id.genderDialogDismissButton);
        Button positiveButton = genderDialog.findViewById(R.id.genderDialogSetButton);

        SharedPreferences settingsPref = getSharedPreferences("SettingsPreferences", MODE_PRIVATE);
        SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        final int userId = authPref.getInt("userId", 0);
        final SharedPreferences.Editor settingsEditor = settingsPref.edit();
        final SharedPreferences.Editor authEditor = authPref.edit();

        genderPicker.setMaxValue(1);
        genderPicker.setMinValue(0);
        genderPicker.setValue(0);

        final String[] displayedValues = {"male", "female"};
        genderPicker.setDisplayedValues(displayedValues);
        genderPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return displayedValues[value];
            }
        });
        genderPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doNotAskAgain.isChecked()) {
                    settingsEditor.putBoolean("promptForGender", false);
                    settingsEditor.apply();
                }
                genderDialog.dismiss();
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = getString(R.string.root_url) + "v1/users/" + Integer.toString(userId);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("gender", genderPicker.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String payload = jsonObject.toString();

                authEditor.putString("gender", displayedValues[genderPicker.getValue()]);
                authEditor.apply();

                NetworkRequest.put(url, payload, new MyUrlRequestCallback(new MyUrlRequestCallback.OnFinishRequest<JSONObject>() {
                    @Override
                    public void onFinishRequest(JSONObject result) {
                        try {
                            if (result.getInt("statusCode") < 300) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Gender Saved", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }));
                genderDialog.dismiss();
            }
        });

        genderDialog.show();

    }

    private void showBottomBar(Boolean show) {
        int containerPaddingBottom = appActivityFragmentScreenContainer.getPaddingBottom();
        if (show) {
            bottomNavigationBarVisibilityAnimator = ObjectAnimator.ofInt(bottomNavigation, "visibility", View.VISIBLE);
            if (containerPaddingBottom < 56) {
                appActivityFragmentScreenContainer.setPadding(0, 0, 0, containerPaddingBottom + 120);
            }
        } else {
            bottomNavigationBarVisibilityAnimator = ObjectAnimator.ofInt(bottomNavigation, "visibility", View.GONE);
            if (containerPaddingBottom > 56) {
                appActivityFragmentScreenContainer.setPadding(0, 0, 0, 0);
            }

        }

        bottomNavigationBarVisibilityAnimator.setDuration(500);
        //add animation objectanimator listener if you want to do extra stuffs
        bottomNavigationBarVisibilityAnimator.start();
    }


    private void silentlyRetrieveAndSaveSettings() {
        String userId = Integer.toString(authPref.getInt("userId", 0));
        String url = getString(R.string.root_url) + "v1/current_user_settings?user_id=" + userId;
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject jsonObject = new JSONObject(result.toString());
                    int statusCode = jsonObject.getInt("statusCode");
                    JSONObject resultObj = new JSONObject(jsonObject.getString("body"));

                    if (statusCode < 300) {
                        SharedPreferences.Editor editor = settingsPref.edit();
                        boolean showAds = resultObj.getBoolean("show_ads");
                        boolean allowNotification = resultObj.getBoolean("allow_notification");
                        boolean useKM = resultObj.getBoolean("km");
                        boolean useKG = resultObj.getBoolean("kg");
                        int settingsId = resultObj.getInt("id");
                        editor.putBoolean("showAds", showAds);
                        editor.putBoolean("km", useKM);
                        editor.putBoolean("kg", useKG);
                        editor.putBoolean("allowNotification", allowNotification);
                        editor.putInt("settingsId", settingsId);

                        editor.apply();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            NetworkRequest.get(url, new MyUrlRequestCallback(onFinishRequest));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        Drawable drawable = menu.getItem(0).getIcon();
        drawable.mutate();
        drawable.setColorFilter(getResources().getColor(R.color.snow), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.settings_menu_item:
                Intent intent = new Intent(getApplicationContext(), SecondaryAppActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);


                return true;
        }
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onBackPressed() {
        if (ArrayUtils.contains(topLevelDestinationIds, currentDestinationId)) {
            if (canClose) {
                boolean jogIsOn = jogPref.getBoolean("jogIsOn", false);
                if (!jogIsOn) {
                    // if jog is NOT on
                    Intent locationServiceIntent = new Intent(this, LocationService.class);
                    Intent jogStatsServiceIntent = new Intent(this, JogStatsService.class);
                    stopService(locationServiceIntent);
                    stopService(jogStatsServiceIntent);

                }

                super.onBackPressed();
                finish();
            } else {
                canClose = true;
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        canClose = false;
                    }
                }, 3000);
            }
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.main_content);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}
