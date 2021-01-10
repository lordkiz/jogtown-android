package com.jogtown.jogtown.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.ConversationActivity;
import com.jogtown.jogtown.activities.MainActivity;
import com.jogtown.jogtown.models.MyDialog;
import com.jogtown.jogtown.models.Message;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.MyTypefaceSpan;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InboxFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    DialogsListAdapter dialogsListAdapter;
    DialogsList inboxList;
    ProgressBar progressBar;
    Button loadMoreButton;

    Boolean loading = false;
    int page = 1;

    LinearLayout inboxFragmentEmptyLayout;

    private OnFragmentInteractionListener mListener;

    AdView mAdView;
    SharedPreferences settingsPref;


    public InboxFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InboxFragment newInstance(String param1, String param2) {
        InboxFragment fragment = new InboxFragment();
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
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        try {
            ActionBar actionBar =  ((AppCompatActivity) getActivity()).getSupportActionBar();

            SpannableString spannableString = new SpannableString("Inbox");
            spannableString.setSpan(
                    new MyTypefaceSpan(getContext(), "fonts/baijamjuree_semi_bold.ttf"),
                    0,
                    spannableString.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            actionBar.setTitle(spannableString);
        } catch (NullPointerException e) {
            //
        }
        settingsPref = MainActivity.appContext.getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE);
        boolean showAds = settingsPref.getBoolean("showAds", true);
        if (showAds) {
            mAdView = view.findViewById(R.id.inboxAdView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        inboxFragmentEmptyLayout = view.findViewById(R.id.inbox_fragment_empty_layout);

        inboxList = view.findViewById(R.id.inboxList);
        progressBar = view.findViewById(R.id.inboxFragmentProgressBar);
        loadMoreButton = view.findViewById(R.id.inboxFragmentLoadMoreButton);
        setDialogListAdapter();
        getUserChats();
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


    private void setDialogListAdapter() {
        dialogsListAdapter = new DialogsListAdapter<>(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                try {
                    Picasso.get().load(url)
                            .placeholder(R.drawable.progress_animation)
                            .into(imageView);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });

        DateFormatter.Formatter dateFormatter = new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                return MyDialog.dateFormatter(date);
            }
        };

        dialogsListAdapter.setDatesFormatter(dateFormatter);

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener() {
            @Override
            public void onDialogClick(IDialog dialog) {
                //clear the unread count for this dialog.
                //Assumption is that user has internet and the read status of the msgs
                //will be updated in the conversations page.
                //So I will create a new dialog with 0 unread count to replace the old dialog

                MyDialog myDialog = new MyDialog();
                myDialog.setUnreadCount(0);
                myDialog.setDialogName(dialog.getDialogName());
                myDialog.setDialogPhoto(dialog.getDialogPhoto());
                myDialog.setId(dialog.getId());
                myDialog.setLastMessage(dialog.getLastMessage());

                String chatId = dialog.getId();
                String chatName = dialog.getDialogName();
                String chatAvatar = dialog.getDialogPhoto();

                Intent intent = new Intent(getContext(), ConversationActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("chatName", chatName);
                intent.putExtra("chatAvatar", chatAvatar);

                //change the dialog to a new dialog with 0 unread msgs
                int position = dialogsListAdapter.getDialogPosition(dialog);
                dialogsListAdapter.updateItem(position, myDialog);

                getContext().startActivity(intent);
            }
        });

        inboxList.setAdapter(dialogsListAdapter);

    }


    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

        }
    }

    private void showEmptyLayout() {
        if (!dialogsListAdapter.isEmpty()) {
            inboxFragmentEmptyLayout.setVisibility(View.GONE);
        } else {
            inboxFragmentEmptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyLayout() {
        inboxFragmentEmptyLayout.setVisibility(View.GONE);
    }


    public void showButton() {
        /*new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!loading && jogs.size() > 0 && jogs.size() % 10 == 0) {
                    loadMoreButton.setVisibility(View.VISIBLE);
                } else {
                    loadMoreButton.setVisibility(View.GONE);
                }
            }
        });*/
    }

    public void getUserChats() {
        loading = true;
        showActivity();
        hideEmptyLayout();

        String url = getString(R.string.root_url) + "v1/user_chats?page=" + page;

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = createNetworkRequestsCallbackActions();
        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);
        NetworkRequest.get(url, requestCallback);
    }




    public MyUrlRequestCallback.OnFinishRequest createNetworkRequestsCallbackActions() {
        return new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode == 200)  { //Some kind of success
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                SharedPreferences sharedPreferences = MainActivity.appContext.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE);

                                String currentUserName = sharedPreferences.getString("name", "");
                                String currentUserAvatar = sharedPreferences.getString("profilePicture", "");
                                int currentUserId = sharedPreferences.getInt("userId", 0);

                                try {

                                    JSONArray jsonArray = new JSONArray(responseBody);
                                    List<IDialog> resList = new ArrayList<>();

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        //Create a Chatkit dialog of each chat
                                        MyDialog dialog = new MyDialog();
                                        IMessage lastMessage = new Message();

                                        String chatObj = jsonArray.get(i).toString();
                                        JSONObject chat = new JSONObject(chatObj);

                                        ((Message) lastMessage).setMessageText(chat.getString("last_message"));
                                        String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
                                        ((Message) lastMessage).setDate(new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(chat.getString("updated_at")));

                                        int chatId = chat.getInt("id");
                                        dialog.setId(Integer.toString(chatId));

                                        if (currentUserName.equals(chat.getString("recipient_name"))) {
                                            dialog.setDialogName(chat.getString("sender_name"));
                                        } else {
                                            dialog.setDialogName(chat.getString("recipient_name"));
                                        }

                                        if (currentUserAvatar.equals(chat.getString("recipient_avatar"))) {
                                            dialog.setDialogPhoto(chat.getString("sender_avatar"));
                                        } else {
                                            dialog.setDialogPhoto(chat.getString("recipient_avatar"));
                                        }

                                        if (currentUserId == chat.getInt("sender_id")) {
                                            dialog.setUnreadCount(chat.getInt("unread_message_count_for_sender"));
                                        } else {
                                            dialog.setUnreadCount(chat.getInt("unread_message_count_for_recipient"));
                                        }


                                        dialog.setLastMessage(lastMessage);


                                        resList.add(dialog);

                                    }

                                    dialogsListAdapter.addItems(resList);
                                    page++;

                                    showButton();
                                    showEmptyLayout();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showEmptyLayout();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    showEmptyLayout();
                                }
                            }
                        });


                    } else if (statusCode > 399){ //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                showButton();
                                showEmptyLayout();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder
                                        .setCancelable(true)
                                        .setMessage(responseBody)
                                        .setTitle("Error!")
                                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialogBuilder.create().show();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loading = false;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showActivity();
                            showEmptyLayout();
                        }
                    });
                }
            }
        };
    }





}
