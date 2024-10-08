package com.example.myyoutube;

public class LikeRequest {
    private String username;

    public LikeRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

