package com.jogtown.jogtown.utils.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CoinsRecyclerViewAdapter extends RecyclerView.Adapter<CoinsRecyclerViewAdapter.MyViewHolder> {


    List<SkuDetails> skuDetailsList;
    BillingClient billingClient;
    Activity activity;

    public CoinsRecyclerViewAdapter(
            List<SkuDetails> skDList,
            BillingClient billingClientObj,
            Activity activityUsed
            ) {

        skuDetailsList = skDList;
        Collections.sort(skuDetailsList, new Comparator<SkuDetails>() {
            @Override
            public int compare(SkuDetails o1, SkuDetails o2) {
                return Double.compare(
                        Double.parseDouble(o1.getOriginalPrice().substring(1).replaceAll("[^\\d.]+", "")),
                        Double.parseDouble(o2.getOriginalPrice().substring(1).replaceAll("[^\\d.]+", ""))
                );
            }
        });

        billingClient = billingClientObj;
        activity = activityUsed;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View layout;
        ImageView coinImage;
        TextView coinTitle;
        TextView coinDescription;
        TextView coinPrice;
        Button buyCoinButton;

        public MyViewHolder(View view) {
            super(view);
            layout = view;
            coinImage = view.findViewById(R.id.coinImage);
            coinTitle = view.findViewById(R.id.coinTitle);
            coinDescription = view.findViewById(R.id.coinDescription);
            coinPrice = view.findViewById(R.id.coinPrice);
            buyCoinButton = view.findViewById(R.id.buyCoinButton);
        }
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.coins_layout, parent, false);
        CoinsRecyclerViewAdapter.MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final SkuDetails skuDetails = skuDetailsList.get(position);

        holder.coinDescription.setText(skuDetails.getDescription());
        holder.coinPrice.setText(skuDetails.getPrice());

        holder.buyCoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyButtonClicked(skuDetails);
            }
        });

        String coinsStr = skuDetails.getTitle().split(" ")[0];
        
        switch (Integer.parseInt(coinsStr)) {
            case 100:
                holder.coinTitle.setText("100 Coins");
                try {
                    Picasso.get().load(R.drawable.one_hundred_coins)
                            .resize(100, 100)
                            .into(holder.coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case 250:
                holder.coinTitle.setText("250 Coins");
                try {
                    Picasso.get().load(R.drawable.two_hundred_and_fifty_coins)
                            .resize(100, 100)
                            .into(holder.coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case 500:
                holder.coinTitle.setText("500 Coins");
                try {
                    Picasso.get().load(R.drawable.five_hundred_coins)
                            .resize(100, 100)
                            .into(holder.coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case 1000:
                holder.coinTitle.setText("1,000 Coins");
                try {
                    Picasso.get().load(R.drawable.one_thousand_coins)
                            .resize(100, 100)
                            .into(holder.coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                holder.coinTitle.setText("Unavailable");

        }
    }

    @Override
    public int getItemCount() {
        return skuDetailsList.size();
    }



    void buyButtonClicked(SkuDetails skuDetails) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        billingClient.launchBillingFlow(activity, flowParams);
    }

}
