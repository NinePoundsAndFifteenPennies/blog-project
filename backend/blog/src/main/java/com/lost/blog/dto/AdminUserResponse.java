package com.lost.blog.dto;

import java.time.LocalDateTime;

/**
 * 管理员用户响应DTO
 * 包含用户详细信息以及统计数据（发文数、评论数）
 */
public class AdminUserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
    private String bio;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计数据
    private Long postCount;
    private Long commentCount;

    public AdminUserResponse() {
    }

    public AdminUserResponse(Long id, String username, String nickname, String email, 
                            String avatarUrl, String bio, String role, Boolean enabled,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            Long postCount, Long commentCount) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postCount = postCount;
        this.commentCount = commentCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
}
