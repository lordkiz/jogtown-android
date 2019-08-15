package com.jogtown.jogtown.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.activities.GroupRunActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.models.Message;
import com.jogtown.jogtown.utils.adapters.GroupMessageRecyclerViewAdapter;
import com.jogtown.jogtown.utils.network.ActionCableSocket;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.LinearLayoutManagerWrapper;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    boolean loading = false;
    List<JSONObject> groupMessages;

    RecyclerView messagesRecyclerView;
    RecyclerView.LayoutManager messagesLayoutManager;
    RecyclerView.Adapter messagesRecyclerViewAdapter;

    ProgressBar progressBar;
    MessageInput messageInput;


    Subscription subscription;


    public GroupChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupChatFragment newInstance(String param1, String param2) {
        GroupChatFragment fragment = new GroupChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        messagesRecyclerView = view.findViewById(R.id.group_chat_fragment_recycler_view);
        progressBar = view.findViewById(R.id.group_chat_fragment_progress_bar);
        messageInput = view.findViewById(R.id.group_chat_fragment_message_input);

        setUpGroupMessages();
        setupGroupMessagesRecyclerAdapter();
        createSocket();
        setUpMessageInput();

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


    public void setUpGroupMessages() {
        groupMessages = new ArrayList<>();
        try {
            JSONArray jsonArray = GroupActivity.groupObject.getJSONArray("group_messages");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                groupMessages.add(jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setupGroupMessagesRecyclerAdapter() {
        messagesLayoutManager = new LinearLayoutManagerWrapper(MainActivity.appContext);
        //Using the wrapper I created here, refer to it in utils to know why I am using it instead
        ((LinearLayoutManager) messagesLayoutManager).setOrientation(RecyclerView.VERTICAL);

        messagesRecyclerViewAdapter = new GroupMessageRecyclerViewAdapter(groupMessages, true);
        messagesRecyclerViewAdapter.setHasStableIds(true);

        messagesRecyclerView.setLayoutManager(messagesLayoutManager);

        messagesRecyclerView.setAdapter(messagesRecyclerViewAdapter);

        messagesRecyclerView.scrollToPosition(messagesRecyclerViewAdapter.getItemCount() - 1);

    }



    public void createSocket() {
        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        final int userId = authPref.getInt("userId", 0);

        ActionCableSocket.OnSocketConnection socketConnection = new ActionCableSocket.OnSocketConnection() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onRejected() {

            }

            @Override
            public void onReceived(JSONObject jsonObject) {
                Log.i("message recieved", "true");
                if (isAGroupMessage(jsonObject)) {
                    try {
                        JSONObject msgObj = new JSONObject(jsonObject.get("message").toString());
                        if (userId != msgObj.getInt("user_id")) {
                            //if not current user's message then add.
                            //because we automatically added current user's message when sent.
                            groupMessages.add(msgObj);
                            notifyDatasetChanged();
                        }

                    } catch (JSONException e) {

                    }

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
                "GroupChannel",
                Integer.toString(GroupActivity.groupId),
                socketConnection
        );

        subscription = actionCableSocket.subscription;

    }

    public void notifyDatasetChanged() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messagesRecyclerViewAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messagesRecyclerViewAdapter.getItemCount() - 1);
            }
        });

    }


    public boolean isAGroupMessage(JSONObject jsonObject) {
        try {
            String data = jsonObject.getString("body");
            JSONObject dataObj = new JSONObject(data);
            if (dataObj.has("message")) {
                return true;
            }
        } catch (JSONException j) {
            j.printStackTrace();
        }
        return false;
    }


    public void setUpMessageInput() {
        final SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                try {
                    //Build the Message Object
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", Math.round(Math.random() * 10000000));
                    jsonObject.put("message_type", "text");
                    jsonObject.put("message_text", input);
                    jsonObject.put("system", false);
                    jsonObject.put("group_id", GroupActivity.groupId);
                    jsonObject.put("user_id", authPref.getInt("userId", 0));
                    jsonObject.put("sender_name", authPref.getString("name", "system"));
                    jsonObject.put("sender_avatar", authPref.getString("profilePicture", MainActivity.appContext.getResources().getString(R.string.default_profile_picture)));
                    jsonObject.put("image", null);
                    jsonObject.put("voice", null);
                    jsonObject.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));
                    jsonObject.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

                    //Add this object to group Messages
                    groupMessages.add(jsonObject);
                    notifyDatasetChanged();

                    String payload = jsonObject.toString();
                    //then send to backend
                    sendMessage(payload);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }


    public void sendMessage(String payload) {
        String url = getString(R.string.root_url) + "v1/group_messages";

        //Send Stuffs to backend: Stats, Message etc
        //All we are sending here is in real time though so I expect action cable to receive

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Ensure whatever was sent was sent
            }
        };

        NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));
    }

}
