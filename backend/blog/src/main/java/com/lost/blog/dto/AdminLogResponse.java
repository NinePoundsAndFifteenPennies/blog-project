package com.lost.blog.dto;

import com.lost.blog.model.AdminLog;
import com.lost.blog.model.AdminLogType;

import java.time.LocalDateTime;

/**
 * 管理操作日志响应DTO
 */
public class AdminLogResponse {
    private Long id;
    private AdminLogType operationType;
    private String title;
    private String description;

    // 管理员信息
    private Long adminId;
    private String adminUsername;
    private String adminNickname;

    // 关联信息
    private Long postId;
    private String postTitle;
    private Long commentId;
    private String commentContentPreview;
    private Long tagId;
    private String tagName;
    private Long categoryId;
    private String categoryName;
    private Long targetUserId;
    private String targetUsername;
    private Long formId;

    // 来自关联表单的扩展信息
    private String extraFields;

    private LocalDateTime createdAt;

    public AdminLogResponse() {
    }

    /**
     * 从实体转换为响应DTO
     */
    public static AdminLogResponse fromEntity(AdminLog log) {
        if (log == null) {
            return null;
        }
        AdminLogResponse response = new AdminLogResponse();
        response.setId(log.getId());
        response.setOperationType(log.getOperationType());
        response.setTitle(log.getTitle());
        response.setDescription(log.getDescription());
        response.setPostId(log.getPostId());
        response.setPostTitle(log.getPostTitle());
        response.setCommentId(log.getCommentId());
        response.setCommentContentPreview(log.getCommentContentPreview());
        response.setTagId(log.getTagId());
        response.setTagName(log.getTagName());
        response.setCategoryId(log.getCategoryId());
        response.setCategoryName(log.getCategoryName());
        response.setTargetUserId(log.getTargetUserId());
        response.setTargetUsername(log.getTargetUsername());
        response.setFormId(log.getFormId());
        response.setExtraFields(log.getExtraFields());
        response.setCreatedAt(log.getCreatedAt());

        if (log.getAdmin() != null) {
            response.setAdminId(log.getAdmin().getId());
            response.setAdminUsername(log.getAdmin().getUsername());
            response.setAdminNickname(log.getAdmin().getNickname());
        }

        return response;
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

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public String getAdminNickname() { return adminNickname; }
    public void setAdminNickname(String adminNickname) { this.adminNickname = adminNickname; }

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
