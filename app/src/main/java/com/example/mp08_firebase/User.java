package com.example.mp08_firebase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class User {
    private String username;
    private String avatar;

    public User() {
    }

    public User(String username, String avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return avatar;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.avatar = profileImageUrl;
    }
}
