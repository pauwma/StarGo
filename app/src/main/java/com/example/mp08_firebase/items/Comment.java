package com.example.mp08_firebase.items;

public class Comment {
    private String content, postID, commentID, uid;
    private long timestamp;

    public Comment(String content, String postID, String commentID, String uid, long timestamp) {
        this.content = content;
        this.postID = postID;
        this.commentID = commentID;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
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
}