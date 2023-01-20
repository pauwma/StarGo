package com.example.mp08_firebase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post {
    public String uid;
    public String author;
    public String authorPhotoUrl;
    public String content;
    public String mediaUrl;
    public String mediaType;
    public Map<String, Boolean> likes = new HashMap<>();
    public Date date;

    // Constructor vacio requerido por Firestore
    public Post() {}

    public Post(String uid, String author, String authorPhotoUrl, String content, Date date, String mediaUrl, String mediaType) {
        this.uid = uid;
        this.author = author;
        this.authorPhotoUrl = authorPhotoUrl;
        this.content = content;
        this.date = date;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }
}