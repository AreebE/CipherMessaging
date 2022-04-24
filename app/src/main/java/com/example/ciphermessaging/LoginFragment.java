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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoginFragment extends Fragment {

    public interface LoginInterface
    {
        public void logInUser(String username);
    }

    private static final String TAG = "LoginFragment";

    private LoginInterface loginInterface;

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
        loginInterface = (LoginInterface) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        Executor executor = Executors.newSingleThreadExecutor();
        EditText username = (EditText) v.findViewById(R.id.username);
        EditText password = (EditText) v.findViewById(R.id.password);
        Button button = (Button) v.findViewById(R.id.submitButton);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                                View.OnClickListener buttonListener = this;
                                Log.d(TAG, "confirm a click");
                                button.setOnClickListener(null);
                                new FirebaseReader().confirmUser
                                        (
                                                username.getText().toString(),
                                                password.getText().toString(),
                                                new FirebaseReader.FirebaseReaderListener()
                                                {
                                                    @Override
                                                    public void notifyOnError(String message) {
                                                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                                        button.setOnClickListener(buttonListener);
                                                    }

                                                    @Override
                                                    public void notifyOnSuccess() {
                                                        loginInterface.logInUser(username.getText().toString());
//                                                        button.setOnClickListener(buttonListener);
                                                    }
                                                },
                                                getContext()
                                        );
                    }
                        });
        Button create = (Button) v.findViewById(R.id.createUser);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateUserFragment().show(getChildFragmentManager(), TAG);
            }
        });
        return v;
    }
}