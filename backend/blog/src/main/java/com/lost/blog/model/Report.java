package com.lost.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 举报实体
 * 记录用户对文章或评论的举报
 */
@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_reporter", columnList = "reporter_id"),
        @Index(name = "idx_report_reported_user", columnList = "reported_user_id"),
        @Index(name = "idx_report_created", columnList = "created_at DESC")
})
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 举报者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * 举报目标类型（文章/评论）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    /**
     * 举报目标ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * 举报内容预览（冗余存储）
     */
    @Column(name = "target_content_preview", length = 200)
    private String targetContentPreview;

    /**
     * 被举报者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    /**
     * 举报原因
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 举报状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * 关联的管理表单ID（处理时关联）
     */
    @Column(name = "admin_form_id")
    private Long adminFormId;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 处理时间
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public ReportTargetType getTargetType() { return targetType; }
    public void setTargetType(ReportTargetType targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getTargetContentPreview() { return targetContentPreview; }
    public void setTargetContentPreview(String targetContentPreview) { this.targetContentPreview = targetContentPreview; }

    public User getReportedUser() { return reportedUser; }
    public void setReportedUser(User reportedUser) { this.reportedUser = reportedUser; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public Long getAdminFormId() { return adminFormId; }
    public void setAdminFormId(Long adminFormId) { this.adminFormId = adminFormId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
