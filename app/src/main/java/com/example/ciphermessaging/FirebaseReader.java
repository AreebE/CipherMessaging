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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import biz.source_code.crypto.Idea;

/*
 * Add a fail when the internet is off
 */
public class FirebaseReader {

    public static final String MESSAGES_DATABASE = "Message";
    public static final String CONTENT_KEY = "content";
    public static final String SENDER_KEY = "sender";
    public static final String TIME_KEY = "time";

    public static final String CONVERSATIONS_DATABASE = "conversations";
    public static final String CIPHER_KEY = "cipher";
    private static final String LIST_OF_MESSAGES_KEY= "convoID";
    private static final String LAST_TIME_SENT_KEY = "lastTimeSent";
    public static final String TITLE_KEY = "title";
    public static final String USERS_KEY = "users";

    private static final String USERNAME_DATABASE = "users";
    public static final String CONVERSATIONS_KEY = "convos";
    public static final String LAST_SEEN_KEY = "lastSeen";
    private static final String CONVO_ID_KEY = "convoID";
    public static final String PASSWORD_KEY = "password";
    public static final String USERNAME_KEY = "username";


    private static final String MESSAGE_LIST_DATABASE = "MessageList";
    private static final String MESSAGES_LINK = "messages";

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
                        try
                        {
                            loadConvo(username, otherUsername, conversationTitle, listener);

                        } catch (NoSuchAlgorithmException nsae)
                        {
                            Log.d(TAG, nsae.toString());
                        }
                    }
                });

    }

    private void loadConvo(
            String username,
            String otherUsername,
            String conversationTitle,
            FirebaseReaderListener listener) throws NoSuchAlgorithmException {
        Map<String, Object> convoData = new HashMap<>();
        convoData.put(USERS_KEY, new ArrayList<String>(){
            {
                add(username);
                add(otherUsername);
            }
        });
        KeyGenerator IDEAKey = KeyGenerator.getInstance("AES");
        IDEAKey.init(256);
        byte[] bytes = IDEAKey.generateKey().getEncoded();
        List<Long> key = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++)
        {
            key.add(i, new Long(bytes[0]));
        }
        convoData.put(TITLE_KEY, conversationTitle);
        convoData.put(CIPHER_KEY, key);

        Map<String, Object> messageListData = new HashMap<>();
        messageListData.put(MESSAGES_LINK, new ArrayList<DocumentReference>());
        database.collection("MessageList")
                .add(messageListData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        convoData.put(CONVO_ID_KEY, task.getResult());
                        Date currentDate = Calendar.getInstance().getTime();
                        convoData.put(LAST_TIME_SENT_KEY, currentDate);
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
                                                //
                                                Map<String, Object> objects = user.getData();
                                                Map<String, Object> comments = new HashMap<>();
                                                comments.put(CONVO_ID_KEY, ref);
                                                comments.put(LAST_SEEN_KEY, currentDate);
                                                ((List<Map<String, Object>>) (objects.get(CONVERSATIONS_KEY))).add(0, comments);
                                                //
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
                                                //
                                                Map<String, Object> comments = new HashMap<>();
                                                Log.d(TAG, objects.toString());
                                                comments.put(CONVO_ID_KEY, ref);
                                                comments.put(LAST_SEEN_KEY, currentDate);
                                                ((List<Map<String, Object>>) (objects.get(CONVERSATIONS_KEY))).add(0, comments);
                                                Log.d(TAG, objects.toString());
                                                //
                                                database.collection(USERNAME_DATABASE).document(user.getId()).update(objects);

                                            }
                                        });
                                    }
                                });
                    }
                });


    }


    public void getMessages(
            String convoID,
            String messageID,
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
                        try
                        {
                            ArrayList<Long> key = (ArrayList<Long>) task.getResult().get(CIPHER_KEY);
                            byte[] bytes = new byte[key.size()];
                            for (int i = 0; i < key.size(); i++)
                            {
                                bytes[i] = key.get(i).byteValue();
                            }
                            SecretKeySpec secKey = new SecretKeySpec(bytes, "AES");
                            Cipher cipher = Cipher.getInstance("AES");
                            cipher.init(Cipher.DECRYPT_MODE, secKey);
                            decryptMessages(messageID, startIndex, items, messagingFragment, cipher);

                        }
                            catch (InvalidKeyException| NoSuchPaddingException|  NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void decryptMessages
            (String messageID,
             int startIndex,
             ArrayList<ConversationDisplayFragment.MessageItem> items,
             FirebaseReaderListener messagingFragment,
             Cipher decryptor
            )
    {
        database.collection(MESSAGE_LIST_DATABASE)
                .document(messageID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot conversation = task.getResult();
                        List<DocumentReference> messageLinks = (List<DocumentReference>) conversation.get(MESSAGES_LINK);
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
                                            String content = "";
                                            try

                                            {
                                                content = new String(
                                                        decryptor.doFinal(
                                                                Base64.getDecoder().decode(message.getString(CONTENT_KEY))
                                                        )
                                                );
                                            } catch (Exception e)
                                            {
                                                Log.d(TAG, "error decrypting -- " + e.toString());
                                            }
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
            String messageListID,
            FirebaseReaderListener listener,
            Context context)
    {
        database.collection(CONVERSATIONS_DATABASE)
                .document(convoID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            ArrayList<Long> key = (ArrayList<Long>) task.getResult().get(CIPHER_KEY);
                            byte[] bytes = new byte[key.size()];
                            for (int i = 0; i < key.size(); i++)
                            {
                                bytes[i] = key.get(i).byteValue();
                            }
                            SecretKeySpec secKey = new SecretKeySpec(bytes, "AES");
                            Cipher cipher = Cipher.getInstance("AES");

                                cipher.init(Cipher.ENCRYPT_MODE, secKey);

                            String encryptedContent = new String(Base64.getEncoder().encode(cipher.doFinal(content.getBytes())));
                            loadEncryptedMessage(sender, encryptedContent, timestamp, convoID, messageListID, listener, context);
                            Log.d(TAG, "Encrypted -- " + encryptedContent);
                        }
                        catch (InvalidKeyException| NoSuchPaddingException| BadPaddingException| NoSuchAlgorithmException | IllegalBlockSizeException e) {
                            Log.d(TAG, "Error - " + e.toString());
                        }
                    }
                });
    }

    private void loadEncryptedMessage(
            String sender,
            String content,
            Date timestamp,
            String convoID,
            String messageListID,
            FirebaseReaderListener listener,
            Context context
    )
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
                        database.collection(MESSAGE_LIST_DATABASE)
                                .document(messageListID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Map<String, Object> convoData = task.getResult().getData();
                                        ((List<DocumentReference>) convoData.get(MESSAGES_LINK)).add(0, messageReference);
                                        database.collection(MESSAGE_LIST_DATABASE).document(messageListID).update(convoData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        listener.notifyOnSuccess();
                                                    }
                                                });
                                    }
                                });
                        database.collection(CONVERSATIONS_DATABASE)
                                .document(convoID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Map<String, Object> data = task.getResult().getData();
                                        data.put(LAST_TIME_SENT_KEY, timestamp);
                                        database.collection(CONVERSATIONS_DATABASE).document(convoID).update(data);
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
                        put(PASSWORD_KEY, password);
                        ArrayList<HashMap<String, Object>> convoData = new ArrayList<>();
                        put(CONVERSATIONS_KEY, convoData);
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
                        final int[] count = {0};
                        List<Map<String, Object>> convosIDs = (List<Map<String, Object>>) document.get(CONVERSATIONS_KEY);
                        for (int i = 0; i < convosIDs.size(); i++)
                        {
                            items.add(0, null);
                        }
                        for (int i = 0; i < convosIDs.size(); i ++)
                        {
                            final int index = i;
                            DocumentReference docRef = (DocumentReference) convosIDs.get(i).get(CONVO_ID_KEY);
                            Date mostRecentChanged = ((Timestamp) convosIDs.get(i).get(LAST_SEEN_KEY)).toDate();

//                            Log.d(TAG,  "\"" + docRef.getId() + "\"");
                            database.collection(CONVERSATIONS_DATABASE)
                                    .document(docRef.getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot convo = task.getResult();

                                            String id = convo.getId();
                                            String title = convo.getString(TITLE_KEY);
                                            String messageID = convo.getDocumentReference(CONVO_ID_KEY).getId();
                                            List<String> users = (List<String>) convo.get(USERS_KEY);
                                            int name = (users.get(0).equals(username))? 1: 0;
                                            items.set(index, new ContactsFragment.ConversationItem(id, (String) users.get(name), title, messageID, mostRecentChanged));
                                            count[0]++;
                                            if (count[0] == items.size())
                                            {
                                                listener.notifyOnSuccess();
                                            }

                                        }
                                    });

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

    public void updateDate(
            String username,
            String convoKey,
            Date newDate
    )
    {
        database.collection(USERNAME_DATABASE)
                .whereEqualTo(USERNAME_KEY, username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Map<String, Object> data = document.getData();
                        List<Map<String, Object>> convos = (List<Map<String, Object>>) data.get(CONVERSATIONS_KEY);

                        for (int i = 0; i < convos.size(); i++)
                        {
//                            Log.d(TAG, ((DocumentReference) (convos.get(i).get(CONVO_ID_KEY))).getId() + " = id: " + convoKey);
                            if (((DocumentReference) (convos.get(i).get(CONVO_ID_KEY))).getId().equals(convoKey))
                            {
                                Log.d(TAG, "made it here");
                                Log.d(TAG, convos.toString());
                                convos.get(i).put(LAST_SEEN_KEY, new Timestamp(newDate));
                                Log.d(TAG, convos.toString());
                                Log.d(TAG, document.getId());
                                database.collection(USERNAME_DATABASE)
                                        .document(document.getId())
                                        .update(data);

                                break;
                            }
                        }
                    }
                });
//        database.collection(CONVERSATIONS_DATABASE)
//                .document(convoKey)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        DocumentSnapshot document = task.getResult();
//                        Map<String, Object> data = document.getData();
//                        data.put(LAST_TIME_SENT_KEY, newDate);
//                        database.collection(CONVERSATIONS_DATABASE)
//                                .document(convoKey)
//                                .update(data);
//                    }
//                });
    }

    public void compareToFirstMessage(
            String convoID,
            Date userDate,
            FirebaseReaderListener listener
    )
    {
        if (userDate == null)
        {
            listener.notifyOnSuccess();
            return;
        }
        Log.d(TAG, userDate.toString());
        database.collection(CONVERSATIONS_DATABASE)
                .document(convoID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot snapshot = task.getResult();
                        Date lastSeenTime = snapshot.getDate(LAST_TIME_SENT_KEY);
                        Log.d(TAG, lastSeenTime + ": " + userDate + ", " + snapshot.getString(TITLE_KEY));
                        if (lastSeenTime.after(userDate))
                        {
                            listener.notifyOnSuccess();
                        }
                        else
                        {
                            listener.notifyOnError("");
                        }
                    }
                });
    }

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
