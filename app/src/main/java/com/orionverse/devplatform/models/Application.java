package com.orionverse.devplatform.models;

import com.google.firebase.Timestamp;

public class Application {
    public enum ApplicationStatus {
        PENDING, ACCEPTED, REJECTED
    }

    private String applicationId;
    private String postId;
    private String postTitle;
    private String developerId;
    private String developerName;
    private String developerImageUrl;
    private String proposal;
    private String status; // Store as String for Firestore
    private Timestamp appliedAt;

    // Empty constructor for Firestore
    public Application() {
        this.status = ApplicationStatus.PENDING.name();
    }

    public Application(String postId, String postTitle, String developerId, 
                      String developerName, String proposal) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.developerId = developerId;
        this.developerName = developerName;
        this.proposal = proposal;
        this.status = ApplicationStatus.PENDING.name();
        this.appliedAt = Timestamp.now();
    }

    // Getters
    public String getApplicationId() { return applicationId; }
    public String getPostId() { return postId; }
    public String getPostTitle() { return postTitle; }
    public String getDeveloperId() { return developerId; }
    public String getDeveloperName() { return developerName; }
    public String getDeveloperImageUrl() { return developerImageUrl; }
    public String getProposal() { return proposal; }
    public String getStatus() { return status; }
    public Timestamp getAppliedAt() { return appliedAt; }

    // Setters
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    public void setDeveloperId(String developerId) { this.developerId = developerId; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }
    public void setDeveloperImageUrl(String developerImageUrl) { this.developerImageUrl = developerImageUrl; }
    public void setProposal(String proposal) { this.proposal = proposal; }
    public void setStatus(String status) { this.status = status; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }

    // Helper method
    public ApplicationStatus getStatusEnum() {
        try {
            return ApplicationStatus.valueOf(status);
        } catch (Exception e) {
            return ApplicationStatus.PENDING;
        }
    }
}
