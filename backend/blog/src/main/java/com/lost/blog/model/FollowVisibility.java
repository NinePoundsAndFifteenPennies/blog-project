package com.lost.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 关注信息可见性设置实体
 * 
 * 用户可以控制自己的关注列表、粉丝列表、朋友列表和关注统计对谁可见。
 * 
 * 可见性规则（按优先级）：
 * 1. 如果viewerNickname在blockedNicknames中 -> 拒绝访问（最高优先级）
 * 2. 如果isPublic为true -> 允许访问
 * 3. 如果viewerNickname在allowedNicknames中 -> 允许访问
 * 4. 如果visibleToFriends为true且viewer是朋友 -> 允许访问
 * 5. 如果visibleToFollowing为true且profileOwner关注了viewer -> 允许访问
 * 6. 如果viewer是profileOwner本人 -> 允许访问
 * 7. 否则 -> 拒绝访问
 * 
 * 设计说明：
 * - 每个用户有一个可见性设置记录
 * - allowedNicknames和blockedNicknames使用nickname而不是username
 * - 多个条件可以组合使用（只要不矛盾）
 */
@Entity
@Table(name = "follow_visibility")
public class FollowVisibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户（一对一关系）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 是否公开可见（所有人都可以看）
     * 默认为true
     */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    /**
     * 是否对朋友可见（互相关注的用户可以看）
     */
    @Column(name = "visible_to_friends", nullable = false)
    private boolean visibleToFriends = false;

    /**
     * 是否对我关注的人可见（我关注的用户可以看）
     */
    @Column(name = "visible_to_following", nullable = false)
    private boolean visibleToFollowing = false;

    /**
     * 允许查看的用户昵称列表（用逗号分隔存储）
     * 使用nickname而不是username
     */
    @Column(name = "allowed_nicknames", length = 2000)
    private String allowedNicknames;

    /**
     * 禁止查看的用户昵称列表（用逗号分隔存储）
     * 使用nickname而不是username
     * 此列表优先级最高，即使满足其他条件也会被拒绝
     */
    @Column(name = "blocked_nicknames", length = 2000)
    private String blockedNicknames;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Helper Methods ---

    /**
     * 获取允许的昵称集合
     */
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

    /**
     * 设置允许的昵称集合
     */
    public void setAllowedNicknameSet(Set<String> nicknames) {
        if (nicknames == null || nicknames.isEmpty()) {
            this.allowedNicknames = null;
        } else {
            this.allowedNicknames = String.join(",", nicknames);
        }
    }

    /**
     * 获取禁止的昵称集合
     */
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

    /**
     * 设置禁止的昵称集合
     */
    public void setBlockedNicknameSet(Set<String> nicknames) {
        if (nicknames == null || nicknames.isEmpty()) {
            this.blockedNicknames = null;
        } else {
            this.blockedNicknames = String.join(",", nicknames);
        }
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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
