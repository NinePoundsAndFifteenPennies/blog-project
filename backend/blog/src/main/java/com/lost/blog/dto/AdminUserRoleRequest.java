package com.lost.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 用户角色更新请求DTO
 */
public class AdminUserRoleRequest {
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "角色必须为USER或ADMIN")
    private String role;

    public AdminUserRoleRequest() {
    }

    public AdminUserRoleRequest(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
