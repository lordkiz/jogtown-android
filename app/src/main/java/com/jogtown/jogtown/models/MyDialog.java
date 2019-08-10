package com.jogtown.jogtown.models;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;

public class MyDialog implements IDialog {

    public IMessage lastMessage = null;
    public String chatId = "";
    public String dialogPhoto = "";
    public String dialogName = "";
    public int unreadCount = 0;

    public MyDialog() {
    }

    @Override
    public String getId() {
        return this.chatId;
    }

    @Override
    public String getDialogPhoto() {
        return this.dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return this.dialogName;
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
        return this.unreadCount;
    }

    //Setters

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public void setId(String chatId) {
        this.chatId = chatId;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }

    public void setUnreadCount(int count) {
        this.unreadCount = count;
    }

    public static String dateFormatter(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, DateFormatter.Template.TIME);
        } else if (DateFormatter.isYesterday(date)) {
            return "Yesterday";
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }
}
