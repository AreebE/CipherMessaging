package com.example.ciphermessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;

public class ConversationActivity extends AppCompatActivity {

    public static final String USER_KEY = "user";
    public static final String CONVO_ID = "convo id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Intent parent = getIntent();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().
                replace
                (
                        R.id.convo_view,
                        ConversationDisplayFragment.newInstance
                                (
                                        parent.getStringExtra(USER_KEY),
                                        parent.getStringExtra(CONVO_ID)
                                )
                )
                .commit();
    }
}