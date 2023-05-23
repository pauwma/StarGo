package com.example.mp08_firebase.items;

import java.util.Date;
import java.util.List;

public class Post {
    private String postId;
    private String uid;
    private long timestamp;
    private String media;
    private String mediaType;
    private String content;
    private List<String> hashtags;

    public Post() {}

    public Post(String postId, String uid, long timestamp, String mediaUrl, String mediaType, String content, List<String> hashtags) {
        this.postId = postId;
        this.uid = uid;
        this.timestamp = timestamp;
        this.media = mediaUrl;
        this.mediaType = mediaType;
        this.content = content;
        this.hashtags = hashtags;
    }

    public Post(String uid, long timestamp, String mediaUrl, String mediaType, String content, List<String> hashtags) {
        this.uid = uid;
        this.timestamp = timestamp;
        this.media = mediaUrl;
        this.mediaType = mediaType;
        this.content = content;
        this.hashtags = hashtags;
    }


    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
}