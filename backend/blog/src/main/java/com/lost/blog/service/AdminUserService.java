package com.lost.blog.service;

import com.lost.blog.dto.AdminUserQueryRequest;
import com.lost.blog.dto.AdminUserResponse;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员用户服务接口
 * 提供用户列表查询、状态管理、角色分配等功能
 */
public interface AdminUserService {
    
    /**
     * 分页查询用户列表（支持多条件搜索）
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<AdminUserResponse> searchUsers(AdminUserQueryRequest query, Pageable pageable);
    
    /**
     * 获取用户详细信息（包含发文数、评论数统计）
     * @param userId 用户ID
     * @return 用户详细信息
     */
    AdminUserResponse getUserDetail(Long userId);
    
    /**
     * 更新用户启用状态（启用/禁用）
     * @param userId 用户ID
     * @param enabled 是否启用
     * @param formTitle 表单标题（可选）
     * @param reason 理由（必填）
     * @param extraFields 扩展字段JSON（可选）
     * @param admin 执行操作的管理员
     * @return 更新后的用户信息
     */
    AdminUserResponse updateUserStatus(Long userId, Boolean enabled, String formTitle, 
                                        String reason, String extraFields, User admin);
    
    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户实体
     */
    User findById(Long userId);
}
