package com.example.ciphermessaging;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseReader {

    private static String TAG = "FirebaseReader";
    private FirebaseFirestore database;
    public FirebaseReader()
    {
        database = FirebaseFirestore.getInstance();
        database.collection("users");
    }

    public Query confirmUser(String username, String password) {
        Query q = database.collection("users")
                .whereEqualTo("username", username);
        return q.whereEqualTo("password", password);

    }

    public void createUser(String firstName, String lastName, int birth)
    {
        Map<String, Object> userData = new HashMap<String, Object>()
        {{
           put("first", firstName);
           put("last", lastName);
           put("born", 1815);
        }};
        database.collection("users")
                .add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "succesfully loaded");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "failed due to " + e.toString());
                    }
                });

        database.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document: task.getResult())
                            {
                                Log.d(TAG, document.getId() + " with " + document.getData());
                            }
                        }
                        else
                        {
                            Log.d(TAG, "couldn't get documents due to " + task.getException().toString());
                        }
                    }
                });
    }
}
