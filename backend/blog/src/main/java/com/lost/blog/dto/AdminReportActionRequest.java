package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 管理员处理举报请求DTO
 */
public class AdminReportActionRequest {

    /**
     * 操作类型: APPROVE, REJECT
     */
    @NotEmpty(message = "操作类型不能为空")
    private String action;

    /**
     * 举报ID
     */
    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    /**
     * 表单标题
     */
    @NotEmpty(message = "表单标题不能为空")
    @Size(max = 100, message = "表单标题不能超过100个字符")
    private String formTitle;

    /**
     * 处理理由（必填）
     */
    @NotEmpty(message = "处理理由不能为空")
    private String reason;

    /**
     * 扩展字段（JSON格式，可选）
     */
    private String extraFields;

    public AdminReportActionRequest() {}

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getExtraFields() { return extraFields; }
    public void setExtraFields(String extraFields) { this.extraFields = extraFields; }
}
