package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员评论服务接口
 * 提供评论列表查询、审核、删除等功能
 */
public interface AdminCommentService {
    
    /**
     * 分页查询评论列表（支持多条件搜索）
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<AdminCommentResponse> searchComments(AdminCommentQueryRequest query, Pageable pageable);
    
    /**
     * 获取评论详细信息
     * @param commentId 评论ID
     * @return 评论详细信息
     */
    AdminCommentResponse getCommentDetail(Long commentId);
    
    /**
     * 批量审核通过评论
     * @param commentIds 评论ID列表
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse approveComments(java.util.List<Long> commentIds, User admin);
    
    /**
     * 批量删除违规评论
     * @param commentIds 评论ID列表
     * @param formTitle 表单标题
     * @param reason 删除理由
     * @param extraFields 扩展字段（JSON格式）
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse deleteComments(java.util.List<Long> commentIds, String formTitle, 
                                            String reason, String extraFields, User admin);
}
