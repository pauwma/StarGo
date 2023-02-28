package com.example.mp08_firebase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class User {
    String uid;
    String avatar;
    String username;
    String email;
    String phone;
    String nPosts;
    DocumentReference userRef;

    public User(String uid){
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        this.uid = uid;

        fStore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userMap = documentSnapshot.getData();
                        avatar = (String) userMap.get("avatar");
                        username = (String) userMap.get("username");
                        email = (String) userMap.get("email");
                        phone = (String) userMap.get("phone");
                        nPosts = (String) userMap.get("nPosts");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });

    }
}
