package com.jogtown.jogtown.models;

import com.stfalcon.chatkit.commons.models.IUser;


public class Author implements IUser {


    public String id = "0";
    public String name = "";
    public String avatar = "";


    public Author() {
        super();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getAvatar() {
        return this.avatar;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
