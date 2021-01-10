package com.jogtown.jogtown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.PurchaseCoinsFragment;
import com.jogtown.jogtown.fragments.SettingsFragment;

public class SecondaryAppActivity extends AppCompatActivity implements
        SettingsFragment.OnFragmentInteractionListener,
        PurchaseCoinsFragment.OnFragmentInteractionListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_app);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
