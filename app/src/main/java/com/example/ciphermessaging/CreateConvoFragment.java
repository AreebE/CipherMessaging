package com.example.ciphermessaging;

import android.app.Dialog;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateConvoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateConvoFragment extends DialogFragment
        implements FirebaseReader.FirebaseReaderListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_KEY = "user";

    // TODO: Rename and change types of parameters
    private String username;

    public CreateConvoFragment() {
        // Required empty public constructor
    }


    public static CreateConvoFragment newInstance(String user) {
        CreateConvoFragment fragment = new CreateConvoFragment();
        Bundle args = new Bundle();
        args.putString(USER_KEY, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(USER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_create_convo, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        EditText otherUser = (EditText) v.findViewById(R.id.other_user);
        EditText convoTitle = (EditText) v.findViewById(R.id.title);
        Button submitButton = (Button) v.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otherUsername = otherUser.getEditableText().toString();
                String conversationTitle = convoTitle.getEditableText().toString();
                new FirebaseReader().createConvo(username, otherUsername, conversationTitle, CreateConvoFragment.this);
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
        return dialog;
    }

    @Override
    public void notifyOnError(String message) {

    }

    @Override
    public void notifyOnSuccess() {
        dismiss();
    }
}