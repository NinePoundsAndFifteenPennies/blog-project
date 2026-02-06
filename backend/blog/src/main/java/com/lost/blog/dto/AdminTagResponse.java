package com.lost.blog.dto;

import com.lost.blog.model.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员标签响应DTO
 * 展示标签的完整信息
 */
public class AdminTagResponse {

    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Integer sortOrder;

    // 创建者信息
    private Long createdById;
    private String createdByUsername;
    private String createdByNickname;

    // 统计信息
    private Long postCount;

    // 关联的文章列表（详情接口返回）
    private List<PostInfo> posts;

    // 时间信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminTagResponse() {
    }

    /**
     * 从Tag实体创建响应DTO
     */
    public static AdminTagResponse fromEntity(Tag tag) {
        return fromEntity(tag, null);
    }

    /**
     * 从Tag实体创建响应DTO（含文章数量统计）
     */
    public static AdminTagResponse fromEntity(Tag tag, Long postCount) {
        AdminTagResponse response = new AdminTagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setDescription(tag.getDescription());
        response.setColor(tag.getColor());
        response.setIcon(tag.getIcon());
        response.setSortOrder(tag.getSortOrder());

        // 创建者信息
        if (tag.getCreatedBy() != null) {
            response.setCreatedById(tag.getCreatedBy().getId());
            response.setCreatedByUsername(tag.getCreatedBy().getUsername());
            response.setCreatedByNickname(tag.getCreatedBy().getNickname());
        }

        // 文章数量
        if (postCount != null) {
            response.setPostCount(postCount);
        } else if (tag.getPosts() != null) {
            response.setPostCount((long) tag.getPosts().size());
        } else {
            response.setPostCount(0L);
        }

        // 关联文章列表（仅在posts已加载时填充）
        if (tag.getPosts() != null && !tag.getPosts().isEmpty()) {
            List<PostInfo> postInfos = new ArrayList<>();
            for (var post : tag.getPosts()) {
                postInfos.add(new PostInfo(post.getId(), post.getTitle()));
            }
            response.setPosts(postInfos);
        }

        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());

        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getCreatedByNickname() {
        return createdByNickname;
    }

    public void setCreatedByNickname(String createdByNickname) {
        this.createdByNickname = createdByNickname;
    }

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
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

    public List<PostInfo> getPosts() {
        return posts;
    }

    public void setPosts(List<PostInfo> posts) {
        this.posts = posts;
    }

    /**
     * 文章简要信息（用于标签-文章关联展示）
     */
    public static class PostInfo {
        private Long postId;
        private String postTitle;

        public PostInfo() {
        }

        public PostInfo(Long postId, String postTitle) {
            this.postId = postId;
            this.postTitle = postTitle;
        }

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }

        public String getPostTitle() {
            return postTitle;
        }

        public void setPostTitle(String postTitle) {
            this.postTitle = postTitle;
        }
    }
}
