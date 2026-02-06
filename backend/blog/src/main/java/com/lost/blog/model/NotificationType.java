package com.lost.blog.model;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    /**
     * 文章被点赞
     */
    POST_LIKED,
    
    /**
     * 文章被评论
     */
    POST_COMMENTED,
    
    /**
     * 被关注
     */
    FOLLOWED,
    
    /**
     * 评论被点赞
     */
    COMMENT_LIKED,
    
    /**
     * 评论被回复
     */
    COMMENT_REPLIED,
    
    /**
     * 收到私信
     */
    MESSAGE_RECEIVED,
    
    /**
     * 文章审核通过
     */
    POST_APPROVED,
    
    /**
     * 文章审核拒绝
     */
    POST_REJECTED,
    
    /**
     * 文章被删除（违规）
     */
    POST_DELETED,

    /**
     * 评论被删除（违规）
     */
    COMMENT_DELETED,

    /**
     * 标签被移除关联（管理员软删除操作）
     */
    TAG_REMOVED,

    /**
     * 标签被删除（管理员硬删除操作）
     */
    TAG_DELETED,

    /**
     * 分类被删除（管理员删除操作）
     */
    CATEGORY_DELETED
}
