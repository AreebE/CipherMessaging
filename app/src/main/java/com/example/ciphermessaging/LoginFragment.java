package com.example.ciphermessaging;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoginFragment extends Fragment {


    private static final String TAG = "LoginFragment";

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        Executor executor = Executors.newSingleThreadExecutor();
        EditText username = (EditText) v.findViewById(R.id.username);
        EditText password = (EditText) v.findViewById(R.id.password);
        ((Button) v.findViewById(R.id.submitButton)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                Query q = new FirebaseReader().confirmUser(username.getText().toString(), password.getText().toString());
                                q.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful())
                                                {
                                                    for (QueryDocumentSnapshot doc: task.getResult())
                                                    {
                                                        Log.d(TAG, doc.getId() + " and " + doc.getData());
                                                    }
                                                }
                                                else
                                                {
                                                    Log.d(TAG, "couldn't get documents due to " +  task.getException().toString());
                                                }
                                            }
                                        });
                            }
                        });
                    }
                }
        );

        return v;
    }
}