package com.lost.blog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 用户状态更新请求DTO
 */
public class AdminUserStatusRequest {
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    /**
     * 表单标题（可选）
     */
    @Size(max = 100, message = "表单标题不能超过100个字符")
    private String formTitle;

    /**
     * 理由（必填）
     */
    private String reason;

    /**
     * 扩展字段（JSON格式，可选）
     * 格式: [{"fieldName": "...", "fieldValue": "..."}, ...]
     */
    private String extraFields;

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

    public String getFormTitle() {
        return formTitle;
    }

    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(String extraFields) {
        this.extraFields = extraFields;
    }
}
