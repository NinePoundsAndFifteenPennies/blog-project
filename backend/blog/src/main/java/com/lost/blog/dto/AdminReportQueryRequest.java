package com.lost.blog.dto;

/**
 * 管理员举报查询请求DTO
 */
public class AdminReportQueryRequest {

    private String status;
    private String targetType;
    private String reporterUsername;
    private String reportedUsername;
    private String startDate;
    private String endDate;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getReportedUsername() { return reportedUsername; }
    public void setReportedUsername(String reportedUsername) { this.reportedUsername = reportedUsername; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
