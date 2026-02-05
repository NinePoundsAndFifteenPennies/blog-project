package com.lost.blog.dto;

import com.lost.blog.model.Comment;
import com.lost.blog.model.CommentStatus;

import java.time.LocalDateTime;

/**
 * 管理员评论响应DTO
 */
public class AdminCommentResponse {
    private Long id;
    private String content;
    private String contentPreview;
    
    // 作者信息
    private Long authorId;
    private String authorUsername;
    private String authorNickname;
    private String authorAvatarUrl;
    private Boolean authorEnabled;
    
    // 文章信息
    private Long postId;
    private String postTitle;
    
    // 父评论信息（用于子评论）
    private Long parentId;
    private String parentContentPreview;
    
    // 被回复用户信息
    private Long replyToUserId;
    private String replyToUsername;
    
    // 评论层级
    private Integer level;
    
    // 审核状态
    private CommentStatus status;
    
    // 统计信息
    private Long likeCount;
    private Long replyCount;
    
    // 时间信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminCommentResponse() {
    }

    /**
     * 从Comment实体创建响应DTO
     */
    public static AdminCommentResponse fromEntity(Comment comment) {
        return fromEntity(comment, 0L, 0L);
    }

    /**
     * 从Comment实体创建响应DTO（含统计信息）
     */
    public static AdminCommentResponse fromEntity(Comment comment, Long likeCount, Long replyCount) {
        AdminCommentResponse response = new AdminCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setContentPreview(truncateContent(comment.getContent(), 100));
        
        // 作者信息
        if (comment.getUser() != null) {
            response.setAuthorId(comment.getUser().getId());
            response.setAuthorUsername(comment.getUser().getUsername());
            response.setAuthorNickname(comment.getUser().getNickname());
            response.setAuthorAvatarUrl(comment.getUser().getAvatarUrl());
            response.setAuthorEnabled(comment.getUser().getEnabled());
        }
        
        // 文章信息
        if (comment.getPost() != null) {
            response.setPostId(comment.getPost().getId());
            response.setPostTitle(comment.getPost().getTitle());
        }
        
        // 父评论信息
        if (comment.getParent() != null) {
            response.setParentId(comment.getParent().getId());
            response.setParentContentPreview(truncateContent(comment.getParent().getContent(), 50));
        }
        
        // 被回复用户信息
        if (comment.getReplyToUser() != null) {
            response.setReplyToUserId(comment.getReplyToUser().getId());
            response.setReplyToUsername(comment.getReplyToUser().getUsername());
        }
        
        response.setLevel(comment.getLevel());
        response.setStatus(comment.getStatus());
        response.setLikeCount(likeCount);
        response.setReplyCount(replyCount);
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        
        return response;
    }

    /**
     * 截断内容用于预览
     */
    private static String truncateContent(String content, int maxLength) {
        if (content == null) return null;
        // 去除换行符
        String cleaned = content.replace("\n", " ").replace("\r", "");
        if (cleaned.length() <= maxLength) return cleaned;
        return cleaned.substring(0, maxLength) + "...";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public Boolean getAuthorEnabled() {
        return authorEnabled;
    }

    public void setAuthorEnabled(Boolean authorEnabled) {
        this.authorEnabled = authorEnabled;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentContentPreview() {
        return parentContentPreview;
    }

    public void setParentContentPreview(String parentContentPreview) {
        this.parentContentPreview = parentContentPreview;
    }

    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }

    public String getReplyToUsername() {
        return replyToUsername;
    }

    public void setReplyToUsername(String replyToUsername) {
        this.replyToUsername = replyToUsername;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Long replyCount) {
        this.replyCount = replyCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
