package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 审核/删除文章请求DTO
 */
public class AdminPostActionRequest {
    
    /**
     * 操作类型: APPROVE, REJECT, DELETE
     */
    @NotEmpty(message = "操作类型不能为空")
    private String action;
    
    /**
     * 文章ID列表（支持批量操作）
     */
    @NotNull(message = "文章ID列表不能为空")
    private List<Long> postIds;
    
    /**
     * 表单标题（用于拒绝/删除时的提示字段显示）
     */
    @Size(max = 100, message = "表单标题不能超过100个字符")
    private String formTitle;
    
    /**
     * 理由（拒绝/删除时必填）
     */
    private String reason;
    
    /**
     * 扩展字段（JSON格式，可选）
     * 格式: [{"fieldName": "...", "fieldValue": "..."}, ...]
     */
    private String extraFields;

    public AdminPostActionRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Long> getPostIds() {
        return postIds;
    }

    public void setPostIds(List<Long> postIds) {
        this.postIds = postIds;
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
