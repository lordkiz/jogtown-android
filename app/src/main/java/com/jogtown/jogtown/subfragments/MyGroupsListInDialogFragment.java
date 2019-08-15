package com.jogtown.jogtown.subfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.AppActivity;
import com.jogtown.jogtown.activities.GroupRunActivity;
import com.jogtown.jogtown.utils.adapters.MyGroupsListRecyclerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.DialogFragment;
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
public class MyGroupsListInDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Dialog rootDialog;

    private OnFragmentInteractionListener mListener;

    ProgressBar progressBar;
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    Button chooseGroupCreateANewGroupButton;

    List<Object> myGroups;

    boolean loading;
    int page = 1;

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
        View view = inflater.inflate(R.layout.choose_from_my_groups_layout, container, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootDialog != null) {
                    rootDialog.dismiss();
                }
            }
        });
        progressBar = view.findViewById(R.id.my_groups_fragment_progressbar);
        recyclerView = view.findViewById(R.id.my_groups_fragment_recyclerview);

        chooseGroupCreateANewGroupButton = view.findViewById(R.id.chooseGroupCreateANewGroupButton);

        myGroups = new ArrayList<>();
        setUpRecyclerView();
        setUpButton();
        getMyGroups();
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            rootDialog = dialog;
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(true);
        }
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
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootDialog != null) {
                    rootDialog.dismiss();
                }
            }
        });

        layoutManager = new LinearLayoutManager(getContext());

        //Need the layout in MyGroupsListRecyclerAdapter know which activity to navigate to
        //when clicked.
        //There are two possible Activities: GroupRunActivity or GroupActivity

        adapter = new MyGroupsListRecyclerAdapter(new GroupRunActivity(), myGroups);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    public void setUpButton() {
        chooseGroupCreateANewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootDialog != null) {
                    rootDialog.dismiss();
                }

                AppActivity.switchToMyGroupsTab();
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




    public void getMyGroups() {
        myGroups.clear();
        loading = true;
        showActivity();

        String url = getString(R.string.root_url) + "v1/user_groups/?" + "page=" + Integer.toString(page);
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
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
                        myGroups.addAll(resList);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //showButton();
                                adapter.notifyDataSetChanged();
                                page++;
                            }
                        });

                    } else if (statusCode > 399){ //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
