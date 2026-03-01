package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 用户举报请求DTO
 */
public class ReportRequest {

    /**
     * 举报目标类型: POST, COMMENT
     */
    @NotEmpty(message = "举报类型不能为空")
    private String targetType;

    /**
     * 举报目标ID
     */
    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;

    /**
     * 举报原因
     */
    @NotEmpty(message = "举报原因不能为空")
    @Size(max = 500, message = "举报原因不能超过500个字符")
    private String reason;

    public ReportRequest() {}

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
