package com.example.nyumbakumitool.ui.Reports;

import java.util.List;

public class ReportModel {
    private String reportId;
    private String userId;
    private String username;
    private String profileImage;
    private String message;
    private List<String> mediaUrls;
    private long createdAt;
    private List<CommentModel> comments;

    public ReportModel() {
        // Required empty constructor for Firebase
    }

    public ReportModel(String reportId, String userId, String username, String profileImage, String message, List<String> mediaUrls, long createdAt, List<CommentModel> comments) {
        this.reportId = reportId;
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.message = message;
        this.mediaUrls = mediaUrls;
        this.createdAt = createdAt;
        this.comments = comments;
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public long getCreatedAt() { return createdAt; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<CommentModel> getComments() { return comments; }
    public void setComments(List<CommentModel> comments) { this.comments = comments; }
}
