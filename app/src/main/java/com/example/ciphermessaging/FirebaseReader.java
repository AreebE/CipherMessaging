package com.example.ciphermessaging;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.LongDef;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Add a fail when the internet is off
 */
public class FirebaseReader {

    public static final String MESSAGES_DATABASE = "Message";
    public static final String CONTENT_KEY = "content";
    public static final String SENDER_KEY = "sender";
    public static final String TIME_KEY = "time";

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

    public void createConvo(
            String username,
            String otherUsername,
            String conversationTitle,
            FirebaseReaderListener listener,
            Context context)
    {
        if (!isInternetEnabled(context))
        {
            listener.notifyOnError("No connection to the internet.");
            return;
        }
        database.collection(USERNAME_DATABASE)
                .whereEqualTo(USERNAME_KEY, otherUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() == 0)
                        {
                            listener.notifyOnError("No other user found.");
                            return;
                        }
                        loadConvo(username, otherUsername, conversationTitle, listener);
                    }
                });

    }

    private void loadConvo(
            String username,
            String otherUsername,
            String conversationTitle,
            FirebaseReaderListener listener)
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
                                database.collection(USERNAME_DATABASE).document(user.getId()).update(objects)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.notifyOnSuccess();
                                    }
                                });
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


    public void getMessages(
            String convoID,
            int startIndex,
            ArrayList<ConversationDisplayFragment.MessageItem> items,
            FirebaseReaderListener messagingFragment,
            Context context
            )
    {

        if (!isInternetEnabled(context))
        {
            messagingFragment.notifyOnError("No connection to the internet.");
            return;
        }
        database.collection(CONVERSATIONS_DATABASE)
                .document(convoID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot conversation = task.getResult();
                        List<DocumentReference> messageLinks = (List<DocumentReference>) conversation.get(MESSAGES_KEY);
                        for (int i = items.size(); i < messageLinks.size(); i++)
                        {
                           items.add(i, null);
                        }
                        final int[] loadedItems = {0};
                        for (int i = 0; i < messageLinks.size(); i++)
                        {
//                            Log.d(TAG, messageLinks.size() + " eee");
                            int finalI = i;
                            database.collection(MESSAGES_DATABASE)
                                    .document(messageLinks.get(i).getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot message = task.getResult();
                                            String content = message.getString(CONTENT_KEY);
                                            String sender = message.getString(SENDER_KEY);
                                            Date timeStamp = message.getDate(TIME_KEY);
                                            items.set(finalI, new ConversationDisplayFragment.MessageItem(sender, timeStamp, content));
                                            loadedItems[0]++;
                                            Log.d(TAG, startIndex + ", " + items.size() + ", " + messageLinks.size());
                                            if (loadedItems[0] == messageLinks.size())
                                            {
                                                Log.d(TAG, "found all items");
                                                messagingFragment.notifyOnSuccess();
                                            }
                                        }
                                    });
                        }
//                        if (items.size() == messageLinks.size())
//                        {
//                            messagingFragment.notifyOnSuccess();
//                        }
                    }
                });
    }

    public void createMessage(
            String sender,
            String content,
            Date timestamp,
            String convoID,
            FirebaseReaderListener listener,
            Context context)
    {
        if (!isInternetEnabled(context))
        {
            listener.notifyOnError("No connection to the internet.");
            return;
        }
        Map<String, Object> messageData = new HashMap<>();
        messageData.put(SENDER_KEY, sender);
        messageData.put(CONTENT_KEY, content);
        messageData.put(TIME_KEY, timestamp);
        database.collection(MESSAGES_DATABASE)
                .add(messageData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        DocumentReference messageReference = task.getResult();
                        database.collection(CONVERSATIONS_DATABASE)
                                .document(convoID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Map<String, Object> convoData = task.getResult().getData();
                                        ((List<DocumentReference>) convoData.get(MESSAGES_KEY)).add(0, messageReference);
                                        database.collection(CONVERSATIONS_DATABASE).document(convoID).update(convoData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                listener.notifyOnSuccess();
                                            }
                                        });
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

    public void confirmUser(
            String username,
            String password,
            FirebaseReaderListener listener,
            Context context) {
        if (!isInternetEnabled(context))
        {
            listener.notifyOnError("No connection to the internet.");
            return;
        }
        database.collection(USERNAME_DATABASE)
                .whereEqualTo(USERNAME_KEY, username)
                .whereEqualTo(PASSWORD_KEY, password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "was successful");
                            QuerySnapshot documents = task.getResult();
                            for (QueryDocumentSnapshot doc: documents)
                            {
                                listener.notifyOnSuccess();
                            }
                            if (documents.isEmpty())
                            {
                                listener.notifyOnError("Could not find the user.");
                            }
                        }
                        else
                        {
                            listener.notifyOnError("Could not access database.");
//                            Log.d(TAG, "couldn't get documents due to " + task.getException().toString());
                        }
                    }
                });

    }

    public void createUser(
            String username,
            String firstName,
            String lastName,
            String password,
            FirebaseReaderListener listener,
            Context context)
    {
        if (!isInternetEnabled(context))
        {
            listener.notifyOnError("No connection to the internet.");
            return;
        }
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
            FirebaseReaderListener listener,
            Context context)
    {
        if (!isInternetEnabled(context))
        {
            listener.notifyOnError("No connection to the internet.");
            return;
        }
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
                            Log.d(TAG,  "\"" + docRef.getId() + "\"");
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
                                            Log.d(TAG, id);
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

    public boolean isInternetEnabled(Context context)
    {
        try
        {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = manager.getActiveNetwork();
            if (network != null)
            {
                return manager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && manager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
            return false;
        } catch (NullPointerException npe)
        {
            return false;
        }

    }
}
