package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.AdminPostMapper;
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
import java.util.stream.Collectors;

/**
 * 管理员文章服务实现类
 */
@Service
public class AdminPostServiceImpl implements AdminPostService {

    private static final Logger logger = LoggerFactory.getLogger(AdminPostServiceImpl.class);

    private final PostRepository postRepository;
    private final AdminFormRepository adminFormRepository;
    private final AdminPostMapper adminPostMapper;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostViewLogRepository postViewLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdminPostServiceImpl(PostRepository postRepository,
                               AdminFormRepository adminFormRepository,
                               AdminPostMapper adminPostMapper,
                               NotificationService notificationService,
                               NotificationRepository notificationRepository,
                               CommentRepository commentRepository,
                               LikeRepository likeRepository,
                               PostViewLogRepository postViewLogRepository) {
        this.postRepository = postRepository;
        this.adminFormRepository = adminFormRepository;
        this.adminPostMapper = adminPostMapper;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.postViewLogRepository = postViewLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPostResponse> searchPosts(AdminPostQueryRequest query, Pageable pageable) {
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
        
        // 处理分类ID
        Long categoryId = null;
        if (query.getCategoryId() != null && !query.getCategoryId().isEmpty()) {
            try {
                categoryId = Long.parseLong(query.getCategoryId());
            } catch (NumberFormatException e) {
                logger.warn("无效的分类ID: {}", query.getCategoryId());
            }
        }
        
        Page<Post> posts = postRepository.adminSearchPosts(
                query.getTitle(),
                query.getAuthor(),
                query.getStatus(),
                categoryId,
                query.getTag(),
                startDateTime,
                endDateTime,
                pageable
        );
        
        return posts.map(adminPostMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPostResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + postId));
        return adminPostMapper.toResponse(post, true);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse approvePosts(List<Long> postIds, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();
        
        for (Long postId : postIds) {
            try {
                Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + postId));
                
                // 检查文章状态是否可以审核通过
                if (post.getStatus() != PostStatus.PENDING_REVIEW && 
                    post.getStatus() != PostStatus.PENDING_REVISION) {
                    failures.add(new AdminBatchActionResponse.FailureItem(postId, 
                            "文章状态不允许审核通过: " + post.getStatus()));
                    continue;
                }
                
                // 如果是PENDING_REVISION状态，清除之前的内容
                if (post.getStatus() == PostStatus.PENDING_REVISION) {
                    post.setPreviousContent(null);
                    post.setPreviousTitle(null);
                }
                
                // 更新状态为已发布
                post.setStatus(PostStatus.PUBLISHED);
                post.setDraft(false);
                post.setRejectionFormId(null);
                
                // 如果没有发布时间，设置发布时间
                if (post.getPublishedAt() == null) {
                    post.setPublishedAt(LocalDateTime.now());
                }
                
                postRepository.save(post);
                successIds.add(postId);
                
                // 发送审核通过通知
                notificationService.createPostApprovedNotification(admin, post);
                
                logger.info("管理员 {} 审核通过文章 {}", admin.getUsername(), postId);
                
            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(postId, e.getMessage()));
            } catch (Exception e) {
                logger.error("审核通过文章 {} 失败: {}", postId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(postId, "操作失败: " + e.getMessage()));
            }
        }
        
        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse rejectPosts(List<Long> postIds, String formTitle, 
                                                String reason, String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();
        
        for (Long postId : postIds) {
            try {
                Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + postId));
                
                // 检查文章状态是否可以拒绝（不能拒绝草稿）
                if (post.getStatus() == PostStatus.DRAFT) {
                    failures.add(new AdminBatchActionResponse.FailureItem(postId, 
                            "草稿状态的文章无需审核。可拒绝的状态: 待审核、修改待审核、已发布"));
                    continue;
                }
                
                if (post.getStatus() == PostStatus.REJECTED) {
                    failures.add(new AdminBatchActionResponse.FailureItem(postId, 
                            "文章已经是拒绝状态，无法重复拒绝"));
                    continue;
                }
                
                // 创建拒绝表单
                AdminForm form = new AdminForm();
                form.setTitle(formTitle != null ? formTitle : "文章审核拒绝通知");
                form.setFormType(AdminFormType.REJECTION);
                form.setReason(reason);
                form.setExtraFields(extraFields);
                form.setPostId(postId);
                form.setPostTitle(post.getTitle());
                form.setTargetUser(post.getUser());
                form.setAdmin(admin);
                form.setSent(true);
                form.setSentAt(LocalDateTime.now());
                
                adminFormRepository.save(form);
                
                // 更新为拒绝状态（包括修改待审核的文章）
                post.setStatus(PostStatus.REJECTED);
                post.setDraft(false);
                post.setPreviousContent(null);
                post.setPreviousTitle(null);
                
                post.setRejectionFormId(form.getId());
                postRepository.save(post);
                successIds.add(postId);
                
                // 发送审核拒绝通知
                notificationService.createPostRejectedNotification(admin, post, reason);
                
                logger.info("管理员 {} 审核拒绝文章 {}, 理由: {}", admin.getUsername(), postId, reason);
                
            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(postId, e.getMessage()));
            } catch (Exception e) {
                logger.error("审核拒绝文章 {} 失败: {}", postId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(postId, "操作失败: " + e.getMessage()));
            }
        }
        
        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse deletePosts(List<Long> postIds, String formTitle, 
                                                String reason, String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();
        
        for (Long postId : postIds) {
            try {
                Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + postId));
                
                // 创建删除表单
                AdminForm form = new AdminForm();
                form.setTitle(formTitle != null ? formTitle : "文章删除通知");
                form.setFormType(AdminFormType.DELETION);
                form.setReason(reason);
                form.setExtraFields(extraFields);
                form.setPostId(postId);
                form.setPostTitle(post.getTitle());
                form.setTargetUser(post.getUser());
                form.setAdmin(admin);
                form.setSent(true);
                form.setSentAt(LocalDateTime.now());
                
                adminFormRepository.save(form);
                
                User postAuthor = post.getUser();
                String postTitle = post.getTitle();
                
                // 发送删除通知（在删除文章之前，这样通知中不会引用即将被删除的文章）
                notificationService.createPostDeletedNotification(admin, postAuthor, postTitle, reason);
                
                // 解除通知表对文章和评论的外键约束（必须在删除评论和文章之前）
                notificationRepository.nullifyAllReferencesForPost(post);
                
                // 删除文章的浏览日志
                postViewLogRepository.deleteByPost(post);
                
                // 删除文章的点赞（文章直接点赞）
                likeRepository.deleteByPost(post);
                
                // 删除文章的评论（会级联删除评论的点赞和子评论）
                commentRepository.deleteByPost(post);
                
                // 删除文章（会级联删除 post_tags）
                postRepository.delete(post);
                successIds.add(postId);
                
                logger.info("管理员 {} 删除文章 {}, 理由: {}", admin.getUsername(), postId, reason);

                // 刷新持久化上下文，确保批量操作中每篇文章的变更独立生效
                entityManager.flush();
                
            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(postId, e.getMessage()));
            } catch (Exception e) {
                logger.error("删除文章 {} 失败: {}", postId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(postId, "操作失败: " + e.getMessage()));
            }
        }
        
        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFormResponse getFormDetail(Long formId) {
        AdminForm form = adminFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到表单: " + formId));
        return AdminFormResponse.fromEntity(form);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminFormResponse> getPostForms(Long postId) {
        List<AdminForm> forms = adminFormRepository.findByPostId(postId);
        return forms.stream()
                .map(AdminFormResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
