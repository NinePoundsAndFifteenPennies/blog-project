package com.lost.blog.dto;

import java.util.Set;

/**
 * 关注可见性设置请求DTO
 */
public class FollowVisibilityRequest {

    /**
     * 是否公开可见（所有人都可以看）
     */
    private Boolean isPublic;

    /**
     * 是否对朋友可见（互相关注的用户可以看）
     */
    private Boolean visibleToFriends;

    /**
     * 是否对我关注的人可见（我关注的用户可以看）
     */
    private Boolean visibleToFollowing;

    /**
     * 允许查看的用户昵称列表
     */
    private Set<String> allowedNicknames;

    /**
     * 禁止查看的用户昵称列表
     */
    private Set<String> blockedNicknames;

    public FollowVisibilityRequest() {
    }

    // --- Getters and Setters ---

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getVisibleToFriends() {
        return visibleToFriends;
    }

    public void setVisibleToFriends(Boolean visibleToFriends) {
        this.visibleToFriends = visibleToFriends;
    }

    public Boolean getVisibleToFollowing() {
        return visibleToFollowing;
    }

    public void setVisibleToFollowing(Boolean visibleToFollowing) {
        this.visibleToFollowing = visibleToFollowing;
    }

    public Set<String> getAllowedNicknames() {
        return allowedNicknames;
    }

    public void setAllowedNicknames(Set<String> allowedNicknames) {
        this.allowedNicknames = allowedNicknames;
    }

    public Set<String> getBlockedNicknames() {
        return blockedNicknames;
    }

    public void setBlockedNicknames(Set<String> blockedNicknames) {
        this.blockedNicknames = blockedNicknames;
    }
}
