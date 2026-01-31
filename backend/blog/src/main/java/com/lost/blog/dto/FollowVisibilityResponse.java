package com.lost.blog.dto;

import java.util.Set;

/**
 * 关注可见性设置响应DTO
 */
public class FollowVisibilityResponse {

    /**
     * 是否公开可见（所有人都可以看）
     */
    private boolean isPublic;

    /**
     * 是否对朋友可见（互相关注的用户可以看）
     */
    private boolean visibleToFriends;

    /**
     * 是否对我关注的人可见（我关注的用户可以看）
     */
    private boolean visibleToFollowing;

    /**
     * 允许查看的用户昵称列表
     */
    private Set<String> allowedNicknames;

    /**
     * 禁止查看的用户昵称列表
     */
    private Set<String> blockedNicknames;

    public FollowVisibilityResponse() {
    }

    public FollowVisibilityResponse(boolean isPublic, boolean visibleToFriends, boolean visibleToFollowing,
                                    Set<String> allowedNicknames, Set<String> blockedNicknames) {
        this.isPublic = isPublic;
        this.visibleToFriends = visibleToFriends;
        this.visibleToFollowing = visibleToFollowing;
        this.allowedNicknames = allowedNicknames;
        this.blockedNicknames = blockedNicknames;
    }

    // --- Getters and Setters ---

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isVisibleToFriends() {
        return visibleToFriends;
    }

    public void setVisibleToFriends(boolean visibleToFriends) {
        this.visibleToFriends = visibleToFriends;
    }

    public boolean isVisibleToFollowing() {
        return visibleToFollowing;
    }

    public void setVisibleToFollowing(boolean visibleToFollowing) {
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
