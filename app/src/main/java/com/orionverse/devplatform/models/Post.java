package com.orionverse.devplatform.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Post {
    public enum PostType {
        PROBLEM, SOLUTION, GENERAL
    }

    private String postId;
    private String authorId;
    private String authorName;
    private String title;
    private String description;
    private String postType; // Store as String for Firestore
    private List<String> tags;
    private int applicationsCount;
    private Timestamp createdAt;

    // Empty constructor for Firestore
    public Post() {
        this.tags = new ArrayList<>();
        this.applicationsCount = 0;
    }

    public Post(String authorId, String authorName, String title, String description, PostType postType) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.description = description;
        this.postType = postType.name();
        this.tags = new ArrayList<>();
        this.applicationsCount = 0;
        this.createdAt = Timestamp.now();
    }

    // Getters
    public String getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPostType() { return postType; }
    public List<String> getTags() { return tags; }
    public int getApplicationsCount() { return applicationsCount; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPostType(String postType) { this.postType = postType; }
    public void setPostTypeEnum(String postTypeEnum) { this.postType = postTypeEnum; } // For desktop compatibility
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setApplicationsCount(int applicationsCount) { this.applicationsCount = applicationsCount; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Helper method to get PostType enum
    public PostType getPostTypeEnum() {
        try {
            return PostType.valueOf(postType);
        } catch (Exception e) {
            return PostType.GENERAL;
        }
    }
}
