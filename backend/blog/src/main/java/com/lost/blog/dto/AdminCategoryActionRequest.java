package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 管理员分类操作请求DTO
 */
public class AdminCategoryActionRequest {

    /**
     * 操作类型: DELETE
     */
    @NotEmpty(message = "操作类型不能为空")
    private String action;

    /**
     * 分类ID列表（支持批量操作）
     */
    @NotNull(message = "分类ID列表不能为空")
    private List<Long> categoryIds;

    /**
     * 理由（删除时必填）
     */
    private String reason;

    /**
     * 扩展字段（JSON格式，可选）
     * 格式: [{"fieldName": "...", "fieldValue": "..."}, ...]
     */
    private String extraFields;

    public AdminCategoryActionRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
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
