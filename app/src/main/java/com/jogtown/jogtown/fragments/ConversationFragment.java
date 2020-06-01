package com.jogtown.jogtown.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.hosopy.actioncable.Subscription;
import com.jogtown.jogtown.R;
import com.jogtown.jogtown.activities.ConversationActivity;
import com.jogtown.jogtown.models.Author;
import com.jogtown.jogtown.models.Message;
import com.jogtown.jogtown.utils.network.ActionCableSocket;
import com.jogtown.jogtown.utils.network.MyUrlRequestCallback;
import com.jogtown.jogtown.utils.network.NetworkRequest;
import com.jogtown.jogtown.utils.ui.PicassoCircle;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConversationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConversationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    boolean loading = false;

    Subscription subscription;

    MessagesList messagesList;
    MessageInput messageInputBar;
    ProgressBar progressBar;
    MessagesListAdapter<Message> messageListAdapter;

    String senderId;
    String chatId;
    String chatName;
    String chatAvatar;
    String senderAvatar;
    String senderName;
    int recipientId = 0;
    //ActionBar actionBar;


    public ConversationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param chatId Parameter 1.
     * @param chatName Parameter 2.
     * @return A new instance of fragment ConversationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConversationFragment newInstance(String chatId, String chatName, String chatAvatar) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putString(chatId, chatId);
        args.putString(chatName, chatName);
        args.putString(chatAvatar, chatAvatar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatId = getArguments().getString(chatId);
            chatName = getArguments().getString(chatName);
            chatAvatar = getArguments().getString(chatAvatar);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        Context context = view.getContext();
        context.setTheme(R.style.AppTheme);
        SharedPreferences authPref = context.getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        messagesList = view.findViewById(R.id.messagesList);
        messageInputBar = view.findViewById(R.id.messageInputBar);
        progressBar = view.findViewById(R.id.conversationActivityProgressBar);

        senderId = Integer.toString(authPref.getInt("userId", 0));
        senderAvatar = authPref.getString("profilePicture", "");
        senderName = authPref.getString("name", "");

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("  " + chatName);
            actionBar.setSubtitle("  online");
            actionBar.setDisplayHomeAsUpEnabled(true);
            try {
                Picasso.get().load(chatAvatar)
                        .placeholder(R.drawable.progress_animation)
                        .resize(100,100)
                        .transform(new PicassoCircle())
                        .into(new Target()
                        {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Drawable d = new BitmapDrawable(getResources(), bitmap);
                                actionBar.setIcon(d);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        setupAdapter();

        setupInput();

        getChatMessages();

        createSocket();


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


    public void setupAdapter() {
        messageListAdapter = new MessagesListAdapter<>(senderId, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                try {
                    Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.progress_animation)
                            .into(imageView);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });

        messagesList.setAdapter(messageListAdapter);

    }

    public void setupInput() {

        messageInputBar.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", Math.round(Math.random() * 10000000));
                    jsonObject.put("message_type", "text");
                    jsonObject.put("message_text", input);
                    jsonObject.put("chat_id", chatId);
                    jsonObject.put("user_id", senderId);
                    jsonObject.put("sender_name", senderName);
                    jsonObject.put("sender_avatar", senderAvatar);
                    jsonObject.put("recipient_id", recipientId);
                    jsonObject.put("read", false);
                    jsonObject.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

                    String payload = jsonObject.toString();
                    sendMessage(payload);

                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(messageInputBar.getWindowToken(), 0);

                    Message message = buildMessage(jsonObject);
                    messageListAdapter.addToStart(message, true);

                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }


    public void getChatMessages() {
        loading = true;
        showActivity();


        String url = getString(R.string.root_url) + "v1/chats/" + chatId;

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = createOnFinishRequestCallbackActions();
        MyUrlRequestCallback requestCallback = new MyUrlRequestCallback(onFinishRequest);

        NetworkRequest.get(url, requestCallback);
    }



    public MyUrlRequestCallback.OnFinishRequest createOnFinishRequestCallbackActions() {
        SharedPreferences authPref = getActivity().getSharedPreferences("AuthPreferences", MODE_PRIVATE);
        final String userId = Integer.toString(authPref.getInt("userId", 0));
        //need this userId to mark messages sent by another user as read.

        return new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                try {
                    JSONObject data = new JSONObject(result.toString());
                    final String responseBody = data.getString("body");
                    String headers = data.getString("headers");
                    int statusCode = data.getInt("statusCode");
                    if (statusCode == 200) { //Some kind of success
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
                                try {

                                    List<Integer> unreadMessageIds = new ArrayList<>();

                                    JSONObject jsonObject = new JSONObject(responseBody);
                                    JSONArray messagesArr = new JSONArray(jsonObject.getString("messages"));

                                    if (Integer.parseInt(userId) != jsonObject.getInt("recipient_id")) {
                                        recipientId = jsonObject.getInt("recipient_id");
                                    } else {
                                        recipientId = jsonObject.getInt("sender_id");
                                    }

                                    for (int i = 0; i < messagesArr.length(); i++) {
                                        //Create a Chatkit Message of each message
                                        String msgStr = messagesArr.get(i).toString();
                                        JSONObject msgObj = new JSONObject(msgStr);

                                        Message message = buildMessage(msgObj);

                                        if (!message.isRead() && !message.getAuthor().getId().equals(userId)) {
                                            //messages that are not from me and haven't been read
                                            unreadMessageIds.add(Integer.parseInt(message.getId()));
                                        }

                                        messageListAdapter.addToStart(message, true);

                                    }

                                    if (unreadMessageIds.size() > 0) {
                                        markUnreadMessagesAsRead(unreadMessageIds);
                                    }

                                } catch (JSONException e) {
                                    loading = false;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showActivity();
                                        }
                                    });

                                }
                            }
                        });

                    } else if (statusCode > 399) { //400 and above errors
                        loading = false;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showActivity();
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
                        }
                    });
                }
            }
        };
    }





    public void showActivity() {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

        }
    }



    public void markUnreadMessagesAsRead(List<Integer> unreadMessageIds) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("message_ids", unreadMessageIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String payloadString = payload.toString();
        String url = getString(R.string.root_url) + "v1/mark_messages_as_read";

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Show read receipts for each of these messages
            }
        };

        NetworkRequest.post(url, payloadString, new MyUrlRequestCallback(onFinishRequest));
    }



    public void createSocket() {
        ActionCableSocket.OnSocketConnection onSocketConnection = new ActionCableSocket.OnSocketConnection() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onRejected() {

            }

            @Override
            public void onReceived(JSONObject jsonObject) {
                try {
                    String data = jsonObject.getString("body");
                    JSONObject dataObj = new JSONObject(data);
                    if (dataObj.has("message")) {
                        JSONObject msgObj = dataObj.getJSONObject("message");
                        if (msgObj.has("message_type")) {
                            final Message message = buildMessage(msgObj);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (Integer.parseInt(message.getAuthor().getId()) != Integer.parseInt(senderId)) {
                                        messageListAdapter.addToStart(message, true);
                                    }
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onFailed() {

            }
        };
        ActionCableSocket actionCableSocket = new ActionCableSocket(
                "ChatChannel",
                chatId,
                onSocketConnection);

        subscription = actionCableSocket.subscription;

    }


    public Message buildMessage(JSONObject jsonObject) {
        Message message = new Message();
        Author author = new Author();

        try {
            JSONObject msgObj = jsonObject;
            String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

            String authorId = Integer.toString(msgObj.getInt("user_id"));
            String authorName = msgObj.getString("sender_name");
            String authorAvatar = msgObj.getString("sender_avatar");
            author.setAvatar(authorAvatar);
            author.setId(authorId);
            author.setName(authorName);

            String messageText = msgObj.getString("message_text");
            String messageType = msgObj.getString("message_type");
            int messageId = msgObj.getInt("id");
            boolean isRead = msgObj.getBoolean("read");


            message.setId(messageId);
            message.setDate(new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(msgObj.getString("updated_at")));
            message.setMessageText(messageText);
            message.setMessageType(messageType);
            message.setRead(isRead);
            message.setAuthor(author);


            if (messageType.equals("voice")) {
                message.setVoiceUrl(msgObj.getString("voice"));
            }

            if (messageType.equals("image")) {
                message.setVoiceUrl(msgObj.getString("image"));
            }

            return message;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return message;

    }


    public void sendMessage(String payload) {
        String url = getString(R.string.root_url) + "v1/messages";

        MyUrlRequestCallback.OnFinishRequest onFinishRequest = new MyUrlRequestCallback.OnFinishRequest() {
            @Override
            public void onFinishRequest(Object result) {
                //TODO: Ensure message was saved
            }
        };

        NetworkRequest.post(url, payload, new MyUrlRequestCallback(onFinishRequest));
    }



    @Override
    public boolean onSupportNavigateUp(){
        getActivity().finish();
        return true;
    }

}
