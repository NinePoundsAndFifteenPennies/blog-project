package com.lost.blog.dto;

/**
 * 管理员用户查询请求DTO
 * 用于多条件搜索用户（用户名、邮箱、注册时间、角色、状态）
 */
public class AdminUserQueryRequest {
    private String username;
    private String email;
    private String role;        // "USER" or "ADMIN"
    private Boolean enabled;    // true=启用, false=禁用
    private String startDate;   // 注册开始时间 (yyyy-MM-dd)
    private String endDate;     // 注册结束时间 (yyyy-MM-dd)

    public AdminUserQueryRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
