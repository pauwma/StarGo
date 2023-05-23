package com.example.mp08_firebase.items;

public class Message {
    private String messageId;
    private String content;
    private String senderId;
    private long timestamp;

    public Message() {
    }

    public Message(String messageId, String content, String senderId, long timestamp) {
        this.messageId = messageId;
        this.content = content;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}