package com.example.ciphermessaging;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Every 1 sec. do a poll to check for new messages
 * Find a way to automatically slow down
 * * OPTIONAL -- Seperate dates by timestamps *
 */

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConversationDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversationDisplayFragment extends ListFragment
{

    private static final String TAG = "ConversationDisplayFrag";

    public static class MessageItem
        implements Comparable<MessageItem>
    {
        private String sender;
        private Date timestamp;
        private String content;

        public MessageItem(String sender, Date timestamp, String content)
        {
            this.sender = sender;
            this.timestamp = timestamp;
            this.content = content;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getSender()
        {
            return sender;
        }

        public String getContent() {
            return content;
        }

        @Override
        public int compareTo(MessageItem messageItem) {
            return 1 * this.timestamp.compareTo(messageItem.getTimestamp());
        }
    }

    private static final String USERNAME_KEY = "username";
    private static final String CONVERSATION_KEY = "conversation";
    private String thisUser;
    private String convoID;
    public ConversationDisplayFragment() {
        // Required empty public constructor
    }


    public static ConversationDisplayFragment newInstance(String username, String convoID) {
        ConversationDisplayFragment fragment = new ConversationDisplayFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME_KEY, username);
        args.putString(CONVERSATION_KEY, convoID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            thisUser = getArguments().getString(USERNAME_KEY);
            convoID = getArguments().getString(CONVERSATION_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_conversation, container, false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "on resume");
        loadConversation();
    }



    public class MessageAdapter extends ArrayAdapter<MessageItem>
    {
        public MessageAdapter(@NonNull Context context, @NonNull List<MessageItem> objects) {
            super(context, R.layout.message_item, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MessageItem currentItem = getItem(position);
            if (position == getCount() - 1)
            {
                getListView().setSelection(position);
            }
            if (convertView != null)
            {
                TextView textContentView = (TextView) convertView.findViewById(R.id.contentDisplay);
                textContentView.setText(currentItem.getContent());
                return convertView;
            }

            convertView = getLayoutInflater().inflate(R.layout.message_item, null);

//            Log.d(TAG, "creating a view");
            LinearLayout layout = (LinearLayout) convertView;
            TextView textContentView = (TextView) convertView.findViewById(R.id.contentDisplay);
            textContentView.setText(currentItem.getContent());

            TextView timeStamp = new TextView(getActivity());
            timeStamp.setText(currentItem.getTimestamp().toString());
            LinearLayout.LayoutParams timeStampParameters = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            timeStamp.setLayoutParams(timeStampParameters);
            timeStamp.setMaxWidth((int) getResources().getDimension(R.dimen.dateSpace));
            timeStamp.setGravity(Gravity.CENTER);

             if (!currentItem.getSender().equals(thisUser))
            {
                layout.addView(timeStamp, 1);
//                layout.addView(spacer, 2);
            }
            else
            {
                layout.addView(timeStamp, 0);
//                layout.addView(spacer, 0);
                layout.setGravity(Gravity.RIGHT);

                textContentView.setBackground(getResources().getDrawable(R.drawable.text_user_background));

            }
//            Log.d(TAG, "Called a get view." + currentItem.getContent() +"," + position);

            return convertView;
        }
    }

    public void loadConversation()
    {
        ArrayList<MessageItem> items = new ArrayList<>();
//        MessageAdapter adapter = new MessageAdapter(getActivity(), items);
//        setListAdapter(adapter);
        Log.d(TAG, convoID + "loading convo");
        new FirebaseReader().getMessages(convoID, items, new FirebaseReader.FirebaseReaderListener() {
            @Override
            public void notifyOnError(String message) {

            }

            @Override
            public void notifyOnSuccess() {
                MessageAdapter adapter = new MessageAdapter(getActivity(), items);
                Collections.sort(items);
                for (int i = 0; i < items.size(); i++)
                {
                    Log.d(TAG, items.get(i).content);
                }
                setListAdapter(adapter);
            }
        },
                getContext());
    }
}