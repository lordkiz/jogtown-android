package com.jogtown.jogtown.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.models.Author;
import com.jogtown.jogtown.models.Message;
import com.jogtown.jogtown.utils.network.ActionCableSocket;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.PicassoCircle;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    boolean loading = false;

    Subscription subscription;

    MessagesList messagesList;
    MessageInput messageInputBar;
    ProgressBar progressBar;
    MessagesListAdapter<Message> messageListAdapter;

    String senderId;
    String chatId;
    String chatName;
    String chatAvatar;
    String senderAvatar;
    String senderName;
    int recipientId = 0;
    //ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        messagesList = findViewById(R.id.messagesList);
        messageInputBar = findViewById(R.id.messageInputBar);
        progressBar = findViewById(R.id.conversationActivityProgressBar);


        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");
        chatName = intent.getStringExtra("chatName");
        chatAvatar = intent.getStringExtra("chatAvatar");

        senderId = Integer.toString(authPref.getInt("userId", 0));
        senderAvatar = authPref.getString("profilePicture", "");
        senderName = authPref.getString("name", "");

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("  " + chatName);
            actionBar.setSubtitle("  online");
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            try {
                Picasso.get().load(chatAvatar)
                        .resize(100,100)
                        .transform(new PicassoCircle())
                        .into(new Target()
                {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable d = new BitmapDrawable(getResources(), bitmap);
                        actionBar.setIcon(d);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        setupAdapter();

        setupInput();

        getChatMessages();

        createSocket();

    }


    public void setupAdapter() {
        messageListAdapter = new MessagesListAdapter<>(senderId, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                try {
                    Picasso.get().load(url).into(imageView);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });

        messagesList.setAdapter(messageListAdapter);

    }

    public void setupInput() {

        messageInputBar.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", Math.round(Math.random() * 10000000));
                    jsonObject.put("message_type", "text");
                    jsonObject.put("message_text", input);
                    jsonObject.put("chat_id", chatId);
                    jsonObject.put("user_id", senderId);
                    jsonObject.put("sender_name", senderName);
                    jsonObject.put("sender_avatar", senderAvatar);
                    jsonObject.put("recipient_id", recipientId);
                    jsonObject.put("read", false);
                    jsonObject.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

                    String payload = jsonObject.toString();

                    sendMessage(payload);

                    Message message = buildMessage(jsonObject);
                    messageListAdapter.addToStart(message, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }


    public void getChatMessages() {
        loading = true;
        showActivity();


        String url = getString(R.string.root_url) + "v1/chats/" + chatId;

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = createOnFinishRequestCallbackActions();
        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);

        NetworkRequest.get(url, requestCallback);
    }



    public MyUrlRequestCallback.OnFinishRequest createOnFinishRequestCallbackActions() {
        SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        final String userId = Integer.toString(authPref.getInt("userId", 0));
        //need this userId to mark messages sent by another user as read.

        return new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode == 200) { //Some kind of success
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                try {

                                    List<Integer> unreadMessageIds = new ArrayList<>();

                                    JSONObject jsonObject = new JSONObject(responseBody);
                                    JSONArray messagesArr = new JSONArray(jsonObject.getString("messages"));

                                    if (Integer.parseInt(userId) != jsonObject.getInt("recipient_id")) {
                                        recipientId = jsonObject.getInt("recipient_id");
                                    } else {
                                        recipientId = jsonObject.getInt("sender_id");
                                    }

                                    for (int i = 0; i < messagesArr.length(); i++) {
                                        //Create a Chatkit Message of each message
                                        String msgStr = messagesArr.get(i).toString();
                                        JSONObject msgObj = new JSONObject(msgStr);

                                        Message message = buildMessage(msgObj);

                                        if (!message.isRead() && !message.getAuthor().getId().equals(userId)) {
                                            //messages that are not from me and haven't been read
                                            unreadMessageIds.add(Integer.parseInt(message.getId()));
                                        }

                                        messageListAdapter.addToStart(message, true);

                                    }

                                    if (unreadMessageIds.size() > 0) {
                                        markUnreadMessagesAsRead(unreadMessageIds);
                                    }

                                } catch (JSONException e) {
                                    loading = false;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showActivity();
                                        }
                                    });

                                }
                            }
                        });

                    } else if (statusCode > 399) { //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConversationActivity.this);
                                alertDialogBuilder
                                        .setCancelable(true)
                                        .setMessage(responseBody)
                                        .setTitle("Error!");
                                alertDialogBuilder.create().show();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loading = false;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showActivity();
                        }
                    });
                }
            }
        };
    }





    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

        }
    }



    public void markUnreadMessagesAsRead(List<Integer> unreadMessageIds) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("message_ids", unreadMessageIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String payloadString = payload.toString();
        String url = getString(R.string.root_url) + "v1/mark_messages_as_read";

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Show read receipts for each of these messages
            }
        };

        NetworkRequest.post(url, payloadString, new MyUrlRequestCallback(onFinishRequest));
    }



    public void createSocket() {
        ActionCableSocket.OnSocketConnection onSocketConnection = new ActionCableSocket.OnSocketConnection() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onRejected() {

            }

            @Override
            public void onReceived(JSONObject jsonObject) {
                try {
                    String data = jsonObject.getString("body");
                    JSONObject dataObj = new JSONObject(data);
                    if (dataObj.has("message")) {
                        JSONObject msgObj = dataObj.getJSONObject("message");
                        if (msgObj.has("message_type")) {
                            final Message message = buildMessage(msgObj);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (Integer.parseInt(message.getAuthor().getId()) != Integer.parseInt(senderId)) {
                                        messageListAdapter.addToStart(message, true);
                                    }
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onFailed() {

            }
        };
        ActionCableSocket actionCableSocket = new ActionCableSocket(
                "ChatChannel",
                chatId,
                onSocketConnection);

        subscription = actionCableSocket.subscription;

    }


    public Message buildMessage(JSONObject jsonObject) {
        Message message = new Message();
        Author author = new Author();

        try {
            JSONObject msgObj = jsonObject;
            String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

            String authorId = Integer.toString(msgObj.getInt("user_id"));
            String authorName = msgObj.getString("sender_name");
            String authorAvatar = msgObj.getString("sender_avatar");
            author.setAvatar(authorAvatar);
            author.setId(authorId);
            author.setName(authorName);

            String messageText = msgObj.getString("message_text");
            String messageType = msgObj.getString("message_type");
            int messageId = msgObj.getInt("id");
            boolean isRead = msgObj.getBoolean("read");


            message.setId(messageId);
            message.setDate(new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(msgObj.getString("updated_at")));
            message.setMessageText(messageText);
            message.setMessageType(messageType);
            message.setRead(isRead);
            message.setAuthor(author);


            if (messageType.equals("voice")) {
                message.setVoiceUrl(msgObj.getString("voice"));
            }

            if (messageType.equals("image")) {
                message.setVoiceUrl(msgObj.getString("image"));
            }

            return message;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return message;

    }


    public void sendMessage(String payload) {
        String url = getString(R.string.root_url) + "v1/messages";

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Ensure message was saved
            }
        };

        NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));
    }

}
