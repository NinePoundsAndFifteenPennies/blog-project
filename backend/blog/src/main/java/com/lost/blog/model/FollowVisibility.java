package com.lost.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 关注信息可见性设置实体
 * 
 * 用户可以分别控制关注列表、粉丝列表、朋友列表和关注统计对谁可见。
 * 每种类型都有独立的可见性设置。
 * 
 * 可见性规则（按优先级）：
 * 1. 如果viewer是profileOwner本人 -> 允许访问
 * 2. 如果viewerNickname在blockedNicknames中 -> 拒绝访问（最高优先级）
 * 3. 如果isPublic为true -> 允许访问
 * 4. 如果viewerNickname在allowedNicknames中 -> 允许访问
 * 5. 如果visibleToFriends为true且viewer是朋友 -> 允许访问
 * 6. 如果visibleToFollowing为true且profileOwner关注了viewer -> 允许访问
 * 7. 否则 -> 拒绝访问
 * 
 * 设计说明：
 * - 每个用户有一个可见性设置记录
 * - 四种类型（following/followers/friends/stats）分别有独立的设置
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
     * 关注列表可见性设置
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "isPublic", column = @Column(name = "following_is_public")),
        @AttributeOverride(name = "visibleToFriends", column = @Column(name = "following_visible_to_friends")),
        @AttributeOverride(name = "visibleToFollowing", column = @Column(name = "following_visible_to_following")),
        @AttributeOverride(name = "allowedNicknames", column = @Column(name = "following_allowed_nicknames", length = 2000)),
        @AttributeOverride(name = "blockedNicknames", column = @Column(name = "following_blocked_nicknames", length = 2000))
    })
    private VisibilitySetting followingSetting = VisibilitySetting.createDefault();

    /**
     * 粉丝列表可见性设置
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "isPublic", column = @Column(name = "followers_is_public")),
        @AttributeOverride(name = "visibleToFriends", column = @Column(name = "followers_visible_to_friends")),
        @AttributeOverride(name = "visibleToFollowing", column = @Column(name = "followers_visible_to_following")),
        @AttributeOverride(name = "allowedNicknames", column = @Column(name = "followers_allowed_nicknames", length = 2000)),
        @AttributeOverride(name = "blockedNicknames", column = @Column(name = "followers_blocked_nicknames", length = 2000))
    })
    private VisibilitySetting followersSetting = VisibilitySetting.createDefault();

    /**
     * 朋友列表可见性设置
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "isPublic", column = @Column(name = "friends_is_public")),
        @AttributeOverride(name = "visibleToFriends", column = @Column(name = "friends_visible_to_friends")),
        @AttributeOverride(name = "visibleToFollowing", column = @Column(name = "friends_visible_to_following")),
        @AttributeOverride(name = "allowedNicknames", column = @Column(name = "friends_allowed_nicknames", length = 2000)),
        @AttributeOverride(name = "blockedNicknames", column = @Column(name = "friends_blocked_nicknames", length = 2000))
    })
    private VisibilitySetting friendsSetting = VisibilitySetting.createDefault();

    /**
     * 统计数据可见性设置
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "isPublic", column = @Column(name = "stats_is_public")),
        @AttributeOverride(name = "visibleToFriends", column = @Column(name = "stats_visible_to_friends")),
        @AttributeOverride(name = "visibleToFollowing", column = @Column(name = "stats_visible_to_following")),
        @AttributeOverride(name = "allowedNicknames", column = @Column(name = "stats_allowed_nicknames", length = 2000)),
        @AttributeOverride(name = "blockedNicknames", column = @Column(name = "stats_blocked_nicknames", length = 2000))
    })
    private VisibilitySetting statsSetting = VisibilitySetting.createDefault();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // 确保嵌入对象不为null
        if (followingSetting == null) followingSetting = VisibilitySetting.createDefault();
        if (followersSetting == null) followersSetting = VisibilitySetting.createDefault();
        if (friendsSetting == null) friendsSetting = VisibilitySetting.createDefault();
        if (statsSetting == null) statsSetting = VisibilitySetting.createDefault();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public VisibilitySetting getFollowingSetting() {
        if (followingSetting == null) {
            followingSetting = VisibilitySetting.createDefault();
        }
        return followingSetting;
    }

    public void setFollowingSetting(VisibilitySetting followingSetting) {
        this.followingSetting = followingSetting;
    }

    public VisibilitySetting getFollowersSetting() {
        if (followersSetting == null) {
            followersSetting = VisibilitySetting.createDefault();
        }
        return followersSetting;
    }

    public void setFollowersSetting(VisibilitySetting followersSetting) {
        this.followersSetting = followersSetting;
    }

    public VisibilitySetting getFriendsSetting() {
        if (friendsSetting == null) {
            friendsSetting = VisibilitySetting.createDefault();
        }
        return friendsSetting;
    }

    public void setFriendsSetting(VisibilitySetting friendsSetting) {
        this.friendsSetting = friendsSetting;
    }

    public VisibilitySetting getStatsSetting() {
        if (statsSetting == null) {
            statsSetting = VisibilitySetting.createDefault();
        }
        return statsSetting;
    }

    public void setStatsSetting(VisibilitySetting statsSetting) {
        this.statsSetting = statsSetting;
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
