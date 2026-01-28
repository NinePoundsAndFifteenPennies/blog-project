package com.lost.blog.dto;

/**
 * 社区统计响应DTO
 * 返回社区统计信息给客户端
 */
public class StatisticsResponse {

    private Long totalUsers;      // 用户总数
    private Long totalPosts;      // 文章总数
    private Long onlineUsers;     // 当前在线用户数
    private Long todayVisits;     // 今日访问数

    // 构造函数
    public StatisticsResponse() {
    }

    public StatisticsResponse(Long totalUsers, Long totalPosts, Long onlineUsers, Long todayVisits) {
        this.totalUsers = totalUsers;
        this.totalPosts = totalPosts;
        this.onlineUsers = onlineUsers;
        this.todayVisits = todayVisits;
    }

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Long getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(Long onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public Long getTodayVisits() {
        return todayVisits;
    }

    public void setTodayVisits(Long todayVisits) {
        this.todayVisits = todayVisits;
    }
}
