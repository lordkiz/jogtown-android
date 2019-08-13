package com.jogtown.jogtown.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchGroupsListRecyclerAdapter extends RecyclerView.Adapter<SearchGroupsListRecyclerAdapter.MyViewHolder> {

    List<Object> groups;

    public SearchGroupsListRecyclerAdapter(List<Object> groups) {
        this.groups = groups;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView joggersCount;
        ImageView groupAvatar;
        TextView groupName;
        TextView groupTagline;

        public MyViewHolder (View view) {
            super(view);
            layout = view;
            joggersCount = view.findViewById(R.id.group_list_joggers_count);
            groupAvatar = view.findViewById(R.id.group_list_group_avatar);
            groupName = view.findViewById(R.id.group_list_group_name);
            groupTagline = view.findViewById(R.id.group_list_group_tagline);
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


            holder.layout.setBackgroundColor(MainActivity.appContext.getResources().getColor(R.color.white));

            String joggersCount = Integer.toString(jsonObject.getInt("members_count"));
            holder.joggersCount.setText(joggersCount);
            holder.joggersCount.setTextColor(MainActivity.appContext.getResources().getColor(R.color.darkOrange));

            Uri uri = Uri.parse(jsonObject.getString("group_avatar"));
            Picasso.get().load(uri)
                    .resize(60,60)
                    .into(holder.groupAvatar);

            final String groupName = jsonObject.getString("name");
            holder.groupName.setText(groupName);
            holder.groupName.setTextColor(MainActivity.appContext.getResources().getColor(R.color.extraDarkSmoke));


            String tagLine = jsonObject.getString("tagline");
            holder.groupTagline.setText(tagLine);
            holder.groupTagline.setTextColor(MainActivity.appContext.getResources().getColor(R.color.extraDarkSnow));

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.appContext, GroupActivity.class);
                    intent.putExtra("group", jsonObject.toString());
                    MainActivity.appContext.startActivity(intent);
                }

            });


        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }


}
