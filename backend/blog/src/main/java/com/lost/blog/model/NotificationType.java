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
    MESSAGE_RECEIVED
}
