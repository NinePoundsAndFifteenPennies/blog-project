package com.lost.blog.dto;

import com.lost.blog.model.ContentType;
import com.lost.blog.model.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员文章响应DTO
 * 包含管理需要的额外信息
 */
public class AdminPostResponse {
    private Long id;
    private String title;
    private String content;
    private ContentType contentType;
    private PostStatus status;
    private Boolean draft;
    
    // 作者信息
    private Long authorId;
    private String authorUsername;
    private String authorNickname;
    private String authorAvatarUrl;
    private Boolean authorEnabled;
    
    // 时间信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    
    // 统计信息
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    
    // 分类和标签
    private CategoryResponse category;
    private List<TagResponse> tags;
    
    // 封面图片
    private String coverImageUrl;
    
    // 审核相关
    private String previousTitle;      // 修改前标题（用于PENDING_REVISION状态）
    private String previousContent;    // 修改前内容（用于PENDING_REVISION状态）
    private Long rejectionFormId;      // 关联的拒绝表单ID
    private AdminFormResponse rejectionForm;  // 拒绝表单详情

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

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public Boolean getAuthorEnabled() {
        return authorEnabled;
    }

    public void setAuthorEnabled(Boolean authorEnabled) {
        this.authorEnabled = authorEnabled;
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

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getPreviousTitle() {
        return previousTitle;
    }

    public void setPreviousTitle(String previousTitle) {
        this.previousTitle = previousTitle;
    }

    public String getPreviousContent() {
        return previousContent;
    }

    public void setPreviousContent(String previousContent) {
        this.previousContent = previousContent;
    }

    public Long getRejectionFormId() {
        return rejectionFormId;
    }

    public void setRejectionFormId(Long rejectionFormId) {
        this.rejectionFormId = rejectionFormId;
    }

    public AdminFormResponse getRejectionForm() {
        return rejectionForm;
    }

    public void setRejectionForm(AdminFormResponse rejectionForm) {
        this.rejectionForm = rejectionForm;
    }
}
