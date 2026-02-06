package com.lost.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理表单实体
 * 用于存储审核拒绝、删除文章等操作的理由表单
 * 支持管理员扩展自定义字段
 */
@Entity
@Table(name = "admin_forms")
public class AdminForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 表单标题（用于提示字段显示）
     */
    @NotEmpty(message = "表单标题不能为空")
    @Size(max = 100, message = "表单标题不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 表单类型
     * REJECTION - 审核拒绝
     * DELETION - 删除文章
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 20)
    private AdminFormType formType;

    /**
     * 必填理由内容
     */
    @NotEmpty(message = "理由内容不能为空")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 扩展字段（JSON格式存储管理员自定义的额外内容）
     * 格式: [{"fieldName": "...", "fieldValue": "..."}, ...]
     */
    @Lob
    @Column(name = "extra_fields", columnDefinition = "TEXT")
    private String extraFields;

    /**
     * 关联的文章ID
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * 关联的文章标题（冗余存储，用于文章被删除后仍可查看）
     */
    @Column(name = "post_title", length = 100)
    private String postTitle;

    /**
     * 关联的评论ID（用于评论删除表单）
     */
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * 关联的评论内容预览（冗余存储，用于评论被删除后仍可查看）
     */
    @Column(name = "comment_content_preview", length = 200)
    private String commentContentPreview;

    /**
     * 关联的标签ID（用于标签删除表单）
     */
    @Column(name = "tag_id")
    private Long tagId;

    /**
     * 关联的标签名称（冗余存储，用于标签被删除后仍可查看）
     */
    @Column(name = "tag_name", length = 50)
    private String tagName;

    /**
     * 受影响的文章列表（JSON格式，用于标签软删除时记录移除了哪些文章的关联）
     * 格式: [{"postId": 1, "postTitle": "文章标题"}, ...]
     */
    @Lob
    @Column(name = "affected_posts", columnDefinition = "TEXT")
    private String affectedPosts;

    /**
     * 被通知的用户（文章作者或评论作者）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    /**
     * 创建此表单的管理员
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 是否已发送给用户
     */
    @Column(name = "is_sent", nullable = false)
    private Boolean sent = false;

    /**
     * 发送时间
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AdminFormType getFormType() {
        return formType;
    }

    public void setFormType(AdminFormType formType) {
        this.formType = formType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(String extraFields) {
        this.extraFields = extraFields;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getCommentContentPreview() {
        return commentContentPreview;
    }

    public void setCommentContentPreview(String commentContentPreview) {
        this.commentContentPreview = commentContentPreview;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getAffectedPosts() {
        return affectedPosts;
    }

    public void setAffectedPosts(String affectedPosts) {
        this.affectedPosts = affectedPosts;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
