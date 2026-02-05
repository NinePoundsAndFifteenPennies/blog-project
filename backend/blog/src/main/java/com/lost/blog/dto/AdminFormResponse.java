package com.lost.blog.dto;

import com.lost.blog.model.AdminForm;
import com.lost.blog.model.AdminFormType;

import java.time.LocalDateTime;

/**
 * 管理表单响应DTO
 */
public class AdminFormResponse {
    private Long id;
    private String title;
    private AdminFormType formType;
    private String reason;
    private String extraFields;
    private Long postId;
    private String postTitle;
    
    // 评论信息（用于评论删除表单）
    private Long commentId;
    private String commentContentPreview;
    
    // 目标用户信息
    private Long targetUserId;
    private String targetUsername;
    private String targetNickname;
    
    // 管理员信息
    private Long adminId;
    private String adminUsername;
    private String adminNickname;
    
    private LocalDateTime createdAt;
    private Boolean sent;
    private LocalDateTime sentAt;

    public AdminFormResponse() {
    }

    /**
     * 从实体转换为响应DTO
     */
    public static AdminFormResponse fromEntity(AdminForm form) {
        if (form == null) {
            return null;
        }
        AdminFormResponse response = new AdminFormResponse();
        response.setId(form.getId());
        response.setTitle(form.getTitle());
        response.setFormType(form.getFormType());
        response.setReason(form.getReason());
        response.setExtraFields(form.getExtraFields());
        response.setPostId(form.getPostId());
        response.setPostTitle(form.getPostTitle());
        response.setCommentId(form.getCommentId());
        response.setCommentContentPreview(form.getCommentContentPreview());
        response.setCreatedAt(form.getCreatedAt());
        response.setSent(form.getSent());
        response.setSentAt(form.getSentAt());
        
        if (form.getTargetUser() != null) {
            response.setTargetUserId(form.getTargetUser().getId());
            response.setTargetUsername(form.getTargetUser().getUsername());
            response.setTargetNickname(form.getTargetUser().getNickname());
        }
        
        if (form.getAdmin() != null) {
            response.setAdminId(form.getAdmin().getId());
            response.setAdminUsername(form.getAdmin().getUsername());
            response.setAdminNickname(form.getAdmin().getNickname());
        }
        
        return response;
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

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getTargetNickname() {
        return targetNickname;
    }

    public void setTargetNickname(String targetNickname) {
        this.targetNickname = targetNickname;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminNickname() {
        return adminNickname;
    }

    public void setAdminNickname(String adminNickname) {
        this.adminNickname = adminNickname;
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
