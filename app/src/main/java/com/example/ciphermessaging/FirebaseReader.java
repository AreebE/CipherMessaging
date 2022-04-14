package com.example.ciphermessaging;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseReader {

    public static final String CONVERSATIONS_DATABASE = "conversations";
    public static final String USERS_KEY = "users";
    public static final String CIPHER_KEY = "cipher";
    public static final String TITLE_KEY = "title";
    public static final String MESSAGES_KEY = "convo";


    private static final String USERNAME_DATABASE = "users";

    public static final String USERNAME_KEY = "username";
    public static final String CONVERSATIONS_KEY = "conversations";
    public static final String PASSWORD_KEY = "password";
    public static final String LAST_NAME_KEY = "last";
    public static final String FIRST_NAME_KEY = "first";

    public void createConvo(String username, String otherUsername, String conversationTitle, FirebaseReaderListener listener)
    {
        Map<String, Object> convoData = new HashMap<>();
        convoData.put(USERS_KEY, new ArrayList<String>(){
            {
                add(username);
                add(otherUsername);
            }
        });
        convoData.put(TITLE_KEY, conversationTitle);
        convoData.put(CIPHER_KEY, "1234567890abcdefghijklmnopqrstuvwxyz");
        convoData.put(MESSAGES_KEY, new ArrayList<DocumentReference>());
        database.collection(CONVERSATIONS_DATABASE)
                .add(convoData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        DocumentReference ref = task.getResult();
                        database.collection(USERNAME_DATABASE)
                                .whereEqualTo(USERNAME_KEY, username)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                DocumentSnapshot user = task.getResult().getDocuments().get(0);
                                Map<String, Object> objects = user.getData();
                                ((List<DocumentReference>) objects.get(CONVERSATIONS_KEY)).add(ref);
                                database.collection(USERNAME_DATABASE).document(user.getId()).update(objects);

                            }
                        });
                        database.collection(USERNAME_DATABASE)
                                .whereEqualTo(USERNAME_KEY, otherUsername)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                DocumentSnapshot user = task.getResult().getDocuments().get(0);
                                Map<String, Object> objects = user.getData();
                                ((List<DocumentReference>) objects.get(CONVERSATIONS_KEY)).add(ref);
                                database.collection(USERNAME_DATABASE).document(user.getId()).update(objects);

                            }
                        });
                    }
                });
    }

    public interface FirebaseReaderListener
    {
        public void notifyOnError(String message);
        public void notifyOnSuccess();
    }
    private static String TAG = "FirebaseReader";
    private FirebaseFirestore database;
    public FirebaseReader()
    {
        database = FirebaseFirestore.getInstance();
        //        database.collection("users");
    }

    public Query confirmUser(String username, String password) {
        return database.collection(USERNAME_DATABASE)
                .whereEqualTo(USERNAME_KEY, username)
                .whereEqualTo(PASSWORD_KEY, password);

    }

    public void createUser(String username, String firstName, String lastName, String password, FirebaseReaderListener listener) {
        Query q = database.collection("users")
                .whereEqualTo("username", username);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().isEmpty()) {
                    Map<String, Object> userData = new HashMap<String, Object>() {{
                        put(USERNAME_KEY, username);
                        put(FIRST_NAME_KEY, firstName);
                        put(LAST_NAME_KEY, lastName);
                        put(PASSWORD_KEY, password);
                        put(CONVERSATIONS_KEY, new ArrayList<>());
                    }};
                    database.collection("users")
                            .add(userData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "succesfully loaded" + ", ");
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            Log.d(TAG, "the results were " + task.getResult().getId() + ", and the contents: " + task.getResult().getData());
                                        }
                                    });
                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "failed due to " + e.toString());
                                }
                            });
                    listener.notifyOnSuccess();
                } else {
                    listener.notifyOnError("The user already exists.");
                }
            }
        });
    }
    public void getConversations(
            String username,
            ArrayList<ContactsFragment.ConversationItem> items,
            FirebaseReaderListener listener)
    {
        System.out.println(username);
        Query q = database.collection(USERNAME_DATABASE)
                .whereEqualTo(USERNAME_KEY, username);

        q.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        int count = 0;
                        List<DocumentReference> convosIDs = (List<DocumentReference>) document.get(CONVERSATIONS_KEY);
                        for (DocumentReference docRef: convosIDs)
                        {
                            Log.d(TAG, docRef.getId());
                            database.collection(CONVERSATIONS_DATABASE)
                                    .document(docRef.getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot convo = task.getResult();
                                            System.out.println(convo.toString());
                                            String id = convo.getId();
                                            String title = convo.getString(TITLE_KEY);
                                            List<String> users = (List<String>) convo.get(USERS_KEY);
                                            int name = (users.get(0).equals(username))? 1: 0;
                                            items.add(new ContactsFragment.ConversationItem(id, users.get(name), title));
                                            listener.notifyOnSuccess();
                                        }
                                    });
                            while(count != items.size())
                            {

                            }
                            listener.notifyOnSuccess();
                        }
                    }
                });

    }

//        database.collection("users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful())
//                        {
//                            for (QueryDocumentSnapshot document: task.getResult())
//                            {
//                                Log.d(TAG, document.getId() + " with " + document.getData());
//                            }
//                        }
//                        else
//                        {
//                            Log.d(TAG, "couldn't get documents due to " + task.getException().toString());
//                        }
//                    }
//                });

}
