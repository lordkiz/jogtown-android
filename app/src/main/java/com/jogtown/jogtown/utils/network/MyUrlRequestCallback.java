package com.jogtown.jogtown.utils.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jogtown.jogtown.activities.MainActivity;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;


import static android.content.ContentValues.TAG;

public class MyUrlRequestCallback extends UrlRequest.Callback {

    public OnFinishRequest<JSONObject> delegate;

    public String headers;
    public String responseBody;
    public int httpStatusCode;

    public MyUrlRequestCallback(OnFinishRequest<JSONObject> onFinishRequest) {
        //You should create a MyUrlRequestCallback.OnFinishRequest() and
        //override onFinishRequest.
        //We will send JSON String response to this interface and you can then
        //perform actions on the UI or otherwise based on the result.

        //All MyUrlRequestCallback functions send response to this.delegate
        //which provides it to the interface onFinishRequest which you use in
        //your activity or fragment.

        delegate = onFinishRequest;
    }


    @Override
    public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) throws Exception {
        Log.i(TAG, "onRedirectReceived method called.");
        // You should call the request.followRedirect() method to continue
        // processing the request.
        request.followRedirect();
    }

    @Override
    public void onResponseStarted(UrlRequest request, UrlResponseInfo info) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024*1024);
        Log.i(TAG, "onResponseStarted method called.");
        // You should call the request.read() method before the request can be
        // further processed. The following instruction provides a ByteBuffer object
        // with a capacity of 102400 bytes to the read() method.
        request.read(byteBuffer);
    }

    @Override
    public void onReadCompleted(UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) throws Exception {
        Log.i(TAG, "onReadCompleted method called.");
        // You should keep reading the request until there's no more data.
        request.read(byteBuffer);

        int statusCode = info.getHttpStatusCode();
        this.httpStatusCode = statusCode;

        byte[] bytes;
        if (byteBuffer.hasArray()) {
            bytes = byteBuffer.array();
        } else {
            bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
        }

        String responseBodyString = new String(bytes); //Convert bytes to string

        //Properly format the response String
        responseBodyString = responseBodyString.trim().replaceAll("(\r\n|\n\r|\r|\n|\r0|\n0)", "");
        if (responseBodyString.endsWith("0")) {
            responseBodyString = responseBodyString.substring(0, responseBodyString.length()-1);
        }

        Log.i("responseBodyString", responseBodyString);

        this.responseBody = responseBodyString;

        Map<String, List<String>> headers = info.getAllHeaders(); //get headers

        String reqHeaders = createHeaders(headers);

        JSONObject results = new JSONObject();
        try {
            results.put("headers", reqHeaders);
            results.put("body", responseBodyString);
            results.put("statusCode", statusCode);
        } catch (JSONException e ) {
            e.printStackTrace();
        }

        //Send to OnFinishRequest which we will override in activity to read results gotten.
        delegate.onFinishRequest(results);
    }


    @Override
    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {

    }


    @Override
    public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {
        String inform = "CronetExceptionError: failed with status code - " + Integer.toString(info.getHttpStatusCode()) +
                ". Caused by: " + error.getLocalizedMessage() + " (" + info.getHttpStatusText()+ ").";

        JSONObject results = new JSONObject();
        try {
            results.put("headers", createHeaders(info.getAllHeaders()));
            results.put("body", inform);
            results.put("statusCode", info.getHttpStatusCode());
        } catch (JSONException e ) {
            e.printStackTrace();
        }

        //Send to OnFinishRequest which we will override in activity to read results gotten.
        delegate.onFinishRequest(results);
    }




    private String createHeaders(Map<String, List<String>> headers) {

        SharedPreferences authPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        String accessToken = "null";
        String client = "null";
        String uid = "null";
        long expiry = 0;

        if (headers.containsKey("Access-Token")) {
            List<String> accTok = headers.get("Access-Token");

            if (accTok.size() > 0) {
                accessToken = accTok.get(accTok.size()-1);
            }
        }

        if (headers.containsKey("Client")) {
            List<String> cl = headers.get("Client");

            if (cl.size() > 0) {
                client = cl.get(cl.size()-1);
            }
        }

        if (headers.containsKey("Uid")) {
            List<String> u = headers.get("Uid");

            if (u.size() > 0) {
                uid = u.get(u.size()-1);
            }
        }

        if (headers.containsKey("Expiry")) {
            List<String> ex = headers.get("Expiry");

            if (ex.size() > 0) {
                expiry = Long.parseLong(ex.get(ex.size()-1));
            }
        }

        JSONObject currentHeaders = new JSONObject();
        try {
            currentHeaders.put("access-token", accessToken);
            currentHeaders.put("client", client);
            currentHeaders.put("uid", uid);
            currentHeaders.put("expiry", expiry);

            return currentHeaders.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currentHeaders.toString();
    }

    public interface OnFinishRequest<JSONObject> {
        public void onFinishRequest(JSONObject result);

    }
}
