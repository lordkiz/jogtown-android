package com.jogtown.jogtown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jogtown.jogtown.R;

public class PurchaseCoinsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_coins);
    }
}
