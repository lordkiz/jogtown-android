package com.jogtown.jogtown.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.subfragments.MyGroupsListFragment;
import com.jogtown.jogtown.subfragments.SearchGroupsListFragment;
import com.jogtown.jogtown.utils.adapters.ViewPagerAdapter;
import com.jogtown.jogtown.utils.ui.ZoomOutPageTransformer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    TabLayout tabLayout;
    ViewPager viewPager;


    public GroupsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupsFragment newInstance(String param1, String param2) {
        GroupsFragment fragment = new GroupsFragment();
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
        View view = inflater.inflate(R.layout.fragment_groups, container, false);


        tabLayout = (TabLayout) view.findViewById(R.id.groups_fragment_tabs);
        viewPager = (ViewPager) view.findViewById(R.id.groups_fragment_view_pager);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());


        addTabsToTabLayout(viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setElevation(0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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


    private void addTabsToTabLayout(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragmentAndTitle(new MyGroupsListFragment(), "My Groups");
        viewPagerAdapter.addFragmentAndTitle(new SearchGroupsListFragment(), "Other Groups");

        viewPager.setAdapter(viewPagerAdapter);
    }


}
