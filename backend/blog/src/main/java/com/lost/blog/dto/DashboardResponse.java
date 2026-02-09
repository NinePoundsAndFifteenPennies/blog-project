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

    // ======================= 标签/分类热力图数据 =======================
    private List<TagCategoryItem> tagStats;
    private List<TagCategoryItem> categoryStats;

    // ======================= 系统概览 =======================
    private long totalTags;
    private long totalCategories;
    private long enabledUsers;
    private long disabledUsers;

    // ======================= 最近活动 =======================
    private List<RecentActivityItem> recentActivities;

    // ======================= 内容质量雷达图 =======================
    private ContentRadarData contentRadar;

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

    /**
     * 标签/分类统计项（名称 + 文章数 + 颜色）
     */
    public static class TagCategoryItem {
        private Long id;
        private String name;
        private long postCount;
        private String color;

        public TagCategoryItem() {}

        public TagCategoryItem(Long id, String name, long postCount, String color) {
            this.id = id;
            this.name = name;
            this.postCount = postCount;
            this.color = color;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getPostCount() { return postCount; }
        public void setPostCount(long postCount) { this.postCount = postCount; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    /**
     * 最近活动项
     */
    public static class RecentActivityItem {
        private String type;
        private String description;
        private String time;
        private String icon;

        public RecentActivityItem() {}

        public RecentActivityItem(String type, String description, String time, String icon) {
            this.type = type;
            this.description = description;
            this.time = time;
            this.icon = icon;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    /**
     * 内容质量雷达图数据
     */
    public static class ContentRadarData {
        private double avgViewsPerPost;
        private double avgCommentsPerPost;
        private double avgLikesPerPost;
        private double publishRate;
        private double userEngagement;
        private double contentFreshness;

        public ContentRadarData() {}

        public double getAvgViewsPerPost() { return avgViewsPerPost; }
        public void setAvgViewsPerPost(double avgViewsPerPost) { this.avgViewsPerPost = avgViewsPerPost; }
        public double getAvgCommentsPerPost() { return avgCommentsPerPost; }
        public void setAvgCommentsPerPost(double avgCommentsPerPost) { this.avgCommentsPerPost = avgCommentsPerPost; }
        public double getAvgLikesPerPost() { return avgLikesPerPost; }
        public void setAvgLikesPerPost(double avgLikesPerPost) { this.avgLikesPerPost = avgLikesPerPost; }
        public double getPublishRate() { return publishRate; }
        public void setPublishRate(double publishRate) { this.publishRate = publishRate; }
        public double getUserEngagement() { return userEngagement; }
        public void setUserEngagement(double userEngagement) { this.userEngagement = userEngagement; }
        public double getContentFreshness() { return contentFreshness; }
        public void setContentFreshness(double contentFreshness) { this.contentFreshness = contentFreshness; }
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
    public List<TagCategoryItem> getTagStats() { return tagStats; }
    public void setTagStats(List<TagCategoryItem> tagStats) { this.tagStats = tagStats; }
    public List<TagCategoryItem> getCategoryStats() { return categoryStats; }
    public void setCategoryStats(List<TagCategoryItem> categoryStats) { this.categoryStats = categoryStats; }
    public long getTotalTags() { return totalTags; }
    public void setTotalTags(long totalTags) { this.totalTags = totalTags; }
    public long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
    public long getEnabledUsers() { return enabledUsers; }
    public void setEnabledUsers(long enabledUsers) { this.enabledUsers = enabledUsers; }
    public long getDisabledUsers() { return disabledUsers; }
    public void setDisabledUsers(long disabledUsers) { this.disabledUsers = disabledUsers; }
    public List<RecentActivityItem> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivityItem> recentActivities) { this.recentActivities = recentActivities; }
    public ContentRadarData getContentRadar() { return contentRadar; }
    public void setContentRadar(ContentRadarData contentRadar) { this.contentRadar = contentRadar; }
    public String getAdmin() { return admin; }
    public void setAdmin(String admin) { this.admin = admin; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
