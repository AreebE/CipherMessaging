package com.example.ciphermessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.Handler;

//import com.apollographql.apollo3.ApolloClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
//m;l

    private Runnable backgroundRunner = new Runnable() {
        @Override
        public void run() {
//            APIHandler.makeTextRequest();
//            APIHandler.createText("ji3jfoijqiofj3qwiojfiowjmqfiib GARBAGE");
            new FirebaseReader().createUser("Ceddby", "Deah", 1815);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.fragment_container) == null)
        {
            manager.beginTransaction().add(R.id.fragment_container, LoginFragment.newInstance()).commit();
        }
        Executor execute = Executors.newSingleThreadExecutor();
        execute.execute(backgroundRunner);
//        new Handler().post(backgroundRunner);
    }
}