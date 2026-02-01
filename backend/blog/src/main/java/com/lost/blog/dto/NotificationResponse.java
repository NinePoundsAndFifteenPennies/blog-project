package com.lost.blog.dto;

import com.lost.blog.model.Notification;
import com.lost.blog.model.NotificationType;

import java.time.LocalDateTime;

/**
 * 通知响应 DTO
 */
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private Long actorId;
    private String actorUsername;
    private String actorNickname;
    private String actorAvatarUrl;
    private Long postId;
    private String postTitle;
    private Long commentId;
    private Long parentCommentId;  // For replies, the parent comment ID
    private String content;
    private boolean read;
    private LocalDateTime createdAt;

    // Default constructor
    public NotificationResponse() {}

    // Constructor from entity
    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setContent(notification.getContent());

        // Set actor info
        if (notification.getActor() != null) {
            response.setActorId(notification.getActor().getId());
            response.setActorUsername(notification.getActor().getUsername());
            response.setActorNickname(notification.getActor().getNickname());
            response.setActorAvatarUrl(notification.getActor().getAvatarUrl());
        }

        // Set post info
        if (notification.getPost() != null) {
            response.setPostId(notification.getPost().getId());
            response.setPostTitle(notification.getPost().getTitle());
        }

        // Set comment info
        if (notification.getComment() != null) {
            response.setCommentId(notification.getComment().getId());
            // Set parent comment ID for replies (sub-comments)
            if (notification.getComment().getParent() != null) {
                response.setParentCommentId(notification.getComment().getParent().getId());
            }
        }

        return response;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorNickname() {
        return actorNickname;
    }

    public void setActorNickname(String actorNickname) {
        this.actorNickname = actorNickname;
    }

    public String getActorAvatarUrl() {
        return actorAvatarUrl;
    }

    public void setActorAvatarUrl(String actorAvatarUrl) {
        this.actorAvatarUrl = actorAvatarUrl;
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

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
