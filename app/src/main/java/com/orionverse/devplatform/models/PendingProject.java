package com.orionverse.devplatform.models;

import com.google.firebase.Timestamp;

public class PendingProject {
    public enum ProjectStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    private String projectId;
    private String postId;
    private String postTitle;
    private String postDescription;
    private String authorId;
    private String authorName;
    private String developerId;
    private String developerName;
    private String applicationId;
    private String status; // Store as String for Firestore
    private Timestamp acceptedAt;
    private Timestamp completedAt;

    // Empty constructor for Firestore
    public PendingProject() {
        this.status = ProjectStatus.PENDING.name();
        this.acceptedAt = Timestamp.now();
    }

    public PendingProject(String postId, String postTitle, String postDescription,
                         String authorId, String authorName,
                         String developerId, String developerName,
                         String applicationId) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.authorId = authorId;
        this.authorName = authorName;
        this.developerId = developerId;
        this.developerName = developerName;
        this.applicationId = applicationId;
        this.status = ProjectStatus.PENDING.name();
        this.acceptedAt = Timestamp.now();
    }

    // Getters
    public String getProjectId() { return projectId; }
    public String getPostId() { return postId; }
    public String getPostTitle() { return postTitle; }
    public String getPostDescription() { return postDescription; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getDeveloperId() { return developerId; }
    public String getDeveloperName() { return developerName; }
    public String getApplicationId() { return applicationId; }
    public String getStatus() { return status; }
    public Timestamp getAcceptedAt() { return acceptedAt; }
    public Timestamp getCompletedAt() { return completedAt; }

    // Setters
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    public void setPostDescription(String postDescription) { this.postDescription = postDescription; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setDeveloperId(String developerId) { this.developerId = developerId; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setStatus(String status) { this.status = status; }
    public void setAcceptedAt(Timestamp acceptedAt) { this.acceptedAt = acceptedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    // Helper method
    public ProjectStatus getStatusEnum() {
        try {
            return ProjectStatus.valueOf(status);
        } catch (Exception e) {
            return ProjectStatus.PENDING;
        }
    }
}
