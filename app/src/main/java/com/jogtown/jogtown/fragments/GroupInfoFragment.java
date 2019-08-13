package com.jogtown.jogtown.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.adapters.GroupInfoMemberListRecyclerViewAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Button joinGroupButton;
    Button editGroupButton;
    Button leaveGroupButton;

    ImageView groupBackgroundImage;
    ImageView groupAvatar;
    TextView groupNameText;
    TextView groupTaglineText;
    TextView groupInfoInviteCodeText;
    LinearLayout ratingsPlaceholder;
    LinearLayout groupInfoShareContainer;

    TextView groupInfoJoggersCount;
    TextView groupInfoDistanceCount;
    TextView groupInfoDurationCount;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    List<JSONObject> groupMembers;



    public GroupInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupInfoFragment newInstance(String param1, String param2) {
        GroupInfoFragment fragment = new GroupInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_group_info, container, false);

        joinGroupButton = view.findViewById(R.id.joinGroupButton);
        leaveGroupButton = view.findViewById(R.id.leaveGroupButton);
        editGroupButton = view.findViewById(R.id.editGroupButton);

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });

        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });

        editGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGroup();
            }
        });

        groupBackgroundImage = view.findViewById(R.id.group_background_image);
        groupAvatar = view.findViewById(R.id.group_avatar);
        groupNameText = view.findViewById(R.id.groupNameText);
        groupTaglineText = view.findViewById(R.id.groupTaglineText);
        groupInfoInviteCodeText = view.findViewById(R.id.groupInfoInviteCodeText);
        ratingsPlaceholder = view.findViewById(R.id.groupInfoBigRatingPlaceholder);
        groupInfoShareContainer = view.findViewById(R.id.groupInfoShareContainer);

        groupInfoJoggersCount = view.findViewById(R.id.groupInfoJoggersCount);
        groupInfoDistanceCount = view.findViewById(R.id.groupInfoDistanceCount);
        groupInfoDurationCount = view.findViewById(R.id.groupInfoDurationCount);

        recyclerView = view.findViewById(R.id.groupInfoMembersRecyclerView);


        try {
            groupNameText.setText(GroupActivity.groupObject.getString("name"));
            groupTaglineText.setText(GroupActivity.groupObject.getString("tagline"));
            groupInfoJoggersCount.setText(Integer.toString(GroupActivity.groupObject.getInt("members_count")));
            groupInfoDistanceCount.setText(Conversions.displayKilometres(GroupActivity.groupObject.getInt("total_distance")));
            groupInfoDurationCount.setText(Conversions.formatToHHMMSS(GroupActivity.groupObject.getInt("total_duration")));
            groupInfoInviteCodeText.setText("Invite Code: " + GroupActivity.groupObject.getString("invite_code"));

            Picasso.get().load(Uri.parse(GroupActivity.groupObject.getString("group_avatar")))
                    .resize(200, 200)
                    .into(groupAvatar);

            if (!GroupActivity.groupObject.isNull("background_image")) {
                Picasso.get().load(Uri.parse(GroupActivity.groupObject.getString("background_image")))
                        .fit()
                        .centerCrop()
                        .into(groupBackgroundImage);
            }

            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            switch (GroupActivity.groupObject.getInt("rating")) {
                case 5:
                    layoutInflater.inflate(R.layout.five_star_large_layout, ratingsPlaceholder);
                    break;
                case 4:
                    layoutInflater.inflate(R.layout.four_star_large_layout, ratingsPlaceholder);
                    break;
                case 3:
                    layoutInflater.inflate(R.layout.three_star_large_layout, ratingsPlaceholder);
                    break;
                case 2:
                    layoutInflater.inflate(R.layout.two_star_large_layout, ratingsPlaceholder);
                    break;
                case 1:
                    layoutInflater.inflate(R.layout.one_star_large_layout, ratingsPlaceholder);
                    break;
                default:
                    layoutInflater.inflate(R.layout.zero_star_large_layout, ratingsPlaceholder);

            }

            if (GroupActivity.groupObject.getBoolean("public") || GroupActivity.userIsAMember()) {
                groupInfoShareContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {

        }

        if (GroupActivity.userIsAMember() & !GroupActivity.userIsOwner()) {
            leaveGroupButton.setVisibility(View.VISIBLE);
        }
        if (GroupActivity.userIsOwner()) {
            editGroupButton.setVisibility(View.VISIBLE);
        }
        if (!GroupActivity.userIsAMember()) {
            joinGroupButton.setVisibility(View.VISIBLE);
        }


        setUpMemberList();
        setUpMemberListRecyclerView();

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


    public void joinGroup() {
    }

    public void editGroup() {
        Log.i("editGroup", "true");
    }

    public void leaveGroup() {

    }


    public void setUpMemberList() {
        groupMembers = new ArrayList<>();
        try {
            JSONArray jsonArray = GroupActivity.groupObject.getJSONArray("group_memberships");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                groupMembers.add(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setUpMemberListRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupInfoMemberListRecyclerViewAdapter(this.groupMembers);

        recyclerView.setAdapter(adapter);
    }


}
