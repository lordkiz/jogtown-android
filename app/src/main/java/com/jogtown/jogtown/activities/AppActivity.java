package com.jogtown.jogtown.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.HistoryFragment;
import com.jogtown.jogtown.fragments.InboxFragment;
import com.jogtown.jogtown.fragments.ProfileFragment;
import com.jogtown.jogtown.fragments.StartFragment;
import com.jogtown.jogtown.subfragments.SingleRunStatsFragment;
import com.jogtown.jogtown.utils.JogStatsService;
import com.jogtown.jogtown.utils.LocationService;

public class AppActivity extends AppCompatActivity implements

        //FRAGMENTS THAT FORM THE BOTTOM TAB NAVIGATION
        StartFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        InboxFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,

        //SUBFRAGMENTS THAT MAKE UP THE FRAGMENTS ABOVE
        SingleRunStatsFragment.OnFragmentInteractionListener

{


    BottomNavigationView bottomNavigation;
    ActionBar actionBar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        sharedPreferences = getSharedPreferences("JogPreferences", MODE_PRIVATE);
        boolean jogIsOn = sharedPreferences.getBoolean("jogIsOn", false);
        String jogType = sharedPreferences.getString("jogType", "n/a");

        if (jogIsOn) {
            if (jogType.equals("single")) {
                Toast.makeText(this, "You have a jog ongoing", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SingleRunActivity.class));
            } else if (jogType.equals("group")) {
                startActivity(new Intent(this, GroupRunActivity.class));
                Toast.makeText(this, "You have a group jog ongoing", Toast.LENGTH_SHORT).show();
            }
        }


        actionBar = getSupportActionBar();
        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomBarNavigation);
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




    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
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
    }



}
