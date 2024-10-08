package com.example.myyoutube;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    private int id;
    private String _id;
    private String username;
    private String profilePicture;
    private String token;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    //    private String username;
//    private String password;
//    private String displayName;
//    private transient Bitmap profileImage; // transient to avoid serialization
//    private String profileImageBase64; // Base64 representation of the profile image
//    private List<Post> likedPosts; // List of liked posts
//    private List<Post> dislikedPosts; // List of disliked posts
//    private List<Post> userPosts; // List of user's own posts
//
//    // Constructor to initialize lists
//    public User() {
//        likedPosts = new ArrayList<>();
//        dislikedPosts = new ArrayList<>();
//        userPosts = new ArrayList<>();
//    }
//
//    // Getters and setters
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
//    }
//
//    public Bitmap getProfileImage() {
//        return profileImage;
//    }
//
//    public void setProfileImage(Bitmap profileImage) {
//        this.profileImage = profileImage;
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
//    public List<Post> getLikedPosts() {
//        return likedPosts;
//    }
//
//    public void addLikedPost(Post post) {
//        if (!isLiked(post)) {
//            likedPosts.add(post);
//        }
//    }
//
//    public void removeLikedPost(Post post) {
//        likedPosts.removeIf(p -> p.getVideoUri().equals(post.getVideoUri()));
//    }
//
//    public boolean isLiked(Post post) {
//        return likedPosts.stream().anyMatch(p -> p.getVideoUri().equals(post.getVideoUri()));
//    }
//
//    public List<Post> getDislikedPosts() {
//        return dislikedPosts;
//    }
//
//    public void addDislikedPost(Post post) {
//        if (!isDisliked(post)) {
//            dislikedPosts.add(post);
//        }
//    }
//
//    public void removeDislikedPost(Post post) {
//        dislikedPosts.removeIf(p -> p.getVideoUri().equals(post.getVideoUri()));
//    }
//
//    public boolean isDisliked(Post post) {
//        return dislikedPosts.stream().anyMatch(p -> p.getVideoUri().equals(post.getVideoUri()));
//    }
//
//    public List<Post> getUserPosts() {
//        return userPosts;
//    }
//
//    public void addUserPost(Post post) {
//        userPosts.add(post);
//    }
//
//    public void removeUserPost(Post post) {
//        userPosts.removeIf(p -> p.getVideoUri().equals(post.getVideoUri()));
//    }
}

