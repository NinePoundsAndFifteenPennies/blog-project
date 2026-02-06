package com.lost.blog.dto;

/**
 * 管理员标签查询请求DTO
 */
public class AdminTagQueryRequest {

    /**
     * 标签名称搜索（模糊匹配）
     */
    private String name;

    /**
     * 创建者用户名搜索（模糊匹配）
     */
    private String createdBy;

    /**
     * 创建开始日期（yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 创建结束日期（yyyy-MM-dd）
     */
    private String endDate;

    public AdminTagQueryRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
