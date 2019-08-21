package com.jogtown.jogtown.fragments;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupJogActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.adapters.GroupMessageRecyclerViewAdapter;
import com.jogtown.jogtown.utils.network.ActionCableSocket;
import com.jogtown.jogtown.utils.adapters.GroupRunMembersRecyclerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.LinearLayoutManagerWrapper;
import com.stfalcon.chatkit.messages.MessageInput;
import com.txusballesteros.widgets.FitChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupJogMembersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupJogMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupJogMembersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Subscription subscription;

    List<JSONObject> groupMembers;
    static JSONObject currentUserMembershipObject;

    RecyclerView membersRecyclerView;
    RecyclerView.LayoutManager membersLayoutManager;
    RecyclerView.Adapter membersRecyclerViewAdapter;



    List<JSONObject> groupMessages;

    RecyclerView messagesRecyclerView;
    RecyclerView.LayoutManager messagesLayoutManager;
    RecyclerView.Adapter messagesRecyclerViewAdapter;

    FitChart joggerCountChart;
    FitChart joggerTotalMembersChart;
    FitChart joggerTotalKmChart;
    TextView joggerActiveCountText;
    TextView joggerTotalMembersText;
    TextView joggerActiveTotalKmText;

    MessageInput messageInput;

    private Handler handler = new Handler();
    private Runnable joggerCountUpdaterRunnable = new Runnable() {
        @Override
        public void run() {
            updateJoggerCountChart();
            handler.postDelayed(this, 60000);
        }
    };

    private Handler streamJogHandler = new Handler();
    private Runnable userJogStreamRunnable = new Runnable() {
        @Override
        public void run() {
            streamCurrentUserJog();
            handler.postDelayed(this, 60000);
        }
    };


    public GroupJogMembersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupJogMembersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupJogMembersFragment newInstance(String param1, String param2) {
        GroupJogMembersFragment fragment = new GroupJogMembersFragment();
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
        View view =  inflater.inflate(R.layout.fragment_group_jog_members, container, false);

        membersRecyclerView = view.findViewById(R.id.group_run_members_fragment_recycler_view);
        messagesRecyclerView = view.findViewById(R.id.group_run_fragment_messages_recycler_view);

        joggerActiveCountText = view.findViewById(R.id.joggerActiveCountText);
        joggerTotalMembersText = view.findViewById(R.id.joggerTotalMembersText);
        joggerActiveTotalKmText = view.findViewById(R.id.joggerActiveTotalKmText);

        joggerCountChart = view.findViewById(R.id.group_run_members_fragment_jogger_count_chart);
        joggerTotalMembersChart = view.findViewById(R.id.group_run_members_fragment_total_members_chart);
        joggerTotalKmChart = view.findViewById(R.id.group_run_members_fragment_total_km_chart);

        messageInput = view.findViewById(R.id.group_run_fragment_message_input);

        joggerCountChart.setMinValue(0f);
        joggerCountChart.setMaxValue(100f);

        joggerTotalKmChart.setMinValue(0f);
        joggerTotalKmChart.setMaxValue(100f);


        joggerTotalMembersChart.setMinValue(0f);
        joggerTotalMembersChart.setMaxValue(100f);


        setUpGroupMembers();
        setUpGroupMessages();

        setupGroupMembersRecyclerAdapter();
        setupGroupMessagesRecyclerAdapter();

        setupMessageInput();

        createSocket();

        streamCurrentUserJog();
        streamJogHandler.postDelayed(userJogStreamRunnable, 60000); //Stream userjog every min

        updateJoggerCountChart();
        handler.postDelayed(joggerCountUpdaterRunnable, 60000); //update chart every minute

        notifyOthersThatUSerHasStarted();

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
        handler.removeCallbacks(joggerCountUpdaterRunnable);
        streamJogHandler.removeCallbacks(userJogStreamRunnable);
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

    private void setUpGroupMessages() {
        groupMessages = new ArrayList<>();
        try {
            JSONArray jsonArray = GroupJogActivity.groupObject.getJSONArray("group_messages");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                groupMessages.add(jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpGroupMembers() {

        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        int currentUserId = authPref.getInt("userId", 0);

        groupMembers = new ArrayList<>();
        try {
            JSONArray jsonArray = GroupJogActivity.groupObject.getJSONArray("group_memberships");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                groupMembers.add(jsonObject);
                if (jsonObject.getInt("user_id") == currentUserId) {
                    currentUserMembershipObject = jsonObject;

                    //toggle jogging to true in the backend
                    JSONObject obj = new JSONObject();
                    obj.put("jogging", true);
                    String payload = obj.toString();
                    String url = MainActivity.appContext.getResources().getString(R.string.root_url) +
                            "v1/group_memberships/" +
                            Integer.toString(currentUserMembershipObject.getInt("id"));
                    send(url, payload, "PUT");

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void streamCurrentUserJog() {
        JSONObject currentUserJog = currentUserMembershipObject;
        if (currentUserJog != null) {
            try {
                SharedPreferences jogPref = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);

                int distance = jogPref.getInt("distance", 0);
                int duration = jogPref.getInt("duration", 0);

                int currentUserMembershipId = currentUserJog.getInt("id");

                currentUserJog.remove("current_distance");
                currentUserJog.remove("current_duration");
                currentUserJog.remove("jogging");

                currentUserJog.put("current_distance", distance);
                currentUserJog.put("current_duration", duration);
                currentUserJog.put("jogging", true);

                addGroupMembershipItem(currentUserJog);

                String url = MainActivity.appContext.getResources().getString(R.string.root_url) + "v1/group_memberships/" + Integer.toString(currentUserMembershipId);
                String payload = currentUserJog.toString();
                send(url, payload, "PUT");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }


    public static void saveGroupMembershipStats(int distance, int duration, boolean jogStatus ) {
        String method = "PUT";
        int currentUserMembershipId = 0;
        try {
            currentUserMembershipId = currentUserMembershipObject.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = MainActivity.appContext.getResources().getString(R.string.root_url) + "v1/group_memberships/" + Integer.toString(currentUserMembershipId);
        JSONObject jsonObject = currentUserMembershipObject;
        if (jsonObject != null) {
            try {
                jsonObject.remove("current_distance");
                jsonObject.remove("current_duration");
                jsonObject.remove("jogging");

                jsonObject.put("current_distance", distance);
                jsonObject.put("current_duration", duration);
                jsonObject.put("jogging", jogStatus);

                String payload = jsonObject.toString();
                send(url, payload, method);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    public void setupGroupMembersRecyclerAdapter() {
        membersLayoutManager = new LinearLayoutManagerWrapper(MainActivity.appContext);
        //Using the wrapper I created here, refer to it in utils to know why I am using it instead
        ((LinearLayoutManager) membersLayoutManager).setOrientation(RecyclerView.HORIZONTAL);

        membersRecyclerViewAdapter = new GroupRunMembersRecyclerAdapter(groupMembers);
        membersRecyclerViewAdapter.setHasStableIds(true);

        membersRecyclerView.setLayoutManager(membersLayoutManager);

        membersRecyclerView.setAdapter(membersRecyclerViewAdapter);

    }


    public void setupGroupMessagesRecyclerAdapter() {
        messagesLayoutManager = new LinearLayoutManagerWrapper(MainActivity.appContext);
        //Using the wrapper I created here, refer to it in utils to know why I am using it instead
        ((LinearLayoutManager) messagesLayoutManager).setOrientation(RecyclerView.VERTICAL);

        messagesRecyclerViewAdapter = new GroupMessageRecyclerViewAdapter(groupMessages, false);
        messagesRecyclerViewAdapter.setHasStableIds(true);

        messagesRecyclerView.setLayoutManager(messagesLayoutManager);

        messagesRecyclerView.setAdapter(messagesRecyclerViewAdapter);

        messagesRecyclerView.scrollToPosition(messagesRecyclerViewAdapter.getItemCount() -1);

    }

    public void notifyDatasetChanged(String dataSetName) {
        if (dataSetName.equals("groupMessages")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    messagesRecyclerViewAdapter.notifyDataSetChanged();
                    messagesRecyclerView.scrollToPosition(messagesRecyclerViewAdapter.getItemCount() -1);
                }
            });

        } else if (dataSetName.equals("groupMembers")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    membersRecyclerViewAdapter.notifyDataSetChanged();
                }
            });
        }

    }


    public void notifyItemInserted(final int position, String dataSetName) {
        if (dataSetName.equals("groupMessages")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    messagesRecyclerViewAdapter.notifyItemInserted(position);
                }
            });
        } else if (dataSetName.equals("groupMembers")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    membersRecyclerViewAdapter.notifyItemInserted(position);
                }
            });
        }
    }


    public void notifyItemRemoved(final int position, String dataSetName) {
        if (dataSetName.equals("groupMessages")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    messagesRecyclerViewAdapter.notifyItemRemoved(position);
                }
            });
        } else if (dataSetName.equals("groupMembers")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    membersRecyclerViewAdapter.notifyItemRemoved(position);
                }
            });
        }
    }

    public void createSocket() {

        final SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        ActionCableSocket.OnSocketConnection socketConnection = new ActionCableSocket.OnSocketConnection() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onRejected() {

            }

            @Override
            public void onReceived(JSONObject jsonObject) {
                if (isAGroupMessage(jsonObject)) {
                    try {
                        JSONObject msgObj = new JSONObject(jsonObject.get("message").toString());
                        groupMessages.add(msgObj);
                        notifyDatasetChanged("groupMessages");

                    } catch (JSONException e) {

                    }

                } else if (isAGroupMembership(jsonObject)) {

                    try {
                        JSONObject object = new JSONObject(jsonObject.get("group_membership").toString());
                        boolean isFromCurrentUSer = object.getInt("user_id") == authPref.getInt("userId", 0);
                        if (!isFromCurrentUSer) {
                            //Not from currentUser
                            addGroupMembershipItem(object);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                Integer.toString(GroupJogActivity.groupId),
                socketConnection
        );

        subscription = actionCableSocket.subscription;

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

    public boolean isAGroupMembership(JSONObject jsonObject) {
        try {
            String data = jsonObject.getString("body");
            JSONObject dataObj = new JSONObject(data);
            if (dataObj.has("group_membership")) {
                return true;
            }
        } catch (JSONException j) {
            j.printStackTrace();
        }
        return false;
    }


    public void addGroupMembershipItem(JSONObject jsonObject) {
        //the list will contain identical objects if we just
        //add whatever jsonObject.
        //First find the group_membership with a jsonobject user_id remove it and
        //replace it with the new one.
        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        int currentUserId = authPref.getInt("userId", 0);

        boolean isJogging = false;
        try {
            isJogging = jsonObject.getBoolean("jogging");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < groupMembers.size(); i++) {
            Object obj = groupMembers.get(i);
            try {
                JSONObject object = new JSONObject(obj.toString());
                if (object.getInt("user_id") == jsonObject.getInt("user_id")) {
                    //The group Membership is already in the list
                    if (object.getInt("user_id") == currentUserId) {
                        //it is current User's so it should be first on list
                        if (groupMembers.size() > 0) {
                            groupMembers.remove(0);
                            notifyDatasetChanged("groupMembers");
                            groupMembers.add(0, jsonObject);
                            notifyDatasetChanged("groupMembers");
                        } else {
                            groupMembers.add(0, jsonObject);
                            notifyDatasetChanged("groupMembers");
                        }
                    } else {
                        groupMembers.remove(i);
                        notifyItemRemoved(i, "groupMembers");

                        if (isJogging) {
                            //if jogging true, move to front of the list after currentuser
                            groupMembers.add(1, jsonObject);

                            notifyDatasetChanged("groupMembers");
                        } else {
                            groupMembers.add(i, jsonObject);

                            notifyDatasetChanged("groupMembers");
                        }
                    }

                    return; //Exit the for loop
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        //Always add jsonObject where jogging is true to front of the line
        //Behind current user's own though.
        if (isJogging) {
            try {
                groupMembers.add(1, jsonObject);
                notifyDatasetChanged("groupMembers");
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            // Send to the back of the list
            try {
                groupMembers.add(jsonObject);
                notifyDatasetChanged("groupMembers");
            }catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }




    public void updateJoggerCountChart() {
        int joggers = 0;
        int totalMeters = 0;
        try {
            totalMeters = GroupJogActivity.groupObject.getInt("total_distance");
            for (int i = 0; i < groupMembers.size(); i++) {
                JSONObject obj = groupMembers.get(i);

                boolean isAJogger = obj.getBoolean("jogging");
                int currentDistance = obj.getInt("current_distance");
                totalMeters += currentDistance;
                if (isAJogger) {
                    joggers++;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        float percentage = joggers > 0 ? ((joggers / (float)groupMembers.size()) * 100f) : 0f;
        Log.i("percentage got", Float.toString(percentage));

        joggerCountChart.setValue(percentage);
        joggerTotalMembersChart.setValue(100f);

        String active = Integer.toString(joggers);
        String totalMembers = Integer.toString(groupMembers.size());
        String totalKm = Conversions.displayKilometres(totalMeters);

        joggerActiveCountText.setText(active);
        joggerTotalMembersText.setText(totalMembers);
        joggerActiveTotalKmText.setText(totalKm);
    }


    public void notifyOthersThatUSerHasStarted() {

        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        int groupId = GroupJogActivity.groupId;

        String userName = authPref.getString("name", "user");
        int userId = authPref.getInt("userId", 0);
        String messageText = userName + " has started jogging";
        try {
            JSONObject payload = new JSONObject();
            payload.put("message_text", messageText);
            payload.put("message_type", "text");
            payload.put("system", true);
            payload.put("group_id", groupId);
            payload.put("user_id", userId);

            String payloatStr = payload.toString();
            String url = getString(R.string.root_url) + "v1/group_messages";
            send(url, payloatStr, "POST");

        } catch (JSONException e) {

        }

    }

    public void setupMessageInput() {
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                try {
                    SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
                    //Build the Message Object
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", Math.round(Math.random() * 10000000));
                    jsonObject.put("message_type", "text");
                    jsonObject.put("message_text", input);
                    jsonObject.put("system", false);
                    jsonObject.put("group_id", GroupJogActivity.groupId);
                    jsonObject.put("user_id", authPref.getInt("userId", 0));
                    jsonObject.put("sender_name", authPref.getString("name", "system"));
                    jsonObject.put("sender_avatar", authPref.getString("profilePicture", MainActivity.appContext.getResources().getString(R.string.default_profile_picture)));
                    jsonObject.put("image", null);
                    jsonObject.put("voice", null);
                    jsonObject.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));
                    jsonObject.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

                    //Add this object to group Messages
                    groupMessages.add(jsonObject);
                    notifyDatasetChanged("groupMessages");


                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);

                    String payload = jsonObject.toString();
                    String url = getString(R.string.root_url) + "/v1/group_messages";
                    send(url, payload, "POST");
                    return true;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return false;
            }
        });
    }


    /// NETWORK REQUESTS
    private static void send(String url, String payload, String method) {

        //Send Stuffs to backend: Stats, Message etc
        //All we are sending here is in real time though so I expect action cable to receive

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Ensure whatever was sent was sent
            }
        };

        if (method.equals("POST")) {
            NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));
        } else if (method.equals("PUT")) {
            NetworkRequest.put(url, payload, new MyUrlRequestCallback(onFinishRequest));
        }
    }


}
