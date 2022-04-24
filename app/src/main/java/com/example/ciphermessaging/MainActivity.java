package com.example.ciphermessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

//import com.apollographql.apollo3.ApolloClient;

import com.google.android.material.tabs.TabLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements FirebaseReader.FirebaseReaderListener,
        LoginFragment.LoginInterface,
        CreateConvoFragment.CreateConvoListener{
    private static final String TAG = "MainActivity";
//m;l

    private Runnable backgroundRunner = new Runnable() {
        @Override
        public void run() {
//            APIHandler.makeTextRequest();
//            APIHandler.createText("ji3jfoijqiofj3qwiojfiowjmqfiib GARBAGE");
//            new FirebaseReader().createUser("CD", "Ceddby", "Deah", "3492msog", MainActivity.this);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.fragment_container) == null)
        {
            manager.beginTransaction().replace(R.id.fragment_container, LoginFragment.newInstance()).commit();
        }
        Executor execute = Executors.newSingleThreadExecutor();
        execute.execute(backgroundRunner);
//        new Handler().post(backgroundRunner);
    }

    @Override
    public void notifyOnError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyOnSuccess() {

    }

    @Override
    public void logInUser(String username) {
//        Log.d(TAG, "trying to log in user");
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container, ContactsFragment.newInstance(username)).commit();

    }

    @Override
    public void onSuccessfulCreation() {
        FragmentManager manager = getSupportFragmentManager();
        ((ContactsFragment) manager.findFragmentById(R.id.fragment_container)).loadConversations();
    }
}