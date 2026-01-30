package com.lost.blog.dto;

/**
 * 关注统计响应DTO
 * 用于返回用户的关注相关统计数据
 */
public class FollowStatsResponse {
    
    /**
     * 关注数（该用户关注了多少人）
     */
    private long followingCount;
    
    /**
     * 粉丝数（有多少人关注该用户）
     */
    private long followerCount;
    
    /**
     * 朋友数（互相关注的用户数量）
     */
    private long friendCount;
    
    /**
     * 当前登录用户是否已关注该用户（可选，用于查看他人资料时）
     */
    private Boolean isFollowing;
    
    /**
     * 是否为朋友关系（可选，用于查看他人资料时）
     */
    private Boolean isFriend;

    public FollowStatsResponse() {
    }

    public FollowStatsResponse(long followingCount, long followerCount, long friendCount) {
        this.followingCount = followingCount;
        this.followerCount = followerCount;
        this.friendCount = friendCount;
    }

    public FollowStatsResponse(long followingCount, long followerCount, long friendCount, 
                               Boolean isFollowing, Boolean isFriend) {
        this.followingCount = followingCount;
        this.followerCount = followerCount;
        this.friendCount = friendCount;
        this.isFollowing = isFollowing;
        this.isFriend = isFriend;
    }

    // --- Getters and Setters ---

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(long friendCount) {
        this.friendCount = friendCount;
    }

    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(Boolean isFollowing) {
        this.isFollowing = isFollowing;
    }

    public Boolean getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(Boolean isFriend) {
        this.isFriend = isFriend;
    }
}
