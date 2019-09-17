package com.jogtown.jogtown.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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
import com.jogtown.jogtown.fragments.ProfileFragment;
import com.jogtown.jogtown.utils.adapters.CoinsRecyclerViewAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PurchaseCoinsActivity extends AppCompatActivity {

    private BillingClient billingClient;
    List<String> skuList = new ArrayList<>();
    SkuDetailsParams skuDetailsParams;
    List<SkuDetails> skuDetailsArray;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter mAdapter;

    boolean loading = true;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_coins);

        recyclerView = findViewById(R.id.coin_list_recycler_view);
        progressBar = findViewById(R.id.purchaseCoinProgressBar);

        shouldShowProgressBar();


        setUpBillingClient();

        //Set up recycler after we are sure the billing client is ready
    }




    void setUpBillingClient() {
        skuList.add("jogtown_coins_100");
        skuList.add("jogtown_coins_250");
        skuList.add("jogtown_coins_500");
        skuList.add("jogtown_coins_1000");


        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
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
        mAdapter = new CoinsRecyclerViewAdapter(skuDetailsArray, billingClient, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }



    void handlePurchase(Purchase purchase) {
        SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
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

        SharedPreferences authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
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
