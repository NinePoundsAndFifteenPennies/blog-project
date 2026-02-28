package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.*;
import com.lost.blog.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员评论服务实现类
 */
@Service
public class AdminCommentServiceImpl implements AdminCommentService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final AdminFormRepository adminFormRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final LikeRepository likeRepository;
    private final AdminLogService adminLogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdminCommentServiceImpl(CommentRepository commentRepository,
                                   AdminFormRepository adminFormRepository,
                                   NotificationService notificationService,
                                   NotificationRepository notificationRepository,
                                   LikeRepository likeRepository,
                                   AdminLogService adminLogService) {
        this.commentRepository = commentRepository;
        this.adminFormRepository = adminFormRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.likeRepository = likeRepository;
        this.adminLogService = adminLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCommentResponse> searchComments(AdminCommentQueryRequest query, Pageable pageable) {
        // 处理日期转换
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            try {
                LocalDate startDate = LocalDate.parse(query.getStartDate(), DateTimeFormatter.ISO_DATE);
                startDateTime = startDate.atStartOfDay();
            } catch (Exception e) {
                logger.warn("无效的开始日期格式: {}", query.getStartDate());
            }
        }

        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            try {
                LocalDate endDate = LocalDate.parse(query.getEndDate(), DateTimeFormatter.ISO_DATE);
                endDateTime = endDate.atTime(LocalTime.MAX);
            } catch (Exception e) {
                logger.warn("无效的结束日期格式: {}", query.getEndDate());
            }
        }

        // 处理状态
        CommentStatus status = null;
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            try {
                status = CommentStatus.valueOf(query.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("无效的评论状态: {}", query.getStatus());
            }
        }

        boolean includeReplies = query.getIncludeReplies() != null ? query.getIncludeReplies() : true;

        Page<Comment> comments = commentRepository.adminSearchComments(
                query.getContent(),
                query.getAuthor(),
                query.getPostTitle(),
                status,
                startDateTime,
                endDateTime,
                includeReplies,
                pageable
        );

        return comments.map(comment -> {
            long likeCount = likeRepository.countByComment(comment);
            long replyCount = commentRepository.countByParent(comment);
            return AdminCommentResponse.fromEntity(comment, likeCount, replyCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCommentResponse getCommentDetail(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论: " + commentId));

        long likeCount = likeRepository.countByComment(comment);
        long replyCount = commentRepository.countByParent(comment);

        return AdminCommentResponse.fromEntity(comment, likeCount, replyCount);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse approveComments(List<Long> commentIds, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();

        for (Long commentId : commentIds) {
            try {
                Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到评论: " + commentId));

                // 已经是通过状态的评论，跳过但计入成功（幂等操作）
                if (comment.getStatus() == CommentStatus.APPROVED) {
                    successIds.add(commentId);
                    logger.debug("评论 {} 已经是通过状态，跳过", commentId);
                    continue;
                }

                // 更新状态为已通过
                comment.setStatus(CommentStatus.APPROVED);
                commentRepository.save(comment);
                successIds.add(commentId);

                // 记录审计日志
                adminLogService.log(AdminLogType.COMMENT_APPROVE,
                        "审核通过评论 #" + commentId, null, null,
                        admin, null, null, commentId, null, null, null, null, null, null, null, null);

                logger.info("管理员 {} 审核通过评论 {}", admin.getUsername(), commentId);

            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(commentId, e.getMessage()));
            } catch (Exception e) {
                logger.error("审核通过评论 {} 失败: {}", commentId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(commentId, "操作失败: " + e.getMessage()));
            }
        }

        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse deleteComments(List<Long> commentIds, String formTitle,
                                                   String reason, String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();

        for (Long commentId : commentIds) {
            try {
                Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到评论: " + commentId));

                User commentAuthor = comment.getUser();
                String postTitle = comment.getPost() != null ? comment.getPost().getTitle() : "未知文章";
                Long postId = comment.getPost() != null ? comment.getPost().getId() : null;
                String contentPreview = truncateContent(comment.getContent(), 100);

                // 创建删除表单
                AdminForm form = new AdminForm();
                form.setTitle(formTitle != null ? formTitle : "评论删除通知");
                form.setFormType(AdminFormType.COMMENT_DELETION);
                form.setReason(reason);
                form.setExtraFields(extraFields);
                form.setPostId(postId);
                form.setPostTitle(postTitle);
                form.setCommentId(commentId);
                form.setCommentContentPreview(contentPreview);
                form.setTargetUser(commentAuthor);
                form.setAdmin(admin);
                form.setSent(true);
                form.setSentAt(LocalDateTime.now());

                adminFormRepository.save(form);

                // 发送删除通知（在删除评论之前）
                notificationService.createCommentDeletedNotification(admin, commentAuthor, postTitle, contentPreview, reason);

                // 解除通知表对评论及其所有后代评论的外键约束
                nullifyNotificationReferencesRecursively(comment);

                // 删除评论（会级联删除子评论和点赞）
                commentRepository.delete(comment);
                successIds.add(commentId);

                // 记录审计日志
                String commentDeleteDefault = "删除评论 #" + commentId;
                String commentDeleteTitle = (formTitle != null && !formTitle.trim().isEmpty())
                        ? formTitle : commentDeleteDefault;
                adminLogService.log(AdminLogType.COMMENT_DELETE,
                        commentDeleteTitle, reason, extraFields,
                        admin, null, null, commentId, null, null, null, null, null, null, null, null);

                logger.info("管理员 {} 删除评论 {}, 理由: {}", admin.getUsername(), commentId, reason);

                // 刷新持久化上下文，确保批量操作中每条评论的变更独立生效
                entityManager.flush();

            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(commentId, e.getMessage()));
            } catch (Exception e) {
                logger.error("删除评论 {} 失败: {}", commentId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(commentId, "操作失败: " + e.getMessage()));
            }
        }

        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }

    /**
     * 递归解除通知表对评论及其所有后代评论的外键约束
     * 必须先处理子评论，再处理父评论，以确保级联删除顺序正确
     */
    private void nullifyNotificationReferencesRecursively(Comment comment) {
        // 先递归处理所有子评论
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment child : comment.getReplies()) {
                nullifyNotificationReferencesRecursively(child);
            }
        }
        // 最后处理当前评论
        notificationRepository.nullifyCommentReference(comment);
    }

    /**
     * 截断内容用于预览
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return null;
        String cleaned = content.replace("\n", " ").replace("\r", "");
        if (cleaned.length() <= maxLength) return cleaned;
        return cleaned.substring(0, maxLength) + "...";
    }
}
