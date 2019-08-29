package com.jogtown.jogtown.subfragments;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.adapters.MyGroupsListRecyclerAdapter;
import com.jogtown.jogtown.utils.adapters.SearchGroupsListRecyclerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchGroupsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchGroupsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchGroupsListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static Activity instance;
    private OnFragmentInteractionListener mListener;

    private static ProgressBar progressBar;
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;

    private static List<Object> groupsResult;

    private static boolean loading;
    EditText searchGroupsEditText;
    Button searchGroupsButton;

    private static int page = 1;

    AdView mAdView;
    SharedPreferences settingsPref;

    public SearchGroupsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchGroupsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchGroupsListFragment newInstance(String param1, String param2) {
        SearchGroupsListFragment fragment = new SearchGroupsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this.getActivity();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_groups_list, container, false);
        progressBar = view.findViewById(R.id.search_groups_fragment_progressbar);
        recyclerView = view.findViewById(R.id.search_groups_fragment_recycler_view);
        searchGroupsEditText = view.findViewById(R.id.searchGroupsEditText);
        searchGroupsButton = view.findViewById(R.id.searchGroupsButton);

        searchGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchGroupsEditText.getText().toString();
                if (query.length() > 0) {
                    getGroups(query);
                } else {
                    getGroups(null);
                }
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        groupsResult = new ArrayList<>();
        setUpRecyclerView();
        getGroups(null);

        settingsPref = MainActivity.appContext.getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
        boolean showAds = settingsPref.getBoolean("showAds", true);
        if (showAds) {
            mAdView = view.findViewById(R.id.searchGroupsAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

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
        //There are two possible Activities: GroupJogActivity or GroupActivity

        adapter = new SearchGroupsListRecyclerAdapter(groupsResult);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }



    private static void showActivity() {

        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

    }





    public static void getGroups(String query) {
        groupsResult.clear();
        loading = true;
        showActivity();

        String url = MainActivity.appContext.getResources().getString(R.string.root_url) + "v1/search_groups";

        if (query != null) {
            url = url += "/?q=" + query;
        }
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode == 200) { //Some kind of success
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                            }
                        });
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() > 0) {

                            for (int i = 0; i < jsonArray.length(); i++) {
                                groupsResult.add(new JSONObject(jsonArray.get(i).toString()));
                            }
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //showButton();
                                adapter.notifyDataSetChanged();
                                page++;

                            }
                        });

                    } else if (statusCode > 399) { //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(instance);
                                alertDialogBuilder
                                        .setCancelable(true)
                                        .setMessage(responseBody)
                                        .setTitle("Error!");
                                alertDialogBuilder.create().show();

                            }
                        });

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

        NetworkRequest.get(url, new MyUrlRequestCallback(onFinishRequest));
    }

}
