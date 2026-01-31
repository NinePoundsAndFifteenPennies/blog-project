package com.lost.blog.dto;

/**
 * 关注统计响应DTO
 * 用于返回用户的关注相关统计数据
 * 
 * 注意：当用户设置了可见性限制且当前查看者无权查看时，
 * followingCount、followerCount、friendCount 会返回 HIDDEN_COUNT (-1)
 */
public class FollowStatsResponse {

    /**
     * 表示统计数据被隐藏的常量值
     * 当用户设置了可见性限制且当前查看者无权查看时使用此值
     */
    public static final long HIDDEN_COUNT = -1;
    
    /**
     * 关注数（该用户关注了多少人）
     * 值为 HIDDEN_COUNT (-1) 表示该数据被隐藏
     */
    private long followingCount;
    
    /**
     * 粉丝数（有多少人关注该用户）
     * 值为 HIDDEN_COUNT (-1) 表示该数据被隐藏
     */
    private long followerCount;
    
    /**
     * 朋友数（互相关注的用户数量）
     * 值为 HIDDEN_COUNT (-1) 表示该数据被隐藏
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

    /**
     * 创建一个隐藏统计数据的响应
     * @param isFollowing 是否已关注
     * @param isFriend 是否为朋友
     * @return 隐藏统计数据的响应
     */
    public static FollowStatsResponse hidden(Boolean isFollowing, Boolean isFriend) {
        FollowStatsResponse response = new FollowStatsResponse();
        response.setFollowingCount(HIDDEN_COUNT);
        response.setFollowerCount(HIDDEN_COUNT);
        response.setFriendCount(HIDDEN_COUNT);
        response.setIsFollowing(isFollowing);
        response.setIsFriend(isFriend);
        return response;
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
