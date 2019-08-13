package com.jogtown.jogtown.subfragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupRunActivity;
import com.jogtown.jogtown.utils.adapters.MyGroupsListRecyclerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyGroupsListInDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyGroupsListInDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyGroupsListInDialogFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ProgressBar progressBar;
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    List<Object> myGroups;

    boolean loading;

    public MyGroupsListInDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyGroupsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyGroupsListInDialogFragment newInstance(String param1, String param2) {
        MyGroupsListInDialogFragment fragment = new MyGroupsListInDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_my_groups_list, container, false);
        progressBar = view.findViewById(R.id.my_groups_fragment_progressbar);
        recyclerView = view.findViewById(R.id.my_groups_fragment_recyclerview);
        myGroups = new ArrayList<>();
        setUpRecyclerView();
        getMyGroups();
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

    public void setUpRecyclerView() {

        layoutManager = new LinearLayoutManager(getContext());

        //Need the layout in MyGroupsListRecyclerAdapter know which activity to navigate to
        //when clicked.
        //There are two possible Activities: GroupRunActivity or GroupActivity

        adapter = new MyGroupsListRecyclerAdapter(new GroupRunActivity(), myGroups);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }



    public void showActivity() {

        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

    }





    public void getMyGroups() {
        loading = true;
        showActivity();

        String url = R.string.root_url + "v1/user_groups";
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                loading = false;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        showActivity();
                    }
                });
                try {
                    JSONObject res = new JSONObject(result.toString());
                    JSONArray resArr = new JSONArray(res.get("body"));
                    if (resArr.length() > 0) {
                        //Also check if we are getting the right kind of results
                        JSONObject testObj = new JSONObject(resArr.get(0).toString());
                        if (testObj.has("group_id")) {
                            //Good. the right result
                            for (int i = 0; i < resArr.length(); i++) {
                                myGroups.add(resArr.get(i));
                            }
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loading = false;
                    showActivity();
                }
            }
        };

        NetworkRequest.get(url, new MyUrlRequestCallback(onFinishRequest));
    }


}
