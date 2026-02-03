package com.lost.blog.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 用户状态更新请求DTO
 */
public class AdminUserStatusRequest {
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    public AdminUserStatusRequest() {
    }

    public AdminUserStatusRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
