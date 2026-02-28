package com.lost.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 管理操作日志实体
 * 记录管理员的所有管理操作
 */
@Entity
@Table(name = "admin_logs", indexes = {
        @Index(name = "idx_admin_log_type", columnList = "operation_type"),
        @Index(name = "idx_admin_log_admin", columnList = "admin_id"),
        @Index(name = "idx_admin_log_created", columnList = "created_at DESC")
})
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 30)
    private AdminLogType operationType;

    /**
     * 操作标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 操作描述
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 执行操作的管理员
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /**
     * 关联的文章ID
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * 关联的文章标题（冗余存储）
     */
    @Column(name = "post_title", length = 100)
    private String postTitle;

    /**
     * 关联的评论ID
     */
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * 关联的评论内容预览（冗余存储）
     */
    @Column(name = "comment_content_preview", length = 200)
    private String commentContentPreview;

    /**
     * 关联的标签ID
     */
    @Column(name = "tag_id")
    private Long tagId;

    /**
     * 关联的标签名称（冗余存储）
     */
    @Column(name = "tag_name", length = 50)
    private String tagName;

    /**
     * 关联的分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 关联的分类名称（冗余存储）
     */
    @Column(name = "category_name", length = 50)
    private String categoryName;

    /**
     * 被操作的用户ID
     */
    @Column(name = "target_user_id")
    private Long targetUserId;

    /**
     * 被操作的用户名（冗余存储）
     */
    @Column(name = "target_username", length = 50)
    private String targetUsername;

    /**
     * 扩展字段（JSON格式，来自admin_forms的extra_fields）
     * 格式: [{"fieldName": "...", "fieldValue": "..."}, ...]
     */
    @Lob
    @Column(name = "extra_fields", columnDefinition = "TEXT")
    private String extraFields;

    /**
     * 关联的管理表单ID
     */
    @Column(name = "form_id")
    private Long formId;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AdminLogType getOperationType() { return operationType; }
    public void setOperationType(AdminLogType operationType) { this.operationType = operationType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public String getCommentContentPreview() { return commentContentPreview; }
    public void setCommentContentPreview(String commentContentPreview) { this.commentContentPreview = commentContentPreview; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public String getExtraFields() { return extraFields; }
    public void setExtraFields(String extraFields) { this.extraFields = extraFields; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
