package com.lost.blog.dto;

import java.util.List;

/**
 * 仪表盘响应DTO
 * 包含核心统计数据、趋势数据和热门内容
 */
public class DashboardResponse {

    // ======================= 核心统计数据 =======================
    private long totalUsers;
    private long todayNewUsers;
    private long totalPosts;
    private long todayNewPosts;
    private long totalComments;
    private long todayNewComments;
    private long totalViews;
    private long todayViews;

    // ======================= 趋势数据（最近30天） =======================
    private List<TrendItem> userTrend;
    private List<TrendItem> postTrend;
    private List<TrendItem> commentTrend;
    private List<TrendItem> viewTrend;

    // ======================= 热门文章 TOP10 =======================
    private List<HotPostItem> hotPosts;

    // ======================= 文章状态分布 =======================
    private long publishedPosts;
    private long draftPosts;
    private long pendingPosts;
    private long rejectedPosts;

    // ======================= 管理员信息 =======================
    private String admin;
    private String message;

    /**
     * 趋势数据项（日期 + 数量）
     */
    public static class TrendItem {
        private String date;
        private long count;

        public TrendItem() {}

        public TrendItem(String date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    /**
     * 热门文章项
     */
    public static class HotPostItem {
        private Long id;
        private String title;
        private String author;
        private long viewCount;
        private long likeCount;
        private long commentCount;

        public HotPostItem() {}

        public HotPostItem(Long id, String title, String author, long viewCount, long likeCount, long commentCount) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public long getViewCount() { return viewCount; }
        public void setViewCount(long viewCount) { this.viewCount = viewCount; }
        public long getLikeCount() { return likeCount; }
        public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
        public long getCommentCount() { return commentCount; }
        public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
    }

    // ======================= Getters and Setters =======================
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTodayNewUsers() { return todayNewUsers; }
    public void setTodayNewUsers(long todayNewUsers) { this.todayNewUsers = todayNewUsers; }
    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
    public long getTodayNewPosts() { return todayNewPosts; }
    public void setTodayNewPosts(long todayNewPosts) { this.todayNewPosts = todayNewPosts; }
    public long getTotalComments() { return totalComments; }
    public void setTotalComments(long totalComments) { this.totalComments = totalComments; }
    public long getTodayNewComments() { return todayNewComments; }
    public void setTodayNewComments(long todayNewComments) { this.todayNewComments = todayNewComments; }
    public long getTotalViews() { return totalViews; }
    public void setTotalViews(long totalViews) { this.totalViews = totalViews; }
    public long getTodayViews() { return todayViews; }
    public void setTodayViews(long todayViews) { this.todayViews = todayViews; }
    public List<TrendItem> getUserTrend() { return userTrend; }
    public void setUserTrend(List<TrendItem> userTrend) { this.userTrend = userTrend; }
    public List<TrendItem> getPostTrend() { return postTrend; }
    public void setPostTrend(List<TrendItem> postTrend) { this.postTrend = postTrend; }
    public List<TrendItem> getCommentTrend() { return commentTrend; }
    public void setCommentTrend(List<TrendItem> commentTrend) { this.commentTrend = commentTrend; }
    public List<TrendItem> getViewTrend() { return viewTrend; }
    public void setViewTrend(List<TrendItem> viewTrend) { this.viewTrend = viewTrend; }
    public List<HotPostItem> getHotPosts() { return hotPosts; }
    public void setHotPosts(List<HotPostItem> hotPosts) { this.hotPosts = hotPosts; }
    public long getPublishedPosts() { return publishedPosts; }
    public void setPublishedPosts(long publishedPosts) { this.publishedPosts = publishedPosts; }
    public long getDraftPosts() { return draftPosts; }
    public void setDraftPosts(long draftPosts) { this.draftPosts = draftPosts; }
    public long getPendingPosts() { return pendingPosts; }
    public void setPendingPosts(long pendingPosts) { this.pendingPosts = pendingPosts; }
    public long getRejectedPosts() { return rejectedPosts; }
    public void setRejectedPosts(long rejectedPosts) { this.rejectedPosts = rejectedPosts; }
    public String getAdmin() { return admin; }
    public void setAdmin(String admin) { this.admin = admin; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
