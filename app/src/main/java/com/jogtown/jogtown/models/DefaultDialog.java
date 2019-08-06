package com.jogtown.jogtown.models;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.Date;

public class DefaultDialog implements IDialog {

    public IMessage lastMessage = null;
    public String chatId = "0";
    public String dialogPhoto = "";
    public String dialogName = "";

    public DefaultDialog() {
    }

    @Override
    public String getId() {
        return chatId;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<IUser> getUsers() {
        return new ArrayList<>();
    }

    @Override
    public IMessage getLastMessage() {
        return this.lastMessage;
    }

    @Override
    public void setLastMessage(IMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return 0;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }
}
