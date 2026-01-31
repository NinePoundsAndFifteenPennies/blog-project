package com.lost.blog.model;

/**
 * 关注信息可见性类型枚举
 * 用于指定要检查或设置哪种类型的可见性
 */
public enum FollowInfoType {
    /**
     * 关注列表（该用户关注了谁）
     */
    FOLLOWING,
    
    /**
     * 粉丝列表（谁关注了该用户）
     */
    FOLLOWERS,
    
    /**
     * 朋友列表（互相关注的用户）
     */
    FRIENDS,
    
    /**
     * 统计数据（关注数、粉丝数、朋友数）
     */
    STATS
}
