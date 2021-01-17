package com.jogtown.jogtown.subfragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
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
 * {@link RemoveAdFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RemoveAdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoveAdFragment extends Fragment {
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
    SkuDetails skuDetails;
    boolean loading = true;
    ProgressBar progressBar;
    LinearLayout removeAdsLayout;
    LinearLayout loadingRemoveAdsLayout;

    SharedPreferences authPref;

    public RemoveAdFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RemoveAdFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RemoveAdFragment newInstance(String param1, String param2) {
        RemoveAdFragment fragment = new RemoveAdFragment();
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
        View view =  inflater.inflate(R.layout.fragment_remove_ad, container, false);
        progressBar = view.findViewById(R.id.removeAdsProgressBar);
        removeAdsLayout = view.findViewById(R.id.removeAdsLayout);

        removeAdsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRemoveAdsFlow();
            }
        });

        loadingRemoveAdsLayout = view.findViewById(R.id.loadingRemoveAdsLayout);

        authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

        boolean isPremium = authPref.getBoolean("premium", false);
        if (!isPremium) {
            removeAdsLayout.setVisibility(View.GONE);
        }

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

        skuList.add("remove_ads");

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
                    String errorMsg = billingResult.getDebugMessage();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg + ". Please contact support",
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
                            
                            skuDetails = skuDetailsList.get(0);

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


    void handlePurchase(Purchase purchase) {
        SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        Boolean isPremium = authPref.getBoolean("premium", false);

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            SharedPreferences.Editor editor = authPref.edit();

            String purchaseSku = purchase.getSku();

            for (String sku : skuList) {
                if (sku.equals(purchaseSku)) {
                    editor.putBoolean("premium", true);
                    editor.apply();

                    updateBackend(true);

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


    void updateBackend(boolean bool) {
        loading = true;
        shouldShowProgressBar();

        final SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        int userId = authPref.getInt("userId", 0);
        String url = getString(R.string.root_url) + "v1/users/" + Integer.toString(userId);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("premium", bool);
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
                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = authPref.edit();
                                editor.putBoolean("premium", true);
                                editor.apply();
                                shouldShowProgressBar();
                            }
                        });

                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                shouldShowProgressBar();
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


    private void startRemoveAdsFlow() {
        if (skuDetails != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            Activity activity = getActivity();
            if (activity != null) {
                billingClient.launchBillingFlow(activity, flowParams);
            }
        }
    }


    void shouldShowProgressBar() {
        if (loading) {
            loadingRemoveAdsLayout.setVisibility(View.VISIBLE);
            removeAdsLayout.setVisibility(View.GONE);
        } else {
            loadingRemoveAdsLayout.setVisibility(View.GONE);

            boolean isPremium = authPref.getBoolean("premium", false);
            if (!isPremium) {
                removeAdsLayout.setVisibility(View.VISIBLE);
            }
        }
    }
}
