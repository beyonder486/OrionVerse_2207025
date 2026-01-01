package com.orionverse.devplatform.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private String email;
    private String bio;
    private String profileImageUrl;
    private List<String> skills;
    private double averageRating;
    private int totalRatings;
    private Timestamp createdAt;

    // Empty constructor required for Firestore
    public User() {
        this.skills = new ArrayList<>();
        this.averageRating = 0.0;
        this.totalRatings = 0;
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bio = "";
        this.profileImageUrl = "";
        this.skills = new ArrayList<>();
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.createdAt = Timestamp.now();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public List<String> getSkills() { return skills; }
    public double getAverageRating() { return averageRating; }
    public int getTotalRatings() { return totalRatings; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setBio(String bio) { this.bio = bio; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
