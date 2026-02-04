package com.lost.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "user_id"}) // 修改约束：同一用户的标题不可以重复，但不同用户间可以有相同标题
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题长度不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String title;

    @NotEmpty(message = "文章内容不能为空")
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    // ===== 新增草稿字段 =====
    @Column(name = "is_draft", nullable = false)
    private Boolean draft = false;  // 默认为非草稿（已发布）

    // ===== 新增文章状态字段 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status = PostStatus.DRAFT;  // 默认为草稿状态

    // ===== 用于存储已发布文章修改前的内容（待审核期间前台展示旧版本）=====
    @Lob
    @Column(name = "previous_content", columnDefinition = "TEXT")
    private String previousContent;

    // ===== 用于存储已发布文章修改前的标题 =====
    @Column(name = "previous_title", length = 100)
    private String previousTitle;

    // ===== 关联的拒绝/删除表单ID =====
    @Column(name = "rejection_form_id")
    private Long rejectionFormId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;  // 首次发布时间

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多对多关系：一篇文章可以有多个标签
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // 多对一关系：一篇文章只能属于一个分类
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 浏览量字段，默认值为0
    @Column(nullable = false)
    private Long viewCount = 0L;

    // 封面图片URL字段（可选）
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // 如果是首次发布（非草稿），设置发布时间
        if (!draft && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // 如果从草稿变为发布状态，设置发布时间
        if (!draft && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    // ===== Getters and Setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public String getPreviousContent() {
        return previousContent;
    }

    public void setPreviousContent(String previousContent) {
        this.previousContent = previousContent;
    }

    public String getPreviousTitle() {
        return previousTitle;
    }

    public void setPreviousTitle(String previousTitle) {
        this.previousTitle = previousTitle;
    }

    public Long getRejectionFormId() {
        return rejectionFormId;
    }

    public void setRejectionFormId(Long rejectionFormId) {
        this.rejectionFormId = rejectionFormId;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
}

