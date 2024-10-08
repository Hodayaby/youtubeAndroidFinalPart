package com.example.myyoutube;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey
    private int id;
    private String _id;
    private String user;
    private String text;
    private int videoId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    //    private String timestamp;
//    private String profileImageBase64;

//    public Comment(String username, String content, String profileImageBase64) {
//        this.user = username;
//        this.text = content;
//        this.timestamp = getCurrentTimestamp();
//        this.profileImageBase64 = profileImageBase64;
//    }
//
//    public Comment(String username, String content) {
//        this.user = username;
//        this.text = content;
//        this.timestamp = getCurrentTimestamp();
//    }

//    public String getUsername() {
//        return user;
//    }
//
//    public void setUsername(String username) {
//        this.user = username;
//    }
//
//    public String getContent() {
//        return text;
//    }
//
//    public void setContent(String content) {
//        this.text = content;
//    }

//    public String getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(String timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    public String getProfileImageBase64() {
//        return profileImageBase64;
//    }
//
//    public void setProfileImageBase64(String profileImageBase64) {
//        this.profileImageBase64 = profileImageBase64;
//    }
//
//    private String getCurrentTimestamp() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        return sdf.format(new Date());
//    }
}
