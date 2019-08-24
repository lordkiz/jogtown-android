package com.jogtown.jogtown.utils.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.ui.PicassoCircle;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GroupRunMembersRecyclerAdapter extends RecyclerView.Adapter<GroupRunMembersRecyclerAdapter.MyViewHolder>{

    public List<Object> groupMembers;

    public GroupRunMembersRecyclerAdapter(List groupMembers) {
        this.groupMembers = groupMembers;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        View layout;
        ImageView userAvatar;
        TextView userName;
        TextView jogging;
        TextView userDistance;
        TextView userDuration;
        TextView userPace;
        TextView userSpeed;

        public MyViewHolder(View view) {
            super(view);
            layout = view;
            userAvatar = view.findViewById(R.id.group_run_members_fragment_user_avatar);
            userName = view.findViewById(R.id.group_run_members_fragment_user_name);
            jogging = view.findViewById(R.id.group_run_members_fragment_user_jogging);
            userDistance = view.findViewById(R.id.group_run_members_fragment_user_distance);
            userDuration = view.findViewById(R.id.group_run_members_fragment_user_duration);
            userPace = view.findViewById(R.id.group_run_members_fragment_user_pace);
            userSpeed = view.findViewById(R.id.group_run_members_fragment_user_speed);
        }

    }



    @NonNull
    @Override
    public GroupRunMembersRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.group_run_members_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(groupMembers.get(position).toString());
            Uri uri = Uri.parse(jsonObject.getString("user_avatar"));

            Picasso.get().load(uri)
                    .resize(120, 120)
                    .placeholder(R.drawable.progress_animation)
                    .transform(new PicassoCircle())
                    .into(holder.userAvatar);

            holder.userName.setText(jsonObject.getString("user_name"));

            boolean isJogging = jsonObject.getBoolean("jogging");
            if (isJogging) {
                holder.jogging.setText("Jogging");
                holder.jogging.setTextColor(MainActivity.appContext.getResources().getColor(R.color.lightGreen));
            } else {
                holder.jogging.setText("Away");
                holder.jogging.setTextColor(MainActivity.appContext.getResources().getColor(R.color.extraDarkSmoke));
            }

            int distance = jsonObject.getInt("current_distance");
            int duration = jsonObject.getInt("current_duration");

            holder.userDistance.setText(" " + Conversions.displayKilometres(distance) + "km  ");

            holder.userDuration.setText(" " + Conversions.formatToHHMMSS(duration) + "  ");

            holder.userPace.setText(" " + Conversions.displayPace(distance, duration) + " /km ");

            holder.userSpeed.setText(" " + Conversions.displaySpeed(distance, duration));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int getItemCount() {
        return this.groupMembers.size();
    }


    @Override
    public long getItemId(int position) {
        return groupMembers.get(position).hashCode();
    }


}
