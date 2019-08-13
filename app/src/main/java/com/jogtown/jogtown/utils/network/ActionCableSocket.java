package com.jogtown.jogtown.utils.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonElement;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ActionCableSocket {

    public OnSocketConnection delegate;

    public Subscription subscription;

    Channel channel;

    URI uri = URI.create(MainActivity.appContext.getString(R.string.websocket_url));

    public Consumer consumer;

    Consumer.Options options = new Consumer.Options();

    public ActionCableSocket(String channelName, String id, OnSocketConnection socketConnectionCallback) {
        this.channel = new Channel(channelName);
        this.delegate = socketConnectionCallback;
        setOptions();
        consumer  = ActionCable.createConsumer(this.uri, this.options);
        this.subscription = setSubscription(id);

    }


    public Subscription setSubscription(String id) {
        this.channel.addParam("id", id);
        Subscription subs = this.consumer.getSubscriptions().create(this.channel);
        this.consumer.connect();

        subs
                .onConnected(new Subscription.ConnectedCallback() {
                    @Override
                    public void call() {
                        Log.i("Socket connected", "connected");
                    }
                })
                .onDisconnected(new Subscription.DisconnectedCallback() {
                    @Override
                    public void call() {

                        Log.i("Socket Disconnected", "Disconnected");
                    }
                })
                .onReceived(new Subscription.ReceivedCallback() {
                    @Override
                    public void call(JsonElement jsonElement) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("body", jsonElement.toString());
                            delegate.onReceived(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .onRejected(new Subscription.RejectedCallback() {
                    @Override
                    public void call() {


                    }
                })
                .onFailed(new Subscription.FailedCallback() {
                    @Override
                    public void call(ActionCableException e) {

                        Log.i("Socket ", "failed");
                    }
                });

        return subs;
    }



    private void setOptions() {
        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        String accessToken = authPref.getString("accessToken", "null");
        String client = authPref.getString("client", "null");
        String uid = authPref.getString("uid", "null");
        long expiry = authPref.getLong("expiry", 0);
        String provider = authPref.getString("provider", "null");

        Map<String, String> query = new HashMap();
        query.put("access-token", accessToken);
        query.put("client", client);
        query.put("uid", uid);
        query.put("provider", provider);
        query.put("expiry", Long.toString(expiry));
        this.options.query = query;

        Map<String, String> headers = new HashMap();
        headers.put("Origin", "https://jogtown.herokuapp.com/");
        this.options.headers = headers;

        this.options.reconnection = true;
        this.options.reconnectionMaxAttempts = 10;

    }



    public interface OnSocketConnection {
        public void onConnected();
        public void onRejected();
        public void onReceived(JSONObject jsonObject);
        public void onDisconnected();
        public void onFailed();
    }


}
