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
import android.widget.ImageView;
import android.widget.TextView;

import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.models.Message;
import com.jogtown.jogtown.utils.ActionCableSocket;
import com.jogtown.jogtown.utils.GroupRunMembersRecyclerAdapter;
import com.jogtown.jogtown.utils.LinearLayoutManagerWrapper;
import com.jogtown.jogtown.utils.PicassoCircle;
import com.squareup.picasso.Picasso;
import com.txusballesteros.widgets.FitChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupRunMembersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupRunMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupRunMembersFragment extends Fragment {
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

    RecyclerView membersRecyclerView;
    RecyclerView.LayoutManager membersLayoutManager;
    RecyclerView.Adapter membersRecyclerViewAdapter;


    FitChart joggerCountChart;
    TextView joggerActiveCountText;
    TextView joggerAllCountText;
    SharedPreferences authPref;
    SharedPreferences jogPref;

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


    public GroupRunMembersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupRunMembersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupRunMembersFragment newInstance(String param1, String param2) {
        GroupRunMembersFragment fragment = new GroupRunMembersFragment();
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
        View view =  inflater.inflate(R.layout.fragment_group_run_members, container, false);

        membersRecyclerView = view.findViewById(R.id.group_run_members_fragment_recycler_view);
        joggerActiveCountText = view.findViewById(R.id.joggerActiveCountText);
        joggerAllCountText = view.findViewById(R.id.joggerAllCountText);


        authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        jogPref = MainActivity.appContext.getSharedPreferences("JogPreferences", Context.MODE_PRIVATE);

        joggerCountChart = view.findViewById(R.id.group_run_members_fragment_jogger_count_chart);
        joggerCountChart.setMinValue(0f);
        joggerCountChart.setMaxValue(100f);


        groupMembers = new ArrayList<>();

        setupGroupMembersRecyclerAdapter();

        createSocket();

        streamCurrentUserJog();
        streamJogHandler.postDelayed(userJogStreamRunnable, 60000); //Stream userjog every min

        updateJoggerCountChart();
        handler.postDelayed(joggerCountUpdaterRunnable, 60000); //update chart every minute

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


    public void streamCurrentUserJog() {
        String avatar = authPref.getString("profilePicture", getString(R.string.default_profile_picture));
        String userName = authPref.getString("name", "-");
        int userId = authPref.getInt("userId", 0);

        int distance = jogPref.getInt("distance", 0);
        int duration = jogPref.getInt("duration", 0);

        JSONObject currentUserJog = new JSONObject();
        try {
            currentUserJog.put("user_name", userName);
            currentUserJog.put("user_id", userId);
            currentUserJog.put("user_avatar", avatar);
            currentUserJog.put("distance", distance);
            currentUserJog.put("duration", duration);
            currentUserJog.put("jogging", true);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        groupMembers.add(0, currentUserJog);
        notifyDatasetChanged();

        //TODO stream jog to other users

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

    public void notifyDatasetChanged() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                membersRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    public void notifyItemInserted(final int position) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                membersRecyclerViewAdapter.notifyItemInserted(position);
            }
        });
    }

    public void notifyItemRemoved(final int position) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                membersRecyclerViewAdapter.notifyItemRemoved(position);
            }
        });
    }

    public void createSocket() {
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
                    //TODO - work a message

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
                Integer.toString(1),
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
                    groupMembers.remove(i);
                    notifyItemRemoved(i);

                    if (isJogging) {
                        //if jogging true, move to front of the list after currentuser
                        groupMembers.add(1, jsonObject);

                        notifyDatasetChanged();
                        return; //Exit the function
                    } else {
                        groupMembers.add(i, jsonObject);

                        notifyDatasetChanged();
                        return;
                    }
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
                notifyDatasetChanged();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            // Send to the back of the list
            try {
                groupMembers.add(jsonObject);
                notifyDatasetChanged();
            }catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }



    public void updateJoggerCountChart() {
        int joggers = 0;
        for (int i = 0; i < groupMembers.size(); i++) {
            JSONObject obj = groupMembers.get(i);
            try {
                boolean isAJogger = obj.getBoolean("jogging");
                if (isAJogger) {
                    joggers++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        float percentage = joggers > 0 ? ((joggers / (float)groupMembers.size()) * 100f) : 0f;
        Log.i("percentage got", Float.toString(percentage));
        joggerCountChart.setValue(percentage);

        String jogPhrase = joggers > 1 ? " Joggers " : " Jogger ";

        String active = Integer.toString(joggers) + jogPhrase + "active now";
        String all = "out of " + Integer.toString(groupMembers.size()) + " Joggers in group";
        joggerActiveCountText.setText(active);
        joggerAllCountText.setText(all);
    }

}
