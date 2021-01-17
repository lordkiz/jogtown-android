package com.jogtown.jogtown.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.adapters.HistoryRecyclerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.MyTypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.SocketHandler;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //this.initialState:
    Boolean loading = false;
    List<Object> jogs = new ArrayList<>();
    int page = 1;

    //Others

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter mAdapter;

    ProgressBar progressBar;
    Button loadMoreButton;

    LinearLayout historyFragmentEmptyLayout;
    AdView mAdView;
    SharedPreferences authPref;


    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        try {
            ActionBar actionBar =  ((AppCompatActivity) getActivity()).getSupportActionBar();

            SpannableString spannableString = new SpannableString("History");
            spannableString.setSpan(
                    new MyTypefaceSpan(getContext(), "fonts/baijamjuree_semi_bold.ttf"),
                    0,
                    spannableString.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            actionBar.setTitle(spannableString);
        } catch (NullPointerException e) {
            //
        }
        authPref = getContext().getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        
        boolean showAds = authPref.getBoolean("premium", false);
        if (showAds) {

            mAdView = view.findViewById(R.id.historyAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        historyFragmentEmptyLayout = view.findViewById(R.id.history_fragment_empty_layout);

        progressBar = view.findViewById(R.id.history_fragment_progess_bar);
        progressBar.setVisibility(View.GONE);

        loadMoreButton = view.findViewById(R.id.loadMoreHistoryButton);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreUserJogs();
//                showButton();
            }
        });
        loadMoreButton.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.history_items_recycler_view);
        setUpRecyclerView();

        getUserJogs();
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




    //My fxns
    public void getUserJogs() {
        loading = true;
        hideEmptyLayout();
        showActivity();

        String url = getString(R.string.root_url) + "v1/user_runs?page=" + page;
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = createNetworkRequestsCallbackActions();
        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);
        NetworkRequest.get(url, requestCallback);

    }

    private void setUpRecyclerView() {
        //recyclerview already set up in onCreateView
        layoutManager = new LinearLayoutManager(MainActivity.appContext);
        mAdapter = new HistoryRecyclerAdapter(jogs);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    loadMoreButton.setVisibility(View.VISIBLE);
                } else {
                    loadMoreButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

        }
    }

    private void showEmptyLayout() {
        if (jogs.size() > 0) {
            historyFragmentEmptyLayout.setVisibility(View.GONE);
        } else {
            historyFragmentEmptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyLayout() {
        historyFragmentEmptyLayout.setVisibility(View.GONE);
    }

    public void notifyDatasetChanged() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void loadMoreUserJogs() {
        getUserJogs();
    }

    public void showButton() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!loading && jogs.size() > 0 && jogs.size() % 10 == 0) {
                    loadMoreButton.setVisibility(View.VISIBLE);
                } else {
                    loadMoreButton.setVisibility(View.GONE);
                }
            }
        });
    }

    public void scrollToBottom(final int position) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(position);
            }
        });
    }


    MyUrlRequestCallback.OnFinishRequest createNetworkRequestsCallbackActions() {
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
                            }
                        });
                        JSONArray jsonArray = new JSONArray(responseBody);
                        List<Object> resList = new ArrayList<>();
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                resList.add(jsonArray.get(i));
                            }
                        }
                        jogs.addAll(resList);
                        notifyDatasetChanged();
                        if (jogs.size() > 0 && page > 1) {
                            scrollToBottom((jogs.size() - resList.size()) - 1);
                            //we want to scroll to the last position the user stopped,
                            //which is the jog.size() -1 before concatenating with resList.
                        }
                        page++;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
//                                showButton();

                                showEmptyLayout();
                            }
                        });

                    } else if (statusCode > 399){ //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                showEmptyLayout();
                                try {
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
                                }catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

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
                            showEmptyLayout();
                        }
                    });
                }
            }
        };
    }

}
