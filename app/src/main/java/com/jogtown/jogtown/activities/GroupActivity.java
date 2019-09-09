package com.jogtown.jogtown.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.fragments.GroupChatFragment;
import com.jogtown.jogtown.fragments.GroupInfoFragment;
import com.jogtown.jogtown.fragments.GroupLeaderboardFragment;
import com.jogtown.jogtown.utils.adapters.ViewPagerAdapter;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.ZoomOutPageTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupActivity extends AppCompatActivity implements
        GroupInfoFragment.OnFragmentInteractionListener,
        GroupLeaderboardFragment.OnFragmentInteractionListener,
        GroupChatFragment.OnFragmentInteractionListener
{

    SharedPreferences authPref;
    Intent intent;

    public static JSONObject groupObject;

    public static int groupId;
    public static int userId;
    String groupName;

    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        intent = getIntent();
        try {
            groupObject = new JSONObject(intent.getStringExtra("group"));
            groupId = groupObject.getInt("id");
            groupName = groupObject.getString("name");
        } catch (JSONException e) {
            groupId = 0;
            groupName="Jogtown";
            e.printStackTrace();
        }

        Log.i("groupId", Integer.toString(groupId));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setTitle(groupName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        authPref = getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        userId = authPref.getInt("userId", 0);



        tabLayout = (TabLayout) findViewById(R.id.group_activity_tabs);
        viewPager = (ViewPager) findViewById(R.id.group_activity_view_pager);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        addTabsToTabLayout(viewPager);

    }



    private void addTabsToTabLayout(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragmentAndTitle(new GroupInfoFragment(), "Group Info");
        viewPagerAdapter.addFragmentAndTitle(new GroupLeaderboardFragment(), "Leaderboard");

        if (userIsAMember()) {
            viewPagerAdapter.addFragmentAndTitle(new GroupChatFragment(), "Messages");
        }

        viewPager.setAdapter(viewPagerAdapter);
    }


    public static boolean userIsAMember() {
        try {
            JSONArray groupMemberships = new JSONArray(groupObject.get("group_memberships").toString());
            for (int i = 0; i < groupMemberships.length(); i++) {
                JSONObject gm = new JSONObject(groupMemberships.get(i).toString());
                if (gm.getInt("user_id") == userId) {
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean userIsOwner() {
        try {
            return userId == groupObject.getInt("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void shareInviteCode(View view) {
        Log.i("share Invite code", "true");
    }

    public void showRatingInfo(View view) {
        String aboutRating = "Group rating is automatically determined by activity level " +
                "of the group members. Groups where most members actively do a group jog " +
                "get a higher rating. Ratings are automatically computed weekly by the system";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(aboutRating);
        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    public static void removeMemberFromList(int position) {
        try {
            JSONArray jsonArray = groupObject.getJSONArray("group_memberships");
            for (int i = 0; i < jsonArray.length(); i++) {
                if (i == position) {
                    jsonArray.remove(i);
                    break;
                }
            }
            groupObject.remove("group_memberships");
            groupObject.put("group_memberships", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        groupObject.remove("group_memberships");
    }


    public static void refreshGroup() {
        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject resultsObj = new JSONObject(result.toString());
                    int statusCode = resultsObj.getInt("statusCode");
                    if (statusCode == 200) {
                        groupObject = resultsObj.getJSONObject("body");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        String url = MainActivity.appContext.getResources().getString(R.string.root_url) + "/v1/groups/?id=" + Integer.toString(groupId);
        NetworkRequest.get(url, new MyUrlRequestCallback(onFinishRequest));
    }




    public static JSONObject groupObjectAfterUserLeft(int userId) {
        try {
            JSONArray groupMembers = groupObject.getJSONArray("group_memberships");
            for (int i = 0; i < groupMembers.length(); i++) {
                JSONObject member = groupMembers.getJSONObject(i);
                if (member.getInt("user_id") == userId) {
                    groupMembers.remove(i);
                    break;
                }
            }
            groupObject.remove("group_memberships");
            groupObject.put("group_memberships", groupMembers);
            return groupObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return groupObject;
    }


    public static JSONObject groupObjectAfterUserJoined(JSONObject userMembership) {
        try {
            JSONArray groupMembers = groupObject.getJSONArray("group_memberships");
            groupMembers.put(userMembership);

            groupObject.remove("group_memberships");
            groupObject.put("group_memberships", groupMembers);
            groupObject.put("members_count", groupObject.getInt("members_count") + 1);
            return groupObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return groupObject;
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


}
