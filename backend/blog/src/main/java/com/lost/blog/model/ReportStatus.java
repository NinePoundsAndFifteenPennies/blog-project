package com.lost.blog.model;

/**
 * 举报状态枚举
 */
public enum ReportStatus {
    /**
     * 待处理
     */
    PENDING,

    /**
     * 举报通过（已确认违规）
     */
    APPROVED,

    /**
     * 举报驳回（未确认违规）
     */
    REJECTED
}
