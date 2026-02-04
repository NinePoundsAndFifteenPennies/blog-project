package com.lost.blog.model;

/**
 * 文章状态枚举
 * 用于文章审核流程管理
 */
public enum PostStatus {
    /**
     * 草稿 - 作者创建但未提交发布的文章
     */
    DRAFT,
    
    /**
     * 待审核 - 作者提交发布申请，等待管理员审核
     */
    PENDING_REVIEW,
    
    /**
     * 已发布 - 审核通过并对外展示的文章
     */
    PUBLISHED,
    
    /**
     * 已拒绝 - 审核不通过的文章
     */
    REJECTED,
    
    /**
     * 发布后修改待审核 - 已发布文章被修改后等待重新审核
     * 审核通过前，前台展示旧版本
     */
    PENDING_REVISION
}
