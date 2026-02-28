package com.lost.blog.service;

import com.lost.blog.dto.AdminLogQueryRequest;
import com.lost.blog.dto.AdminLogResponse;
import com.lost.blog.model.AdminLogType;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理操作日志服务接口
 */
public interface AdminLogService {

    /**
     * 记录管理操作日志
     */
    void log(AdminLogType operationType, String title, String description, User admin,
             Long postId, String postTitle,
             Long commentId, String commentContentPreview,
             Long tagId, String tagName,
             Long categoryId, String categoryName,
             Long targetUserId, String targetUsername,
             Long formId);

    /**
     * 分页查询日志列表（支持多条件搜索）
     */
    Page<AdminLogResponse> searchLogs(AdminLogQueryRequest query, Pageable pageable);

    /**
     * 获取日志详情
     */
    AdminLogResponse getLogDetail(Long logId);
}
