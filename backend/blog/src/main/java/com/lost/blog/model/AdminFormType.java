package com.lost.blog.model;

/**
 * 管理表单类型枚举
 */
public enum AdminFormType {
    /**
     * 审核拒绝表单
     */
    REJECTION,
    
    /**
     * 删除文章表单
     */
    DELETION,

    /**
     * 删除评论表单
     */
    COMMENT_DELETION,

    /**
     * 标签软删除表单（移除标签与文章的关联）
     */
    TAG_SOFT_DELETION,

    /**
     * 标签硬删除表单（删除标签本身）
     */
    TAG_HARD_DELETION
}
