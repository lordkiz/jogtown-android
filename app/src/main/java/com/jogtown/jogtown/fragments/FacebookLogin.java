package com.jogtown.jogtown.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.chromium.net.UrlRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FacebookLogin.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FacebookLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FacebookLogin extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String email = "email";
    private static final String public_profile = "public_profile";

    // TODO: Rename and change types of parameters
    private String userEmail;
    private String publicProfile;

    private OnFragmentInteractionListener mListener;

    CallbackManager callbackManager;

    public FacebookLogin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param email Parameter 1.
     * @param public_profile Parameter 2.
     * @return A new instance of fragment FacebookLogin.
     */
    // TODO: Rename and change types and number of parameters
    public static FacebookLogin newInstance(String email, String public_profile) {
        FacebookLogin fragment = new FacebookLogin();
        Bundle args = new Bundle();
        args.putString(email, email);
        args.putString(public_profile, public_profile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail = getArguments().getString(email);
            publicProfile = getArguments().getString(public_profile);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = null;
        try{
            view = inflater.inflate(R.layout.fragment_facebook_login, container);
            Button facebookLoginButton =  (Button) view.findViewById(R.id.facebookLoginButton);


            callbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    AccessToken accessToken = loginResult.getAccessToken();
                    getUserInformation(accessToken);
                }

                @Override
                public void onCancel() {
                    if (mListener != null) {
                        mListener.onFragmentInteraction(Uri.parse("loading: false"));
                    }
                }

                @Override
                public void onError(FacebookException error) {
                    if (mListener != null) {
                        mListener.onFragmentInteraction(Uri.parse("loading: false"));
                    }
                }
            });


            facebookLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginManager.getInstance().logInWithReadPermissions(
                            FacebookLogin.this,
                            Arrays.asList("public_profile", "email"));
                    if (mListener != null) {
                        //No need running on UI thread as this is already on the UI thread
                        mListener.onFragmentInteraction(Uri.parse("loading: true"));
                    }
                }
            });


            return view;
        } catch (Exception e) {
            throw e;
        }
        //return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    //Further FB registration Implementations below
    //To my own backend

    public void getUserInformation(AccessToken token) {

        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");
                    String uid = object.getString("id");
                    String profilePicture = "https://graph.facebook.com/" + uid + "/picture?type=large";
                    String provider = "facebook";

                    JSONObject payload = new JSONObject();

                    if (object.has("email") && object.has("name")) {

                        payload.put("name", name);
                        payload.put("email", email);
                        payload.put("uid", uid);
                        payload.put("profile_picture", profilePicture);
                        payload.put("provider", provider);
                        payload.put("device_id", MainActivity.deviceId);

                        String payloadString = payload.toString();


                        String url = getString(R.string.root_url) + "v1/auth";

                        MyUrlRequestCallback.OnFinishRequest callbackInfo = createNetworkRequestCallbackActions();

                        MyUrlRequestCallback myUrlRequestCallback = new MyUrlRequestCallback(callbackInfo);
                        loginUser(url, payloadString, myUrlRequestCallback);

                    } else {
                        //No name, no email
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder
                                .setCancelable(true)
                                .setMessage("Unable to retrieve email and name from facebook. " +
                                "This may be due to your privacy settings. " +
                                "Try using another login method."
                                )
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create().show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        runOnUiThread(Uri.parse("loading: false"));
                        Toast.makeText(getContext(), "An Error occurred.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    public void loginUser(String url, String payload,  MyUrlRequestCallback callback) {

        NetworkRequest.post(url, payload, callback);
    }



    public MyUrlRequestCallback.OnFinishRequest createNetworkRequestCallbackActions() {
        return new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode < 399)  { //Some kind of success
                        if (Auth.login(responseBody, headers)) {
                            if (mListener != null) {
                                //inform parent Activity
                                runOnUiThread(Uri.parse("loading: false"));
                                runOnUiThread(Uri.parse("redirect"));
                            }
                        } else {
                            //TODO: handle error
                            if (mListener != null) {
                                runOnUiThread(Uri.parse("loading: false"));
                            }
                        }
                    } else { //400 and above errors
                        if (mListener != null) {
                            runOnUiThread(Uri.parse("loading: false"));
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder
                                        .setCancelable(true)
                                        .setMessage(responseBody)
                                        .setTitle("Error!")
                                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialogBuilder.create().show();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        runOnUiThread(Uri.parse("loading: false"));
                    }
                }
            }
        };

    }



    public void onFBButtonClick(View view) {

    }

    public void runOnUiThread(final Uri uri) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mListener.onFragmentInteraction(uri);
            }
        });
    }

}
