package com.example.mp08_firebase.items;

import java.io.Serializable;
import java.util.List;

public class Chat implements Serializable {

    private String chatId;
    private long lastMessageTimestamp;
    private List<String> users;

    public Chat(){}

    public Chat(String chatId, List<String> users, Long lastMessageTimestamp) {
        this.chatId = chatId;
        this.users = users;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}