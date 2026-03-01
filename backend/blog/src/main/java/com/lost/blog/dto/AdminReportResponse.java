package com.lost.blog.dto;

import java.time.LocalDateTime;

/**
 * 管理员举报响应DTO
 */
public class AdminReportResponse {

    private Long id;
    private String targetType;
    private Long targetId;
    private String targetContentPreview;
    private String reason;
    private String status;

    // 举报者信息
    private Long reporterId;
    private String reporterUsername;
    private String reporterNickname;
    private String reporterAvatarUrl;

    // 被举报者信息
    private Long reportedUserId;
    private String reportedUsername;
    private String reportedNickname;
    private String reportedAvatarUrl;

    // 管理表单ID
    private Long adminFormId;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getTargetContentPreview() { return targetContentPreview; }
    public void setTargetContentPreview(String targetContentPreview) { this.targetContentPreview = targetContentPreview; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getReporterNickname() { return reporterNickname; }
    public void setReporterNickname(String reporterNickname) { this.reporterNickname = reporterNickname; }

    public String getReporterAvatarUrl() { return reporterAvatarUrl; }
    public void setReporterAvatarUrl(String reporterAvatarUrl) { this.reporterAvatarUrl = reporterAvatarUrl; }

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReportedUsername() { return reportedUsername; }
    public void setReportedUsername(String reportedUsername) { this.reportedUsername = reportedUsername; }

    public String getReportedNickname() { return reportedNickname; }
    public void setReportedNickname(String reportedNickname) { this.reportedNickname = reportedNickname; }

    public String getReportedAvatarUrl() { return reportedAvatarUrl; }
    public void setReportedAvatarUrl(String reportedAvatarUrl) { this.reportedAvatarUrl = reportedAvatarUrl; }

    public Long getAdminFormId() { return adminFormId; }
    public void setAdminFormId(Long adminFormId) { this.adminFormId = adminFormId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
