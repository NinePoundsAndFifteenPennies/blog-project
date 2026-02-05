package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 管理员评论操作请求DTO
 */
public class AdminCommentActionRequest {

    /**
     * 操作类型: APPROVE, DELETE
     */
    @NotEmpty(message = "操作类型不能为空")
    private String action;

    /**
     * 评论ID列表（支持批量操作）
     */
    @NotNull(message = "评论ID列表不能为空")
    private List<Long> commentIds;

    /**
     * 表单标题（用于删除时的通知）
     */
    @Size(max = 100, message = "表单标题不能超过100个字符")
    private String formTitle;

    /**
     * 理由（删除时必填）
     */
    private String reason;

    /**
     * 扩展字段（JSON格式，可选）
     * 格式: [{"fieldName": "...", "fieldValue": "..."}, ...]
     */
    private String extraFields;

    public AdminCommentActionRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Long> getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(List<Long> commentIds) {
        this.commentIds = commentIds;
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
