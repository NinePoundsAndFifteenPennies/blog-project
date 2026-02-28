package com.lost.blog.model;

/**
 * 管理操作日志类型枚举
 */
public enum AdminLogType {
    /**
     * 文章审核通过
     */
    POST_APPROVE,

    /**
     * 文章审核拒绝
     */
    POST_REJECT,

    /**
     * 文章删除
     */
    POST_DELETE,

    /**
     * 评论审核通过
     */
    COMMENT_APPROVE,

    /**
     * 评论删除
     */
    COMMENT_DELETE,

    /**
     * 标签创建
     */
    TAG_CREATE,

    /**
     * 标签更新
     */
    TAG_UPDATE,

    /**
     * 标签软删除（移除关联）
     */
    TAG_SOFT_DELETE,

    /**
     * 标签硬删除
     */
    TAG_HARD_DELETE,

    /**
     * 分类创建
     */
    CATEGORY_CREATE,

    /**
     * 分类更新
     */
    CATEGORY_UPDATE,

    /**
     * 分类删除
     */
    CATEGORY_DELETE,

    /**
     * 分类添加文章
     */
    CATEGORY_ASSIGN_POST,

    /**
     * 分类移除文章
     */
    CATEGORY_REMOVE_POST,

    /**
     * 用户状态变更
     */
    USER_STATUS_CHANGE
}
