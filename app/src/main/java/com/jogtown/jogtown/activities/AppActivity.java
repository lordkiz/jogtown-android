package com.jogtown.jogtown.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.GroupsFragment;
import com.jogtown.jogtown.fragments.HistoryFragment;
import com.jogtown.jogtown.fragments.InboxFragment;
import com.jogtown.jogtown.fragments.ProfileFragment;
import com.jogtown.jogtown.fragments.StartFragment;
import com.jogtown.jogtown.subfragments.MyGroupsListFragment;
import com.jogtown.jogtown.subfragments.MyGroupsListInDialogFragment;
import com.jogtown.jogtown.subfragments.SearchGroupsListFragment;
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
        StartFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        InboxFragment.OnFragmentInteractionListener,
        GroupsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,

        //SUBFRAGMENTS THAT MAKE UP THE FRAGMENTS ABOVE
        JogStatsFragment.OnFragmentInteractionListener,
        MyGroupsListFragment.OnFragmentInteractionListener,
        SearchGroupsListFragment.OnFragmentInteractionListener,
        MyGroupsListInDialogFragment.OnFragmentInteractionListener

{


    private static BottomNavigationView bottomNavigation;
    ActionBar actionBar;
    SharedPreferences jogPref;
    SharedPreferences authPref;
    SharedPreferences settingsPref;
    boolean canClose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        canClose = false;

        jogPref = getSharedPreferences("JogPreferences", MODE_PRIVATE);
        authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        settingsPref = getSharedPreferences("SettingsPreferences", MODE_PRIVATE);

        boolean jogIsOn = jogPref.getBoolean("jogIsOn", false);
        String jogType = jogPref.getString("jogType", "n/a");

        int weight = authPref.getInt("weight", 0);
        Log.i("weight", Integer.toString(weight));
        String gender = authPref.getString("gender", "null");

        if (jogIsOn) {
            //jog is more important
            if (jogType.equals("single")) {
                Toast.makeText(this, "You have a jog ongoing", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SingleJogActivity.class));
            } else if (jogType.equals("group")) {
                String group = jogPref.getString("group", "");
                //check if we have the right group pls
                try {
                    JSONObject obj = new JSONObject(group);
                    if (obj.getInt("id") > 0) {
                        Intent intent = new Intent(this, GroupJogActivity.class);
                        intent.putExtra("group", group);
                        startActivity(intent);
                        Toast.makeText(this, "You have a group jog ongoing", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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


        actionBar = getSupportActionBar();
        bottomNavigation = findViewById(R.id.bottomBarNavigation);
        if (actionBar != null) {
            actionBar.setTitle("Start");
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }
        StartFragment startFragment = new StartFragment();
        openFragment(startFragment, "Start");

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getTitle().toString()) {
                    case "Start":
                        if (actionBar != null) {
                            actionBar.setTitle("Start");
                        }
                        StartFragment startFragment = new StartFragment();
                        openFragment(startFragment, "Start");
                        return true;

                    case "History":
                        if (actionBar != null) {
                            actionBar.setTitle("History");
                        }
                        HistoryFragment historyFragment = new HistoryFragment();
                        openFragment(historyFragment, "History");
                        return true;

                    case "Inbox":
                        if (actionBar != null) {
                            actionBar.setTitle("Inbox");
                        }
                        InboxFragment inboxFragment = new InboxFragment();
                        openFragment(inboxFragment, "Inbox");
                        return true;

                    case "Groups":
                        if (actionBar != null) {
                            actionBar.setTitle("Groups");
                        }
                        GroupsFragment groupsFragment = new GroupsFragment();
                        openFragment(groupsFragment, "Groups");
                        return true;

                    case "Profile":
                        if (actionBar != null) {
                            actionBar.setTitle("Profile");
                            actionBar.setElevation(0);
                            actionBar.setBackgroundDrawable(getDrawable(R.drawable.purple_linear_background));
                        }
                        ProfileFragment profileFragment = new ProfileFragment();
                        openFragment(profileFragment, "Profile");
                        return true;

                    default:
                        return false;
                }
            }
        });

        silentlyRetrieveAndSaveSettings();
    }


    private void openFragment(Fragment fragment, String fragmentName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentScreenContainer, fragment, fragmentName);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public static void switchToMyGroupsTab() {
        bottomNavigation.setSelectedItemId(R.id.groupsTab);
    }

    public static void switchToInboxTab() {
        bottomNavigation.setSelectedItemId(R.id.inboxTab);
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

        SharedPreferences settingsPref = getSharedPreferences("SettingsPreferences", MODE_PRIVATE);
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
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("weightPickerValue: ", Integer.toString(weightPicker.getValue()));

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

                Log.i("genderPickerValue: ", displayedValues[genderPicker.getValue()]);

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
                        int settingsId = resultObj.getInt("id");
                        editor.putBoolean("showAds", showAds);
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
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
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
    }



}
