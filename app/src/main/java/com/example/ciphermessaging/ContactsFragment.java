package com.example.ciphermessaging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.ListAdapter;

import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Create badge to show when there are new messages availabl
 */

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends ListFragment
        implements CreateConvoFragment.CreateConvoListener{

    private Handler handler = new Handler();
    private static final String TAG = "ContactsFragment";

    @Override
    public void onSuccessfulCreation() {
        loadConversations();
    }
//ljlkkpp

    public static class ConversationItem
    {
        private String idOfConvo;
        private String nameOfOther;
        private String convoName;
        private Date mostRecent;
        public ConversationItem(String id, String name, String convoName, Date date)
        {
            this.idOfConvo = id;
            this.nameOfOther = name;
            this.convoName = convoName;
            this.mostRecent = date;
        }

        public String getID()
        {
            return idOfConvo;
        }

        public String getName()
        {
            return nameOfOther;
        }

        public String getConvoName()
        {
            return convoName;
        }

        public Date getDate() {return mostRecent;}
    }
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USERNAME = "user";

    // TODO: Rename and change types of parameters
    private String username;
//    private String mParam2;

    public ContactsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String username) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            username = getArguments().getString(USERNAME);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        loadConversations();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
//        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        if (!new FirebaseReader().isInternetEnabled(getContext()))
        {
            Toast.makeText(getActivity(), "The internet is not working.", Toast.LENGTH_SHORT).show();
            return;
        }
        handler.removeCallbacksAndMessages(null);
        String convoID = ((ConversationItem) getListAdapter().getItem(position)).getID();
        Intent i = new Intent(getActivity(), ConversationActivity.class);
        Log.d(TAG, "item cliekc");
        i.putExtra(ConversationActivity.CONVO_ID, convoID);
        i.putExtra(ConversationActivity.USER_KEY, username);
        startActivity(i);
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "loading convos");
        loadConversations();
    }

    /* The menus */



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.convo_item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.add_convo:
                DialogFragment createConvo = CreateConvoFragment.newInstance(username);
//                createConvo.setTargetFragment(this, 000);
                createConvo.show(getChildFragmentManager(), TAG);

        }
        return super.onOptionsItemSelected(item);

    }
//
//    @Override
//    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
////        super.onCreateContextMenu(menu, v, menuInfo);
////        getActivity().getMenuInflater().inflate(R.menu.convo_context_menu, menu);
//    }

    /* The adapter */
    private class ContactAdapter extends ArrayAdapter<ConversationItem>
    {

        public ContactAdapter(@NonNull Context context, @NonNull List<ConversationItem> objects) {
            super(context, R.layout.conversation_item, objects);
            Log.d(TAG, "new id created");
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.conversation_item, null);
            ImageView badge = (ImageView) convertView.findViewById(R.id.badge_display);
            badge.setVisibility(View.INVISIBLE);
            TextView name = (TextView) convertView.findViewById(R.id.other_user);
            TextView convo = (TextView) convertView.findViewById(R.id.convo_name);
            ConversationItem conversationItem = this.getItem(position);
            name.setText(conversationItem.getName());
            Runnable badgeUpdater = null;
//            Log.d(TAG, "creating");
            badgeUpdater = new Runnable() {
                @Override
                public void run() {
                    new FirebaseReader().compareToFirstMessage
                            (
                                    conversationItem.getID(),
                                    conversationItem.getDate(),
                                    new FirebaseReader.FirebaseReaderListener() {
                                        @Override
                                        public void notifyOnError(String message) {
                                            postDelay();
                                        }

                                        @Override
                                        public void notifyOnSuccess() {
                                            badge.setVisibility(View.VISIBLE);
                                        }
                                    }
                            );
                }

                private void postDelay()
                {
                    Log.d(TAG, "testing the delay");
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(badgeUpdater);

            System.out.println(conversationItem.getName());
            convo.setText(conversationItem.getConvoName());
            return convertView;
        }


    }

    public void loadConversations()
    {
        ArrayList<ConversationItem> items = new ArrayList<>();
        new FirebaseReader().getConversations(username, items, new FirebaseReader.FirebaseReaderListener() {
            @Override
            public void notifyOnError(String message) {

            }

            @Override
            public void notifyOnSuccess() {
                handler.removeCallbacksAndMessages(null);
                Log.d(TAG, "finished loading");
                ContactsFragment.this.setListAdapter(new ContactAdapter(getActivity(), items));

//                getListAdapter().notify();
            }
        },
                getContext());
    }
}