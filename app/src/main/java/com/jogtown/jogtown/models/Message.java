package com.jogtown.jogtown.models;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.time.LocalDate;
import java.util.Date;

public class Message implements IMessage {

    public String messageId = "0";
    public String messageText = "";
    public Author author = new Author();
    public Date date = new Date();
    public String messageType = "text";
    public String voiceUrl = "";
    public String imageUrl = "";
    public boolean read = false;

    public Message() {
        super();
    }

    @Override
    public String getId() {
        return messageId;
    }

    @Override
    public String getText() {
        return messageText;
    }

    @Override
    public Author getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return date;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public String getVoiceUrl() {
        return this.voiceUrl;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public boolean isRead() {
        return this.read;
    }

    public Author getAuthor() {
        return this.author;
    }



    //Setters
    public void setId(int id) {
        this.messageId = Integer.toString(id);
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setRead(boolean readStatus) {
        this.read = readStatus;
    }
}
