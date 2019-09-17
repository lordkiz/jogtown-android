package com.jogtown.jogtown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.fragments.FacebookLogin;
import com.jogtown.jogtown.fragments.GoogleLogin;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.onesignal.OneSignal;


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
    AdView mAdView;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        // Initialise appcenter && crash reports
        AppCenter.start(getApplication(), "86e003b2-00eb-4a14-96e7-dabd7572d7ab",
                Analytics.class, Crashes.class);

        indicator = (ProgressBar) findViewById(R.id.authLoadingIndicator);
        facebookLoginButton = (Button) findViewById(R.id.facebookLoginButton);
        googleLoginButton = (Button) findViewById(R.id.googleLoginButton);

        appContext = getApplicationContext();

        createJogNotificationChannel();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        videoView = findViewById(R.id.login_background_video_view);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.jogtown_login_background_video);
        videoView.setVideoURI(uri);
        videoView.start();

        if (Auth.isSignedIn()) {
            videoView.stopPlayback();
            redirect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        videoView = findViewById(R.id.login_background_video_view);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.jogtown_login_background_video);
        videoView.setVideoURI(uri);
        videoView.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
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
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(JOG_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
