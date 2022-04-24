package com.example.ciphermessaging;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateMessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateMessageFragment extends Fragment {
    private static Handler handler = new Handler();
    private static final String TAG = "CreateMessageFrag";
    private boolean inSendingTextState = false;
    public interface MessageCreatedListener
    {
        public void onMessageCreated();
    }


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SENDER_KEY = "sender";
    private static final String CONVO_ID_KEY = "convo id";

    // TODO: Rename and change types of parameters
    private String sender;
    private String convoID;

    public CreateMessageFragment() {
        // Required empty public constructor
    }


    public static CreateMessageFragment newInstance(String sender, String convoID) {
        CreateMessageFragment fragment = new CreateMessageFragment();
        Bundle args = new Bundle();
        args.putString(SENDER_KEY, sender);
        args.putString(CONVO_ID_KEY, convoID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sender = getArguments().getString(SENDER_KEY);
            convoID = getArguments().getString(CONVO_ID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_create_message, container, false);
        EditText contentView = (EditText) v.findViewById(R.id.messageContent);
        ImageButton submitMessage = (ImageButton) v.findViewById(R.id.messageSendButton);
        submitMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View.OnClickListener clickListener = this;
                submitMessage.setOnClickListener(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submitMessage.setOnClickListener(clickListener);
                    }
                }, 1000);
                Log.d(TAG, "clicked message");
                submitMessage.setClickable(false);

                String content = contentView.getText().toString();
                if (content.length() == 0)
                {
                    return;
                }
                Date timestamp = Calendar.getInstance().getTime();
                new FirebaseReader().createMessage
                        (
                                sender,
                                content,
                                timestamp,
                                convoID,
                                new FirebaseReader.FirebaseReaderListener() {
                                    @Override
                                    public void notifyOnError(String message) {
//                                        Log.d(TAG, message);
                                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void notifyOnSuccess() {
                                        ((MessageCreatedListener) getActivity()).onMessageCreated();
                                    }
                                },
                                getContext()
                        );


            }
        });
        return v;
    }
}