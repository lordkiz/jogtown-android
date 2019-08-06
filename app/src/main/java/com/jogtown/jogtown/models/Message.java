package com.jogtown.jogtown.models;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.time.LocalDate;
import java.util.Date;

public class Message implements IMessage {

    public String messageId = "0";
    public String messageText = "";
    public Author author = new Author();
    public Date date = new Date();

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
}
