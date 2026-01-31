package com.lost.blog.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 单项关注信息可见性设置
 * 
 * 用于存储关注列表、粉丝列表、朋友列表、统计数据中某一项的可见性设置
 */
@Embeddable
public class VisibilitySetting {

    /**
     * 是否公开可见（所有人都可以看）
     * 默认为true
     */
    @Column(nullable = false)
    private boolean isPublic = true;

    /**
     * 是否对朋友可见（互相关注的用户可以看）
     */
    @Column(nullable = false)
    private boolean visibleToFriends = false;

    /**
     * 是否对我关注的人可见（我关注的用户可以看）
     */
    @Column(nullable = false)
    private boolean visibleToFollowing = false;

    /**
     * 允许查看的用户昵称列表（用逗号分隔存储）
     */
    @Column(length = 2000)
    private String allowedNicknames;

    /**
     * 禁止查看的用户昵称列表（用逗号分隔存储）
     */
    @Column(length = 2000)
    private String blockedNicknames;

    public VisibilitySetting() {
    }

    /**
     * 创建默认设置（公开）
     */
    public static VisibilitySetting createDefault() {
        VisibilitySetting setting = new VisibilitySetting();
        setting.setPublic(true);
        setting.setVisibleToFriends(false);
        setting.setVisibleToFollowing(false);
        return setting;
    }

    // --- Helper Methods ---

    public Set<String> getAllowedNicknameSet() {
        if (allowedNicknames == null || allowedNicknames.trim().isEmpty()) {
            return new HashSet<>();
        }
        Set<String> result = new HashSet<>();
        for (String nickname : allowedNicknames.split(",")) {
            String trimmed = nickname.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public void setAllowedNicknameSet(Set<String> nicknames) {
        if (nicknames == null || nicknames.isEmpty()) {
            this.allowedNicknames = null;
        } else {
            this.allowedNicknames = String.join(",", nicknames);
        }
    }

    public Set<String> getBlockedNicknameSet() {
        if (blockedNicknames == null || blockedNicknames.trim().isEmpty()) {
            return new HashSet<>();
        }
        Set<String> result = new HashSet<>();
        for (String nickname : blockedNicknames.split(",")) {
            String trimmed = nickname.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public void setBlockedNicknameSet(Set<String> nicknames) {
        if (nicknames == null || nicknames.isEmpty()) {
            this.blockedNicknames = null;
        } else {
            this.blockedNicknames = String.join(",", nicknames);
        }
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

    public String getAllowedNicknames() {
        return allowedNicknames;
    }

    public void setAllowedNicknames(String allowedNicknames) {
        this.allowedNicknames = allowedNicknames;
    }

    public String getBlockedNicknames() {
        return blockedNicknames;
    }

    public void setBlockedNicknames(String blockedNicknames) {
        this.blockedNicknames = blockedNicknames;
    }
}
