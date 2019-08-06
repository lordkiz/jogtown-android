package com.jogtown.jogtown.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.models.DefaultDialog;
import com.jogtown.jogtown.models.Message;
import com.jogtown.jogtown.utils.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.NetworkRequest;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InboxFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    DialogsListAdapter dialogsListAdapter;
    DialogsList inboxList;
    ProgressBar progressBar;
    Button loadMoreButton;

    Boolean loading = false;
    int page = 1;

    private OnFragmentInteractionListener mListener;

    public InboxFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InboxFragment newInstance(String param1, String param2) {
        InboxFragment fragment = new InboxFragment();
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
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        inboxList = view.findViewById(R.id.inboxList);
        progressBar = view.findViewById(R.id.inboxFragmentProgressBar);
        loadMoreButton = view.findViewById(R.id.inboxFragmentLoadMoreButton);
        setDialogListAdapter(view);
        getUserChats();
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


    private void setDialogListAdapter(View v) {
        int dialog = R.layout.fragment_inbox;
        dialogsListAdapter = new DialogsListAdapter<>(dialog, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);
            }
        });

        inboxList.setAdapter(dialogsListAdapter);
    }


    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

        }
    }


    public void showButton() {
        /*new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!loading && jogs.size() > 0 && jogs.size() % 10 == 0) {
                    loadMoreButton.setVisibility(View.VISIBLE);
                } else {
                    loadMoreButton.setVisibility(View.GONE);
                }
            }
        });*/
    }

    public void getUserChats() {
        loading = true;
        showActivity();

        String url = getString(R.string.root_url) + "v1/user_chats?page=" + page;

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = createNetworkRequestsCallbackActions();
        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);
        NetworkRequest.get(url, requestCallback);
    }




    public MyUrlRequestCallback.OnFinishRequest createNetworkRequestsCallbackActions() {
        return new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode == 200)  { //Some kind of success
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                SharedPreferences sharedPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

                                String currentUserName = sharedPreferences.getString("name", "");
                                String currentUserAvatar = sharedPreferences.getString("profilePicture", "");

                                try {

                                    JSONArray jsonArray = new JSONArray(responseBody);
                                    List<IDialog> resList = new ArrayList<>();

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        //Create a Chatkit dialog of each chat
                                        DefaultDialog dialog = new DefaultDialog();
                                        IMessage lastMessage = new Message();

                                        String chatObj = jsonArray.get(i).toString();
                                        JSONObject chat = new JSONObject(chatObj);

                                        dialog.setChatId(Integer.toString(chat.getInt("id")));
                                        if (currentUserName.equals(chat.getString("recipient_name"))) {
                                            dialog.setDialogName(chat.getString("sender_name"));
                                        } else {
                                            dialog.setDialogName(chat.getString("recipient_name"));
                                        }

                                        if (currentUserAvatar.equals(chat.getString("recipient_avatar"))) {
                                            dialog.setDialogPhoto(chat.getString("sender_avatar"));
                                        } else {
                                            dialog.setDialogPhoto(chat.getString("recipient_avatar"));
                                        }

                                        dialog.setLastMessage(lastMessage);

                                        resList.add(dialog);

                                    }

                                    dialogsListAdapter.addItems(resList);
                                    page++;

                                    showButton();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                    } else if (statusCode > 399){ //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                showButton();
                            }
                        });
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder
                                .setCancelable(true)
                                .setMessage(responseBody)
                                .setTitle("Error!");
                        alertDialogBuilder.create().show();

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





}
