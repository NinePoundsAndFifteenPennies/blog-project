package com.lost.blog.dto;

/**
 * 管理员文章查询请求DTO
 * 用于多条件搜索文章
 */
public class AdminPostQueryRequest {
    private String title;           // 标题搜索（模糊匹配）
    private String author;          // 作者用户名/昵称搜索（模糊匹配）
    private String status;          // 状态过滤：DRAFT/PENDING_REVIEW/PUBLISHED/REJECTED/PENDING_REVISION
    private String categoryId;      // 分类ID过滤
    private String tag;             // 标签名称过滤
    private String startDate;       // 创建开始时间 (yyyy-MM-dd)
    private String endDate;         // 创建结束时间 (yyyy-MM-dd)

    public AdminPostQueryRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
