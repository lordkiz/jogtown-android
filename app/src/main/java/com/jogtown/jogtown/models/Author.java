package com.jogtown.jogtown.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.jogtown.jogtown.activities.MainActivity;
import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser {

    //Author is always current User
    SharedPreferences authPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

    public Author() {
        super();
    }

    @Override
    public String getId() {
        String id = Integer.toString(authPreferences.getInt("userId", 0));
        return id;
    }

    @Override
    public String getName() {
        String name = authPreferences.getString("name", "");
        return name;
    }

    @Override
    public String getAvatar() {
        String avatar = authPreferences.getString("profilePicture", "");
        return avatar;
    }
}
