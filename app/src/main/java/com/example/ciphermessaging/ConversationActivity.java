package com.example.ciphermessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

public class ConversationActivity extends AppCompatActivity
        implements CreateMessageFragment.MessageCreatedListener{

    public static final String USER_KEY = "user";
    public static final String CONVO_ID = "convo id";
    public static final String MESSAGE_ID = "message id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Intent parent = getIntent();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragments = manager.beginTransaction();

        fragments.
                replace
                (
                        R.id.convo_view,
                        ConversationDisplayFragment.newInstance
                                (
                                        parent.getStringExtra(USER_KEY),
                                        parent.getStringExtra(CONVO_ID),
                                        parent.getStringExtra(MESSAGE_ID)
                                )
                );
        fragments.replace
                (
                        R.id.message_sender,
                        CreateMessageFragment.newInstance
                                (
                                        parent.getStringExtra(USER_KEY),
                                        parent.getStringExtra(CONVO_ID),
                                        parent.getStringExtra(MESSAGE_ID)
                                )
                );
        fragments.commit();

    }

    @Override
    public void onMessageCreated() {

    }


}