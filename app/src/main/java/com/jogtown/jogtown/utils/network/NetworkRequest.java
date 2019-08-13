package com.jogtown.jogtown.utils.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jogtown.jogtown.activities.MainActivity;

import org.chromium.net.CronetEngine;
import org.chromium.net.UploadDataProvider;
import org.chromium.net.UploadDataProviders;
import org.chromium.net.UrlRequest;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NetworkRequest {
    public static CronetEngine cronetEngine = new CronetEngine.Builder(MainActivity.appContext)
            .build();

    public static Executor executor = Executors.newSingleThreadExecutor();


    public static UrlRequest.Builder makeRequestBuilder(String url, String httpMethod, UrlRequest.Callback callback) {

        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(url, callback, executor);

        requestBuilder.setHttpMethod(httpMethod);

        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        String accessToken = authPref.getString("accessToken", "null");
        String client = authPref.getString("client", "null");
        String expiry = Long.toString(authPref.getLong("expiry", 0));
        String uid = authPref.getString("uid", "null");

        requestBuilder.addHeader("Content-Type", "application/json");
        requestBuilder.addHeader("Accept", "application/json");
        requestBuilder.addHeader("access-token", accessToken);
        requestBuilder.addHeader("client", client);
        requestBuilder.addHeader("uid", uid);
        requestBuilder.addHeader("expiry", expiry);



        return requestBuilder;

    }



    public static UploadDataProvider generateUploadDataProvider(String payload) {
        byte[] bytes = convertStringToBytes(payload);
        UploadDataProvider uploadDataProvider = UploadDataProviders.create(bytes);

        return uploadDataProvider;
    }



    public static byte[] convertStringToBytes(String payload) {
        byte[] bytes;
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload.getBytes());
        if (byteBuffer.hasArray()) {
            bytes = byteBuffer.array();
        } else {
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }

        return bytes;
    }



    public static void get(String url, UrlRequest.Callback callback) {
        //You should define your callback from where you called this method from
        UrlRequest.Builder requestBuilder = makeRequestBuilder(url, "GET", callback);
        UrlRequest request = requestBuilder.build();
        request.start();
    }


    public static void put(String url, String payload, UrlRequest.Callback callback) {
        //You should define your callback from where you called this method from
        UrlRequest.Builder requestBuilder = makeRequestBuilder(url, "PUT", callback);

        requestBuilder.setUploadDataProvider(generateUploadDataProvider(payload), executor);

        UrlRequest request = requestBuilder.build();

        request.start();
    }

    public static void post(String url, String payload, UrlRequest.Callback callback) {
        //You should define your callback from where you called this method from
        UrlRequest.Builder requestBuilder = makeRequestBuilder(url, "POST", callback);

        requestBuilder.setUploadDataProvider(generateUploadDataProvider(payload), executor);

        UrlRequest request = requestBuilder.build();

        request.start();
    }

    public static void delete(String url, String payload, UrlRequest.Callback callback) {
        //You should define your callback from where you called this method from
        UrlRequest.Builder requestBuilder = makeRequestBuilder(url, "DELETE", callback);

        requestBuilder.setUploadDataProvider(generateUploadDataProvider(payload), executor);

        UrlRequest request = requestBuilder.build();

        request.start();
    }

}
