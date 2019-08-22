package com.jogtown.jogtown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.fragments.FacebookLogin;
import com.jogtown.jogtown.fragments.GoogleLogin;


public class MainActivity extends AppCompatActivity implements
        FacebookLogin.OnFragmentInteractionListener,
        GoogleLogin.OnFragmentInteractionListener

{

    final String JOG_NOTIFICATION_CHANNEL_ID = "JOG_NOTIFICATION";


    public static Context appContext;
    public static String deviceId = null;

    Boolean loading = false;

    ProgressBar indicator;
    Button facebookLoginButton;
    Button googleLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_main);

        indicator = (ProgressBar) findViewById(R.id.authLoadingIndicator);
        facebookLoginButton = (Button) findViewById(R.id.facebookLoginButton);
        googleLoginButton = (Button) findViewById(R.id.googleLoginButton);

        appContext = getApplicationContext();

        createJogNotificationChannel();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (Auth.isSignedIn()) {
            redirect();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Use this to create listeners btw activity and fragments
        String info = uri.toString();

        switch (info) {
            case "loading: true":
                Log.i("case loading true", info);
                loading = true;
                showActivityIndicator();
                break;
            case"loading: false":
                Log.i("case loading false", info);
                loading = false;
                showActivityIndicator();
                break;
            case "redirect":
                Log.i("case redirect", info);
                redirect();
                showActivityIndicator();
                break;
            default:
                loading = false;
                showActivityIndicator();
        }
    }

    public void showActivityIndicator() {
        //Replaces a view with Progress bar
        if (loading) {
            indicator.setVisibility(View.VISIBLE);
            facebookLoginButton.setVisibility(View.INVISIBLE);
            googleLoginButton.setVisibility(View.INVISIBLE);
        }  else {
            indicator.setVisibility(View.INVISIBLE);
            facebookLoginButton.setVisibility(View.VISIBLE);
            googleLoginButton.setVisibility(View.VISIBLE);
        }
    }

    public void redirect() {
        Intent intent = new Intent(this, AppActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }


    private void createJogNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "JOG_NOTIFICATION";
            String description = "Send Jog Stats";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(JOG_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
