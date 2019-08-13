package com.jogtown.jogtown.utils.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.GroupActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.fragments.GroupInfoFragment;
import com.jogtown.jogtown.utils.Conversions;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.PicassoCircle;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class GroupInfoMemberListRecyclerViewAdapter extends RecyclerView.Adapter<GroupInfoMemberListRecyclerViewAdapter.MyViewHolder>{

    List<Object> groupMembers;
    SharedPreferences authPref = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);
    int myUserId = authPref.getInt("userId", 0);

    public GroupInfoMemberListRecyclerViewAdapter(List groupMembers) {
        this.groupMembers = groupMembers;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        View layout;
        ImageView groupInfoMemberListAvatar;
        TextView groupInfoMemberListName;
        TextView groupInfoMemberListJoinDate;
        TextView groupInfoMemberListDistance;
        TextView groupInfoMemberListDuration;

        public MyViewHolder(View view) {
            super(view);
            layout = view;
            groupInfoMemberListAvatar = view.findViewById(R.id.groupInfoMemberListAvatar);
            groupInfoMemberListName = view.findViewById(R.id.groupInfoMemberListName);
            groupInfoMemberListJoinDate = view.findViewById(R.id.groupInfoMemberListJoinDate);
            groupInfoMemberListDistance = view.findViewById(R.id.groupInfoMemberListDistance);
            groupInfoMemberListDuration = view.findViewById(R.id.groupInfoMemberListDuration);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.group_info_members_list_item_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        try {
            JSONObject jsonObject = new JSONObject(groupMembers.get(position).toString());
            final int id = jsonObject.getInt("id");
            Uri avatar = Uri.parse(jsonObject.getString("user_avatar"));
            final String name = jsonObject.getString("user_name");
            String joinDate = "joined on " + Conversions.formatDateTime(jsonObject.getString("created_at"));
            final String distance = Conversions.displayKilometres(jsonObject.getInt("total_distance"));
            final String duration = Conversions.formatToHHMMSS(jsonObject.getInt("total_duration"));

            Picasso.get().load(avatar)
                    .resize(100, 100)
                    .transform(new PicassoCircle())
                    .into(holder.groupInfoMemberListAvatar);

            holder.groupInfoMemberListName.setText(name);
            holder.groupInfoMemberListJoinDate.setText(joinDate);
            holder.groupInfoMemberListDistance.setText(distance);
            holder.groupInfoMemberListDuration.setText(duration);

            boolean isMyLayout = this.myUserId == jsonObject.getInt("user_id");
            if (!isMyLayout) {
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertUserProperties(v, name, distance, duration, id, position);
                    }
                });
            }

        } catch (JSONException e) {

        } catch (IllegalArgumentException e) {

        }

    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }



    public void alertUserProperties(final View view, final String name, String distance, String duration, final int membershipId, final int positionOnList) {
        String message = "Total Distance: " + distance + " km. \n" +
        "Total Duration: " + duration;
        AlertDialog.Builder builder = null;
        builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle(name);
        builder.setMessage(message);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (GroupActivity.userIsAMember()) {
            builder.setNeutralButton("Chat", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startAChat(view, positionOnList);
                }
            });
        }

        if (GroupActivity.userIsOwner()) {
            builder.setPositiveButton("Remove From Group", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    promptRemoveMember(view, membershipId, positionOnList, name);
                }
            });
        }

        builder.create().show();

    }



    public void promptRemoveMember(View view, final int membershipId, final int positionOnList, String memberName) {
        String message = "Are you sure you want to permanently remove " +
                memberName + " from this group?";
        AlertDialog.Builder builder = null;
        builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage(message);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeMember(membershipId, positionOnList);
                dialog.dismiss();
            }
        });
        builder.create().show();

    }


    public void startAChat(View view, int position) {
        try {
            final Dialog dialog = new Dialog(view.getContext(), android.R.style.Theme_NoTitleBar);
            dialog.setContentView(R.layout.start_a_chat_dialog_layout);
            ImageView recipientAvatarView = dialog.findViewById(R.id.startChatRecipientAvatar);
            TextView recipientNameText = dialog.findViewById(R.id.startChatRecipientName);
            MessageInput startChatMessageInput = dialog.findViewById(R.id.startChatMessageInput);


            JSONObject jsonObject = new JSONObject(groupMembers.get(position).toString());
            final int recipientId = jsonObject.getInt("user_id");
            Uri recipientAvatar = Uri.parse(jsonObject.getString("user_avatar"));
            String recipientName = jsonObject.getString("user_name");

            startChatMessageInput.setInputListener(new MessageInput.InputListener() {
                @Override
                public boolean onSubmit(CharSequence input) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("message_type", "text");
                        jsonObject.put("message_text", input);
                        jsonObject.put("user_id", recipientId);
                        jsonObject.put("sender_name", authPref.getString("name", ""));
                        jsonObject.put("sender_avatar",
                                authPref.getString("profilePicture",
                                        MainActivity.appContext.getResources().getString(R.string.default_profile_picture)));
                        jsonObject.put("recipient_id", recipientId);
                        jsonObject.put("read", false);

                        String payload = jsonObject.toString();

                        sendMessage(payload);
                        dialog.dismiss();
                    } catch (JSONException e) {

                    }

                    return false;
                }
            });

            Picasso.get().load(recipientAvatar)
                    .resize(120, 120)
                    .transform(new PicassoCircle())
                    .into(recipientAvatarView);
            recipientNameText.setText(recipientName);

            dialog.show();
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

        } catch (JSONException e) {

        } catch (IllegalArgumentException e) {

        }

    }


    public void removeMember(int membershipId, int positionOnList) {
        GroupActivity.removeMemberFromList(positionOnList);
        this.groupMembers.remove(positionOnList);
        // notify dataset changed
        String url = MainActivity.appContext.getResources().getString(R.string.root_url) +
                "v1/group_memberships";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", membershipId );
            String payload = jsonObject.toString();
            MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
                @Override
                public void onFinishRequest(Object result) {
                    try {
                        JSONObject data = new JSONObject(result.toString());
                        int statusCode = data.getInt("statusCode");
                        if (statusCode < 399) {
                            //successfully deleted
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };

            NetworkRequest.delete(url, payload, new MyUrlRequestCallback(onFinishRequest));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }




    public void sendMessage(String payload) {
        String url = MainActivity.appContext.getResources().getString(R.string.root_url) + "v1/messages";

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Ensure message was saved
            }
        };

        NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));
    }



}
