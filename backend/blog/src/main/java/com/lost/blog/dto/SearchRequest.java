package com.lost.blog.dto;

import jakarta.validation.constraints.Size;

/**
 * 文章搜索请求DTO
 * 支持按作者、标题、内容、标签等多维度搜索
 */
public class SearchRequest {

    @Size(max = 200, message = "搜索关键词长度不能超过200个字符")
    private String keyword;  // 通用关键词，可匹配标题和内容

    @Size(max = 50, message = "作者用户名长度不能超过50个字符")
    private String author;  // 按作者用户名搜索

    @Size(max = 100, message = "标题搜索长度不能超过100个字符")
    private String title;  // 按标题搜索

    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String tag;  // 按标签搜索

    // 排序方式：time（按时间）或 hotness（按热度），默认time
    private String sortBy = "time";

    // 排序顺序：asc（升序）或 desc（降序），默认desc
    private String order = "desc";

    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * 检查是否有有效的搜索条件
     */
    public boolean hasSearchCriteria() {
        return (keyword != null && !keyword.trim().isEmpty()) ||
               (author != null && !author.trim().isEmpty()) ||
               (title != null && !title.trim().isEmpty()) ||
               (tag != null && !tag.trim().isEmpty());
    }
}
