package com.lost.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 关注关系实体
 * 表示用户A关注用户B的单向关系
 * 当用户A关注用户B时：
 *   - follower = 用户A（关注者/粉丝）
 *   - followed = 用户B（被关注者）
 * 
 * 设计说明：
 * - 使用联合唯一约束确保不会重复关注
 * - 保留创建时间用于后期关系图生成等功能
 * - 使用LAZY加载优化性能
 */
@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "followed_id"})
})
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关注者（粉丝）- 执行关注动作的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /**
     * 被关注者 - 被关注的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    /**
     * 关注时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowed() {
        return followed;
    }

    public void setFollowed(User followed) {
        this.followed = followed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
