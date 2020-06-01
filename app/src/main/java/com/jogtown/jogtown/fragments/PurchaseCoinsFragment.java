package com.jogtown.jogtown.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.utils.adapters.CoinsRecyclerViewAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PurchaseCoinsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PurchaseCoinsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PurchaseCoinsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private BillingClient billingClient;
    List<String> skuList = new ArrayList<>();
    SkuDetailsParams skuDetailsParams;
    List<SkuDetails> skuDetailsArray;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter mAdapter;

    boolean loading = true;
    ProgressBar progressBar;

    public PurchaseCoinsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PurchaseCoinsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PurchaseCoinsFragment newInstance(String param1, String param2) {
        PurchaseCoinsFragment fragment = new PurchaseCoinsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_purchase_coins, container, false);
        getActivity().setTheme(R.style.AppTheme);
        recyclerView = view.findViewById(R.id.coin_list_recycler_view);
        progressBar = view.findViewById(R.id.purchaseCoinProgressBar);

        shouldShowProgressBar();


        setUpBillingClient();

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


    void setUpBillingClient() {
        skuList.add("jogtown_coins_100");
        skuList.add("jogtown_coins_250");
        skuList.add("jogtown_coins_500");
        skuList.add("jogtown_coins_1000");


        billingClient = BillingClient.newBuilder(getApplicationContext()).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }

                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    Toast.makeText(getApplicationContext(), "Purchase Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle any other error codes.
                    Toast.makeText(getApplicationContext(),
                            "An unexpected error occurred. Please contact support",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    skuDetailsParams = SkuDetailsParams.newBuilder()
                            .setSkusList(skuList)
                            .setType(BillingClient.SkuType.INAPP)
                            .build();

                    billingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                            Log.i("skudetals", skuDetailsList.toString());
                            skuDetailsArray = skuDetailsList;

                            setUpRecyclerAdapter();

                            loading = false;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    shouldShowProgressBar();
                                }
                            });

                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });


    }


    void setUpRecyclerAdapter() {
        layoutManager = new LinearLayoutManager(getApplicationContext());
        mAdapter = new CoinsRecyclerViewAdapter(skuDetailsArray, billingClient, getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }



    void handlePurchase(Purchase purchase) {
        SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        int userCoins = authPref.getInt("coins", 0);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            SharedPreferences.Editor editor = authPref.edit();

            String purchaseSku = purchase.getSku();

            for (String sku : skuList) {
                if (sku.equals(purchaseSku)) {
                    int coinsToAward = Integer.parseInt(sku.split("_")[2]) + userCoins;
                    editor.putInt("coins", coinsToAward);
                    editor.apply();

                    updateCoinsInBackend(coinsToAward);

                    break;
                }
            }
        }

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                }
            });
        }
    }




    void updateCoinsInBackend(int coins) {
        loading = true;
        shouldShowProgressBar();

        SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        int userId = authPref.getInt("userId", 0);
        String url = getString(R.string.root_url) + "v1/users/" + Integer.toString(userId);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("coins", coins);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String payload = jsonObject.toString();

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                loading = false;
                try {
                    JSONObject jsonObj = new JSONObject(result.toString());
                    final int statusCode = jsonObj.getInt("statusCode");

                    if (statusCode < 300) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Coins updated", Toast.LENGTH_SHORT).show();
                                shouldShowProgressBar();
                                ProfileFragment.setCoinText();
                            }
                        });

                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                shouldShowProgressBar();
                                ProfileFragment.setCoinText();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    loading = false;
                    shouldShowProgressBar();
                }
            }
        };

        NetworkRequest.put(url, payload, new MyUrlRequestCallback(onFinishRequest));
    }



    void shouldShowProgressBar() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
