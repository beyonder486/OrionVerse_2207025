package com.orionverse.devplatform.models;

import com.google.firebase.Timestamp;

public class Notification {
    public enum NotificationType {
        APPLICATION_ACCEPTED,
        APPLICATION_REJECTED,
        APPLICATION,
        NEW_APPLICATION,
        GENERAL
    }

    private String notificationId;
    private String userId;
    private String type; // Store as String for Firestore
    private String title;
    private String message;
    private String relatedId; // Application ID or Post ID
    private boolean read;
    private Timestamp createdAt;

    // Empty constructor for Firestore
    public Notification() {
        this.read = false;
        this.createdAt = Timestamp.now();
    }

    public Notification(String userId, NotificationType type, String title, String message, String relatedId) {
        this.userId = userId;
        this.type = type.name();
        this.title = title;
        this.message = message;
        this.relatedId = relatedId;
        this.read = false;
        this.createdAt = Timestamp.now();
    }

    // Getters
    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getRelatedId() { return relatedId; }
    public boolean isRead() { return read; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public void setRead(boolean read) { this.read = read; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Helper method
    public NotificationType getTypeEnum() {
        try {
            return NotificationType.valueOf(type);
        } catch (Exception e) {
            return NotificationType.GENERAL;
        }
    }
}
