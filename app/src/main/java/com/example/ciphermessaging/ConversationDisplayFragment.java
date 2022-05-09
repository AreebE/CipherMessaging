package com.example.ciphermessaging;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.os.Handler;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

// fix bug where user does not see first few messages made
public class ConversationDisplayFragment extends ListFragment
{

    private static final Handler handler = new Handler();
    private static final String TAG = "ConversationDisplayFrag";
    private int numLoaded = 0;
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

    private Runnable conversationLoader = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "ccalled conversation loader");
            loadConversation(numLoaded);
        }
    };

    private static final String USERNAME_KEY = "username";
    private static final String CONVERSATION_KEY = "conversation";
    private static final String MESSAGE_LIST_KEY = "message list";
    private String thisUser;
    private String convoID;
    private String messageID;
    public ConversationDisplayFragment() {
        // Required empty public constructor
    }


    public static ConversationDisplayFragment newInstance(String username, String convoID, String messageID) {
        ConversationDisplayFragment fragment = new ConversationDisplayFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME_KEY, username);
        args.putString(CONVERSATION_KEY, convoID);
        args.putString(MESSAGE_LIST_KEY, messageID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            thisUser = getArguments().getString(USERNAME_KEY);
            convoID = getArguments().getString(CONVERSATION_KEY);
            messageID = getArguments().getString(MESSAGE_LIST_KEY);
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
        numLoaded = 0;
        MessageAdapter adapter = new MessageAdapter(getActivity(), new ArrayList<MessageItem>());
        new FirebaseReader().updateDate(thisUser, convoID, Calendar.getInstance().getTime());
        setListAdapter(adapter);
        Log.d(TAG, "on resume");
        handler.post(conversationLoader);
    }




    public class MessageAdapter extends ArrayAdapter<MessageItem>
    {
        private List<MessageItem> items;
        public MessageAdapter(@NonNull Context context, @NonNull List<MessageItem> objects) {
            super(context, R.layout.message_item, objects);
            this.items = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MessageItem currentItem = getItem(position);

            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.message_item, null);
            }


//            Log.d(TAG, "creating a view");
            LinearLayout layout = (LinearLayout) convertView;
            TextView textContentView = (TextView) convertView.findViewById(R.id.contentDisplay);
            textContentView.setText(currentItem.getContent());
            Log.d(TAG, "position = " + position + "; current item: " + currentItem.getContent() + ", " + currentItem.getSender() + ". "+  thisUser );
//            TextView timeStamp = new TextView(getActivity());
//            timeStamp.setText(currentItem.getTimestamp().toString());
//            LinearLayout.LayoutParams timeStampParameters = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            timeStamp.setLayoutParams(timeStampParameters);
//            timeStamp.setMaxWidth((int) getResources().getDimension(R.dimen.dateSpace));
//            timeStamp.setGravity(Gravity.CENTER);

             if (currentItem.getSender().equals(thisUser))
            {
                Log.d(TAG, "Assigning the blue to " + currentItem.getContent());
//                layout.addView(timeStamp, 0);
//                layout.addView(spacer, 0);
                layout.setGravity(Gravity.RIGHT);

                textContentView.setBackground(getResources().getDrawable(R.drawable.text_user_background));

            }
             else
             {
                 layout.setGravity(Gravity.LEFT);

                 textContentView.setBackground(getResources().getDrawable(R.drawable.text_background));
             }
//            Log.d(TAG, "Called a get view." + currentItem.getContent() +"," + position);

            return convertView;
        }

        public List<MessageItem> getItems() {
            return items;
        }
    }



    private void loadConversation(int numToLoad)
    {
        ArrayList<MessageItem> items = new ArrayList<>();
        MessageAdapter adapter = (MessageAdapter) getListAdapter();
//        setListAdapter(adapter);
//        Log.d(TAG, convoID + "loading convo");

        new FirebaseReader().getMessages(convoID, messageID, numToLoad, items, new FirebaseReader.FirebaseReaderListener() {
            @Override
            public void notifyOnError(String message) {

            }

            @Override
            public void notifyOnSuccess() {
//                Log.d(TAG, "notified the success");
                if (items.size() != adapter.getCount())
                {
                    new FirebaseReader().updateDate(thisUser, convoID, Calendar.getInstance().getTime());
//                    String itemsList = "";
//                    for (int j = 0; j < items.size(); j++)
//                    {
//                        itemsList += "\" " + items.get(j).getContent() + " \", ";
//                    }
//                    Log.d(TAG, "All items:" + itemsList);

                    for (int i = 0; i < items.size() - numLoaded; i++)
                    {
//                        Log.d(TAG, "Adding item: " + items.get(i).getContent() + "," + items.get(i).getSender());
                        adapter.add(items.get(i));

                        adapter.sort(new Comparator<MessageItem>() {
                            @Override
                            public int compare(MessageItem messageItem, MessageItem t1) {
                                return messageItem.compareTo(t1);
                            }
                        });
                    }
                    numLoaded = adapter.getCount();
                    synchronized (adapter)
                    {
                        adapter.notifyDataSetChanged();
                        getListView().setSelection(adapter.getCount() - 1);
                    }

                }

//                Log.d(TAG, "items found = " + items.size());

                handler.postDelayed(conversationLoader, 2000);

            }
        },
                getContext());
    }



    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        new FirebaseReader().updateDate(thisUser, convoID, Calendar.getInstance().getTime());
    }

    public int getNumLoaded()
    {
        return numLoaded;
    }
}