package com.example.ciphermessaging;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateUserFragment extends DialogFragment
        implements FirebaseReader.FirebaseReaderListener{



    public CreateUserFragment() {
        // Required empty public constructor
    }


    public static CreateUserFragment newInstance(String param1, String param2) {
        CreateUserFragment fragment = new CreateUserFragment();
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
        return inflater.inflate(R.layout.fragment_create_user, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        EditText username = (EditText) v.findViewById(R.id.username);
        EditText password = (EditText) v.findViewById(R.id.password);

        Button submitUser = (Button) v.findViewById(R.id.submitButton);
        submitUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();

                if (user.length() == 0
                        || pass.length() == 0
                        )
                {
                    notifyOnError("Please put valid input.");
                    return;
                }
                new FirebaseReader().createUser(user, pass, CreateUserFragment.this, getContext());
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
        return dialog;
    }


    @Override
    public void notifyOnError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyOnSuccess() {
        dismiss();
    }
}