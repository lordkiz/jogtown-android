package com.jogtown.jogtown.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.gson.Gson;
import com.jogtown.jogtown.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class Auth {

    public Auth(){

    }


    public static Boolean login(String response, String header) {
        SharedPreferences sharedPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        JSONObject res = null;
        Log.i("resp auth", response);
        try {

            res = new JSONObject(response);

            JSONObject data = res.getJSONObject("data");
            JSONObject headers = new JSONObject(header);

            long dateOfLastLogin = new Date().getTime();

            //Headers
            String uid = headers.getString("uid");
            String client = headers.getString("client");
            String accessToken = headers.getString("access-token");
            long expiry = headers.getLong("expiry");

            //Data
            int userId = data.getInt("id");
            String email = data.getString("email");
            String name = data.getString("name");
            String gender = data.getString("gender") != null ?
                    data.getString("gender")
                    : "null";
            String profilePicture = data.getString("profile_picture") != null ?
                    data.getString("profile_picture")
                    : "null";
            String provider = data.getString("provider");
            String weight = data.getString("weight") != null ?
                    data.getString("weight")
                    : "null";
            String deviceId = MainActivity.deviceId;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("dateOfLastLogin", dateOfLastLogin);
            editor.putString("uid", uid);
            editor.putString("client", client);
            editor.putString("accessToken", accessToken);
            editor.putLong("expiry", expiry);
            editor.putInt("userId", userId);
            editor.putString("email", email);
            editor.putString("name", name);
            editor.putString("gender", gender);
            editor.putString("profilePicture", profilePicture);
            editor.putString("provider", provider);
            editor.putString("weight", weight);

            editor.putBoolean("authKey", true);

            editor.apply();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static Boolean isSignedIn() {
        SharedPreferences sharedPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        Boolean authKey = sharedPreferences.getBoolean("authKey", false);
        long dateOfLastLogin = sharedPreferences.getLong("dateOfLastLogin", 0);

        long today = new Date().getTime();

        int daysElapsed = Math.round((today - dateOfLastLogin) / 86400000);

        if (authKey && daysElapsed < 360) {
            return true;
        }
        return false;
    }


    public static void signOut(Activity activity) {
        Intent locationServiceIntent = new Intent(activity.getApplicationContext(), LocationService.class);
        Intent jogStatsServiceIntent = new Intent(activity.getApplicationContext(), JogStatsService.class);

        SharedPreferences authPreferences = activity.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        SharedPreferences jogPreferences = activity.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);
        String provider = authPreferences.getString("provider", "n/a");

        activity.stopService(locationServiceIntent);
        activity.stopService(jogStatsServiceIntent);


        Log.i("provider", provider);
        if (provider.equals("facebook")) {
            LoginManager.getInstance().logOut();
            SharedPreferences.Editor authEditor = authPreferences.edit();
            SharedPreferences.Editor jogEditor = jogPreferences.edit();
            authEditor.clear();
            authEditor.apply();
            jogEditor.clear();
            jogEditor.apply();

            Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getApplicationContext().startActivity(intent);
            activity.finish();
            Log.i("logged out of ", provider);
            Toast.makeText(activity, "Successfully signed out", Toast.LENGTH_SHORT).show();
        } else if (provider.equals("google")) {
            //For Google I have already signed the user out immediately I got the data I needed
            //because Google Login keeps login in same user all the time.
            SharedPreferences.Editor authEditor = authPreferences.edit();
            SharedPreferences.Editor jogEditor = jogPreferences.edit();
            authEditor.clear();
            authEditor.apply();
            jogEditor.clear();
            jogEditor.apply();

            Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getApplicationContext().startActivity(intent);
            activity.finish();
            Toast.makeText(activity, "Successfully signed out", Toast.LENGTH_SHORT).show();
        }
    }

}
