package com.lost.blog.dto;

/**
 * 关注可见性设置请求DTO
 * 
 * 支持分别设置四种类型的可见性：
 * - following: 关注列表
 * - followers: 粉丝列表
 * - friends: 朋友列表
 * - stats: 统计数据
 * 
 * 每种类型都可以独立设置可见性规则
 */
public class FollowVisibilityRequest {

    /**
     * 关注列表可见性设置
     */
    private VisibilitySettingDto following;

    /**
     * 粉丝列表可见性设置
     */
    private VisibilitySettingDto followers;

    /**
     * 朋友列表可见性设置
     */
    private VisibilitySettingDto friends;

    /**
     * 统计数据可见性设置
     */
    private VisibilitySettingDto stats;

    public FollowVisibilityRequest() {
    }

    // --- Getters and Setters ---

    public VisibilitySettingDto getFollowing() {
        return following;
    }

    public void setFollowing(VisibilitySettingDto following) {
        this.following = following;
    }

    public VisibilitySettingDto getFollowers() {
        return followers;
    }

    public void setFollowers(VisibilitySettingDto followers) {
        this.followers = followers;
    }

    public VisibilitySettingDto getFriends() {
        return friends;
    }

    public void setFriends(VisibilitySettingDto friends) {
        this.friends = friends;
    }

    public VisibilitySettingDto getStats() {
        return stats;
    }

    public void setStats(VisibilitySettingDto stats) {
        this.stats = stats;
    }
}
