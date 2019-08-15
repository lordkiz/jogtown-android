package com.jogtown.jogtown.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.utils.adapters.GroupInfoMemberListRecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupLeaderboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupLeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupLeaderboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    List<JSONObject> sortedGroupMembers;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    TextView groupLeaderboardMembersPrivateNotAMemberText;

    public GroupLeaderboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupLeaderboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupLeaderboardFragment newInstance(String param1, String param2) {
        GroupLeaderboardFragment fragment = new GroupLeaderboardFragment();
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
        View view = inflater.inflate(R.layout.fragment_group_leaderboard, container, false);
        groupLeaderboardMembersPrivateNotAMemberText = view.findViewById(R.id.groupLeaderboardMembersPrivateNotAMemberText);
        recyclerView = view.findViewById(R.id.groupLeaderboardRecyclerView);

        boolean isAPublicGroup = false;

        try {
            isAPublicGroup = GroupActivity.groupObject.getBoolean("public");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!GroupActivity.userIsAMember() && !isAPublicGroup) {
            groupLeaderboardMembersPrivateNotAMemberText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }

        sortGroupMembers();
        setUpAdapter();
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



    private void sortGroupMembers() {
        List<JSONObject> groupMembers = new ArrayList<>();
        try {
            JSONArray jsonArray = GroupActivity.groupObject.getJSONArray("group_memberships");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                groupMembers.add(jsonObject);
            }
            Collections.sort(groupMembers, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject objectOne, JSONObject objectTwo) {
                    try {
                        int objOneTotalDistance = objectOne.getInt("total_distance");
                        int objTwoTotalDistance = objectTwo.getInt("total_distance");
                        //in reverse (descending)
                        return Integer.compare(objTwoTotalDistance, objOneTotalDistance);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            sortedGroupMembers = groupMembers;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setUpAdapter() {
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupInfoMemberListRecyclerViewAdapter(this.sortedGroupMembers, true);

        recyclerView.setAdapter(adapter);
    }



}
