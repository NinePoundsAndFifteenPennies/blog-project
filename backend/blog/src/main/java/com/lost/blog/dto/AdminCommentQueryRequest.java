package com.lost.blog.dto;

/**
 * 管理员评论查询请求DTO
 */
public class AdminCommentQueryRequest {

    /**
     * 评论内容搜索（模糊匹配）
     */
    private String content;

    /**
     * 作者用户名/昵称搜索（模糊匹配）
     */
    private String author;

    /**
     * 文章标题搜索（模糊匹配）
     */
    private String postTitle;

    /**
     * 评论状态过滤：PENDING/APPROVED
     */
    private String status;

    /**
     * 创建开始日期（yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 创建结束日期（yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 是否包含子评论（默认true）
     */
    private Boolean includeReplies = true;

    public AdminCommentQueryRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getIncludeReplies() {
        return includeReplies;
    }

    public void setIncludeReplies(Boolean includeReplies) {
        this.includeReplies = includeReplies;
    }
}
