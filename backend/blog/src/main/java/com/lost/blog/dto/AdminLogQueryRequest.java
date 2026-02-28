package com.lost.blog.dto;

/**
 * 管理操作日志查询请求DTO
 */
public class AdminLogQueryRequest {
    private String operationType;
    private String adminUsername;
    private String title;
    private String startDate;
    private String endDate;

    // ===== Getters and Setters =====

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
