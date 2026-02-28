package com.lost.blog.service;

import com.lost.blog.dto.AdminLogQueryRequest;
import com.lost.blog.dto.AdminLogResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.AdminLog;
import com.lost.blog.model.AdminLogType;
import com.lost.blog.model.User;
import com.lost.blog.repository.AdminLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 管理操作日志服务实现类
 */
@Service
public class AdminLogServiceImpl implements AdminLogService {

    private static final Logger logger = LoggerFactory.getLogger(AdminLogServiceImpl.class);

    private final AdminLogRepository adminLogRepository;

    @Autowired
    public AdminLogServiceImpl(AdminLogRepository adminLogRepository) {
        this.adminLogRepository = adminLogRepository;
    }

    @Override
    @Transactional
    public void log(AdminLogType operationType, String title, String description, String extraFields, User admin,
                    Long postId, String postTitle,
                    Long commentId, String commentContentPreview,
                    Long tagId, String tagName,
                    Long categoryId, String categoryName,
                    Long targetUserId, String targetUsername,
                    Long formId) {
        AdminLog log = new AdminLog();
        log.setOperationType(operationType);
        log.setTitle(title);
        log.setDescription(description);
        log.setExtraFields(extraFields);
        log.setAdmin(admin);
        log.setPostId(postId);
        log.setPostTitle(postTitle);
        log.setCommentId(commentId);
        log.setCommentContentPreview(commentContentPreview);
        log.setTagId(tagId);
        log.setTagName(tagName);
        log.setCategoryId(categoryId);
        log.setCategoryName(categoryName);
        log.setTargetUserId(targetUserId);
        log.setTargetUsername(targetUsername);
        log.setFormId(formId);
        adminLogRepository.save(log);
        logger.debug("记录管理操作日志: {} - {}", operationType, title);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminLogResponse> searchLogs(AdminLogQueryRequest query, Pageable pageable) {
        AdminLogType operationType = null;
        if (query.getOperationType() != null && !query.getOperationType().isEmpty()) {
            try {
                operationType = AdminLogType.valueOf(query.getOperationType());
            } catch (IllegalArgumentException e) {
                logger.warn("无效的操作类型: {}", query.getOperationType());
            }
        }

        String adminUsername = (query.getAdminUsername() != null && !query.getAdminUsername().isEmpty())
                ? query.getAdminUsername() : null;
        String title = (query.getTitle() != null && !query.getTitle().isEmpty())
                ? query.getTitle() : null;

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            try {
                LocalDate startDate = LocalDate.parse(query.getStartDate(), DateTimeFormatter.ISO_DATE);
                startTime = startDate.atStartOfDay();
            } catch (Exception e) {
                logger.warn("无效的开始日期格式: {}", query.getStartDate());
            }
        }

        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            try {
                LocalDate endDate = LocalDate.parse(query.getEndDate(), DateTimeFormatter.ISO_DATE);
                endTime = endDate.atTime(LocalTime.MAX);
            } catch (Exception e) {
                logger.warn("无效的结束日期格式: {}", query.getEndDate());
            }
        }

        Page<AdminLog> logs = adminLogRepository.searchLogs(
                operationType, adminUsername, title, startTime, endTime, pageable);

        return logs.map(AdminLogResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminLogResponse getLogDetail(Long logId) {
        AdminLog log = adminLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("日志不存在: " + logId));
        return AdminLogResponse.fromEntity(log);
    }
}
