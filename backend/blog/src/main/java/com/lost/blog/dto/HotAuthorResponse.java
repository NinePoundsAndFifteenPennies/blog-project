package com.lost.blog.dto;

import java.time.LocalDateTime;

/**
 * 热门作者响应DTO
 * 用于返回热门作者信息给前端
 */
public class HotAuthorResponse {

    private Long id;               // 用户ID
    private String username;       // 用户名
    private String nickname;       // 昵称
    private String avatarUrl;      // 头像URL
    private String bio;            // 简介
    private Long followerCount;    // 粉丝数
    private Long articleCount;     // 文章数
    private Long likeCount;        // 获赞数
    private Long commentCount;     // 评论数
    private Long viewCount;        // 浏览量
    private Double heatScore;      // 热度值
    private LocalDateTime lastActiveAt;  // 最后活跃时间

    // 默认构造函数
    public HotAuthorResponse() {
    }

    // 全参构造函数
    public HotAuthorResponse(Long id, String username, String nickname, String avatarUrl, 
                             String bio, Long followerCount, Long articleCount, 
                             Long likeCount, Long commentCount, Long viewCount, 
                             Double heatScore, LocalDateTime lastActiveAt) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.followerCount = followerCount;
        this.articleCount = articleCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.heatScore = heatScore;
        this.lastActiveAt = lastActiveAt;
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

    public Long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Long followerCount) {
        this.followerCount = followerCount;
    }

    public Long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Double getHeatScore() {
        return heatScore;
    }

    public void setHeatScore(Double heatScore) {
        this.heatScore = heatScore;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
