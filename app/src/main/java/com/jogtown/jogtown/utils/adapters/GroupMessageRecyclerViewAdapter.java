package com.jogtown.jogtown.utils.adapters;

import android.graphics.Typeface;
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

public class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<GroupMessageRecyclerViewAdapter.MyViewHolder>{
    public List<Object> groupMessages;
    boolean lightBackground;

    public GroupMessageRecyclerViewAdapter(List groupMessages, boolean lightBackground) {
        this.groupMessages = groupMessages;
        this.lightBackground = lightBackground;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        View layout;
        ImageView userAvatar;
        TextView userName;
        TextView messageText;
        TextView messageDate;

        public MyViewHolder(View view) {
            super(view);
            layout = view;
            userAvatar = view.findViewById(R.id.group_message_user_avatar);
            userName = view.findViewById(R.id.group_message_user_name);
            messageText = view.findViewById(R.id.group_message_text);
            messageDate = view.findViewById(R.id.group_message_date);

        }
    }




    @NonNull
    @Override
    public GroupMessageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.group_message_layout, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JSONObject msg;
        try {
            msg = new JSONObject(groupMessages.get(position).toString());

            if (msg.getBoolean("system")) {

                //if it is a system message. i.e not from any user but from the system
                String messageText = msg.getString("message_text");
                holder.messageText.setText(messageText);
                holder.messageText.setTextColor(MainActivity.appContext.getResources().getColor(R.color.extraDarkSmoke));
                holder.messageText.setTypeface(holder.messageText.getTypeface(), Typeface.ITALIC);
                holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            } else {

                Uri uri = Uri.parse(msg.getString("sender_avatar"));

                Picasso.get().
                        load(uri).
                        resize(60, 60)
                        .transform(new PicassoCircle())
                        .into(holder.userAvatar);

                String userName = msg.getString("sender_name");
                holder.userName.setText(userName);
                holder.userName.setTextColor(MainActivity.appContext.getResources().getColor(R.color.lightPurple));

                String messageText = msg.getString("message_text");
                holder.messageText.setText(messageText);
                String date = Conversions.formatDateTime(msg.getString("created_at"));
                holder.messageDate.setText(date);
                if (this.lightBackground) {
                    holder.messageText.setTextColor(MainActivity.appContext.getResources().getColor(R.color.silver));
                    holder.messageDate.setTextColor(MainActivity.appContext.getResources().getColor(R.color.silver));
                } else {
                    holder.messageText.setTextColor(MainActivity.appContext.getResources().getColor(R.color.darkSmoke));
                    holder.messageDate.setTextColor(MainActivity.appContext.getResources().getColor(R.color.darkSmoke));
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }

    @Override
    public long getItemId(int position) {
        return groupMessages.get(position).hashCode();
    }


}
