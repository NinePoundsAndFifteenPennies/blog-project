package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员文章服务接口
 * 提供文章列表查询、审核、删除等功能
 */
public interface AdminPostService {
    
    /**
     * 分页查询文章列表（支持多条件搜索）
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<AdminPostResponse> searchPosts(AdminPostQueryRequest query, Pageable pageable);
    
    /**
     * 获取文章详细信息
     * @param postId 文章ID
     * @return 文章详细信息
     */
    AdminPostResponse getPostDetail(Long postId);
    
    /**
     * 批量审核通过文章
     * @param postIds 文章ID列表
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse approvePosts(java.util.List<Long> postIds, User admin);
    
    /**
     * 批量审核拒绝文章
     * @param postIds 文章ID列表
     * @param formTitle 表单标题
     * @param reason 拒绝理由
     * @param extraFields 扩展字段（JSON格式）
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse rejectPosts(java.util.List<Long> postIds, String formTitle, 
                                         String reason, String extraFields, User admin);
    
    /**
     * 批量删除违规文章
     * @param postIds 文章ID列表
     * @param formTitle 表单标题
     * @param reason 删除理由
     * @param extraFields 扩展字段（JSON格式）
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse deletePosts(java.util.List<Long> postIds, String formTitle, 
                                         String reason, String extraFields, User admin);
    
    /**
     * 获取表单详情
     * @param formId 表单ID
     * @return 表单详情
     */
    AdminFormResponse getFormDetail(Long formId);
    
    /**
     * 获取文章关联的表单列表
     * @param postId 文章ID
     * @return 表单列表
     */
    java.util.List<AdminFormResponse> getPostForms(Long postId);
}
