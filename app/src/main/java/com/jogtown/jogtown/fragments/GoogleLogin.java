package com.jogtown.jogtown.fragments;

import android.app.Activity;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Auth;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.chromium.net.UrlRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GoogleLogin.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GoogleLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GoogleLogin extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    GoogleSignInClient googleSignInClient;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GoogleLogin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GoogleLogin.
     */
    // TODO: Rename and change types and number of parameters
    public static GoogleLogin newInstance(String param1, String param2) {
        GoogleLogin fragment = new GoogleLogin();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_google_login, container, false);
        Button googleLoginButton = (Button) view.findViewById(R.id.googleLoginButton);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(view.getContext(), googleSignInOptions);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);


                if (mListener != null) {
                    //No need running on UI thread as this is already on the UI thread
                    mListener.onFragmentInteraction(Uri.parse("loading: true"));
                }
            }
        });
        return view;
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

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 101:
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    try {
                        GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);

                        String name = googleSignInAccount.getDisplayName();
                        String email = googleSignInAccount.getEmail();
                        String uid = googleSignInAccount.getId();
                        Uri profilePicture = googleSignInAccount.getPhotoUrl();
                        if (profilePicture == null) {
                            profilePicture = Uri.parse(getString(R.string.default_profile_picture));
                        }
                        String provider = "google";

                        final JSONObject payload = new JSONObject();

                        payload.put("name", name);
                        payload.put("email", email);
                        payload.put("uid", uid);
                        payload.put("profile_picture", profilePicture.toString());
                        payload.put("provider", provider);
                        payload.put("device_id", MainActivity.deviceId);
                        String payloadString = payload.toString();

                        String url = getString(R.string.root_url) + "v1/auth";

                        //Sign out user from google after getting needed data
                        googleSignInClient.signOut();

                        MyUrlRequestCallback.OnFinishRequest callbackInfo = createNetworkRequestCallbackActions();
                        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(callbackInfo);
                        loginUser(url, payloadString, requestCallback);

                    } catch (ApiException e) {
                        e.printStackTrace();
                        if (mListener != null) {
                            runOnUiThread(Uri.parse("loading: false"));
                            Toast.makeText(getContext(), "An Error occurred.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mListener != null) {
                            runOnUiThread(Uri.parse("loading: false"));
                            Toast.makeText(getContext(), "An Error occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
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


    public void runOnUiThread(final Uri uri) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mListener.onFragmentInteraction(uri);
            }
        });
    }

}
