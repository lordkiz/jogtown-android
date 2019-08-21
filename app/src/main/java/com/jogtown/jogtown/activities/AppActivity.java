package com.jogtown.jogtown.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
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
import com.jogtown.jogtown.utils.services.JogStatsService;
import com.jogtown.jogtown.utils.services.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

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
    SharedPreferences sharedPreferences;
    boolean canClose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        canClose = false;

        sharedPreferences = getSharedPreferences("JogPreferences", MODE_PRIVATE);
        boolean jogIsOn = sharedPreferences.getBoolean("jogIsOn", false);
        String jogType = sharedPreferences.getString("jogType", "n/a");

        if (jogIsOn) {
            if (jogType.equals("single")) {
                Toast.makeText(this, "You have a jog ongoing", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SingleJogActivity.class));
            } else if (jogType.equals("group")) {
                String group = sharedPreferences.getString("group", "");
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




    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        if (canClose) {
            boolean jogIsOn = sharedPreferences.getBoolean("jogIsOn", false);
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
