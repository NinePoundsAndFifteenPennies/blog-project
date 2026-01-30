package com.lost.blog.dto;

/**
 * 关注操作响应DTO
 * 用于返回关注/取消关注操作的结果
 */
public class FollowResponse {
    
    /**
     * 是否已关注
     */
    private boolean following;
    
    /**
     * 是否为朋友（互相关注）
     */
    private boolean friend;
    
    /**
     * 操作消息
     */
    private String message;

    public FollowResponse() {
    }

    public FollowResponse(boolean following, boolean friend, String message) {
        this.following = following;
        this.friend = friend;
        this.message = message;
    }

    // --- Getters and Setters ---

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
