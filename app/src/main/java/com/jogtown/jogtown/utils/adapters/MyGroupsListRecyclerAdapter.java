package com.jogtown.jogtown.utils.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class MyGroupsListRecyclerAdapter extends RecyclerView.Adapter<MyGroupsListRecyclerAdapter.MyViewHolder> {

    List<Object> groups;
    Activity activityToNavigateTo;

    public MyGroupsListRecyclerAdapter(@Nullable  Activity activityToNavigateTo, List<Object> groups) {
        this.groups = groups;
        this.activityToNavigateTo = activityToNavigateTo;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView joggersCount;
        ImageView groupAvatar;
        TextView groupName;
        TextView groupTagline;
        TextView joggersHeaderText;
        LinearLayout ratingsPlaceholder;

        public MyViewHolder (View view) {
            super(view);
            layout = view;
            joggersCount = view.findViewById(R.id.group_list_joggers_count);
            joggersHeaderText = view.findViewById(R.id.group_list_joggers_text);
            groupAvatar = view.findViewById(R.id.group_list_group_avatar);
            groupName = view.findViewById(R.id.group_list_group_name);
            groupTagline = view.findViewById(R.id.group_list_group_tagline);
            ratingsPlaceholder = view.findViewById(R.id.group_list_ratings_placeholder);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.group_list_item_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
        int userId = authPref.getInt("userId", 0);
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(groups.get(position).toString());
            boolean iAmAdmin = jsonObject.getInt("user_id") == userId;
            if (iAmAdmin) {
                //TODO: Show something on layout to signify I am admin
            }

            String joggersCount = Integer.toString(jsonObject.getInt("members_count"));
            holder.joggersCount.setText(joggersCount);

            if (jsonObject.getInt("members_count") == 1) {
                holder.joggersHeaderText.setText("JOGGER");
            }

            Uri uri = Uri.parse(jsonObject.getString("group_avatar"));
            Picasso.get().load(uri)
                    .resize(100,100)
                    .into(holder.groupAvatar);

            final String groupName = jsonObject.getString("name");
            holder.groupName.setText(groupName);


            String tagLine = jsonObject.getString("tagline");
            holder.groupTagline.setText(tagLine);

            final Activity activity = this.activityToNavigateTo;
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (activity != null) {
                        Intent intent = new Intent(MainActivity.appContext, activity.getClass());
                        intent.putExtra("group", jsonObject.toString());
                        MainActivity.appContext.startActivity(intent);
                    }

                }
            });

            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.appContext);

            if (holder.ratingsPlaceholder.getChildCount() == 0) {
                switch (jsonObject.getInt("rating")) {
                    case 5:
                        layoutInflater.inflate(R.layout.five_star_small_layout, holder.ratingsPlaceholder);
                        break;
                    case 4:
                        layoutInflater.inflate(R.layout.four_star_small_layout, holder.ratingsPlaceholder);
                        break;
                    case 3:
                        layoutInflater.inflate(R.layout.three_star_small_layout, holder.ratingsPlaceholder);
                        break;
                    case 2:
                        layoutInflater.inflate(R.layout.two_star_small_layout, holder.ratingsPlaceholder);
                        break;
                    case 1:
                        layoutInflater.inflate(R.layout.one_star_small_layout, holder.ratingsPlaceholder);
                        break;
                    default:
                        layoutInflater.inflate(R.layout.zero_star_small_layout, holder.ratingsPlaceholder);

                }
            }


        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }


}
