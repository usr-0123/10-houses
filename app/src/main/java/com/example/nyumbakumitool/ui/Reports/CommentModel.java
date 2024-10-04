package com.example.nyumbakumitool.ui.Reports;

public class CommentModel {
    private String commentId;
    private String userId;
    private String username;
    private String profileImage;
    private String message;
    private long timestamp;

    public CommentModel() {
        // Required empty constructor for Firebase
    }

    public CommentModel(String commentId, String userId, String username, String profileImage, String message, long timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
