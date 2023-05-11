package com.example.mp08_firebase;

import java.io.Serializable;
import java.util.List;

public class Chat implements Serializable {

    private String chatId;
    private String lastMessage;
    private List<String> users;

    public Chat() {
    }

    public Chat(String chatId, List<String> users) {
        this.chatId = chatId;
        this.users = users;
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

}