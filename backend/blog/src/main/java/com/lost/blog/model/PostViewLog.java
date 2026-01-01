package com.lost.blog.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_view_logs")
public class PostViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联文章，方便未来查询"某篇文章的所有访问记录"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 记录IP，用于防刷和地域分析
    @Column(nullable = false)
    private String ip;

    // 记录设备信息（浏览器/手机），用于设备分析
    private String userAgent;

    // 访问时间
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    // 构造函数
    public PostViewLog() {}

    public PostViewLog(Post post, String ip, String userAgent) {
        this.post = post;
        this.ip = ip;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
