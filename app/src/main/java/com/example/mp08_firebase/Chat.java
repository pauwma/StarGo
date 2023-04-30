package com.example.mp08_firebase;

import java.util.List;

public class Chat {

    private String chatId;
    private String otherUserProfileImageUrl;
    private String title;
    private List<String> users;

    public Chat() {
    }

    public Chat(String chatId, String title, List<String> users) {
        this.chatId = chatId;
        this.title = title;
        this.users = users;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getOtherUserProfileImageUrl() {
        return otherUserProfileImageUrl;
    }

    public void setOtherUserProfileImageUrl(String otherUserProfileImageUrl) {
        this.otherUserProfileImageUrl = otherUserProfileImageUrl;
    }
}