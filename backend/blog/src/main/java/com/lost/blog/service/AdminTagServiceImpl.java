package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.*;
import com.lost.blog.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理员标签服务实现类
 */
@Service
public class AdminTagServiceImpl implements AdminTagService {

    private static final Logger logger = LoggerFactory.getLogger(AdminTagServiceImpl.class);

    private final TagRepository tagRepository;
    private final AdminFormRepository adminFormRepository;
    private final NotificationService notificationService;
    private final AdminLogService adminLogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdminTagServiceImpl(TagRepository tagRepository,
                               AdminFormRepository adminFormRepository,
                               NotificationService notificationService,
                               AdminLogService adminLogService) {
        this.tagRepository = tagRepository;
        this.adminFormRepository = adminFormRepository;
        this.notificationService = notificationService;
        this.adminLogService = adminLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminTagResponse> searchTags(AdminTagQueryRequest query, Pageable pageable) {
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

        Page<Object[]> results = tagRepository.adminSearchTags(
                query.getName(),
                query.getCreatedBy(),
                startDateTime,
                endDateTime,
                pageable
        );

        return results.map(row -> {
            Tag tag = (Tag) row[0];
            Long postCount = (Long) row[1];
            return AdminTagResponse.fromEntity(tag, postCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTagResponse getTagDetail(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到标签: " + tagId));

        // 通过专用查询获取关联的文章列表（避免依赖Hibernate延迟加载）
        List<Object[]> postRows = tagRepository.findPostsByTagId(tagId);
        AdminTagResponse response = AdminTagResponse.fromEntity(tag, (long) postRows.size());

        if (!postRows.isEmpty()) {
            List<AdminTagResponse.PostInfo> postInfos = new ArrayList<>();
            for (Object[] row : postRows) {
                Long postId = (Long) row[0];
                String postTitle = (String) row[1];
                postInfos.add(new AdminTagResponse.PostInfo(postId, postTitle));
            }
            response.setPosts(postInfos);
        }

        return response;
    }

    @Override
    @Transactional
    public AdminTagResponse createTag(TagRequest tagRequest, User admin) {
        // 检查标签名称是否已存在
        if (tagRepository.existsByName(tagRequest.getName())) {
            throw new IllegalArgumentException("标签名称已存在：" + tagRequest.getName());
        }

        Tag tag = new Tag();
        tag.setName(tagRequest.getName());
        tag.setDescription(tagRequest.getDescription());
        tag.setColor(tagRequest.getColor());
        tag.setIcon(tagRequest.getIcon());
        tag.setSortOrder(tagRequest.getSortOrder());
        tag.setCreatedBy(admin);

        Tag savedTag = tagRepository.save(tag);
        logger.info("管理员 {} 创建标签: {}", admin.getUsername(), savedTag.getName());

        // 记录审计日志
        adminLogService.log(AdminLogType.TAG_CREATE,
                "创建标签: " + savedTag.getName(), null, null,
                admin, null, null, null, null, savedTag.getId(), savedTag.getName(), null, null, null, null, null);

        return AdminTagResponse.fromEntity(savedTag, 0L);
    }

    @Override
    @Transactional
    public AdminTagResponse updateTag(Long tagId, TagRequest tagRequest, User admin) {
        Tag tag = tagRepository.findByIdWithPosts(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到标签: " + tagId));

        // 如果修改了标签名称，需要检查新名称是否已被使用
        if (!tag.getName().equals(tagRequest.getName()) &&
            tagRepository.existsByName(tagRequest.getName())) {
            throw new IllegalArgumentException("标签名称已存在：" + tagRequest.getName());
        }

        tag.setName(tagRequest.getName());
        tag.setDescription(tagRequest.getDescription());
        tag.setColor(tagRequest.getColor());
        tag.setIcon(tagRequest.getIcon());
        tag.setSortOrder(tagRequest.getSortOrder());

        Tag updatedTag = tagRepository.save(tag);
        logger.info("管理员 {} 更新标签 {}: {}", admin.getUsername(), tagId, updatedTag.getName());

        // 记录审计日志
        adminLogService.log(AdminLogType.TAG_UPDATE,
                "更新标签: " + updatedTag.getName(), null, null,
                admin, null, null, null, null, updatedTag.getId(), updatedTag.getName(), null, null, null, null, null);

        return AdminTagResponse.fromEntity(updatedTag);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse softDeleteTags(List<Long> tagIds, List<Long> postIds,
                                                    String formTitle, String reason, String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();

        for (Long tagId : tagIds) {
            try {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到标签: " + tagId));

                User tagCreator = tag.getCreatedBy();
                String tagName = tag.getName();

                // 通过专用查询获取关联的文章（避免依赖Hibernate延迟加载）
                List<Object[]> postRows = tagRepository.findPostsByTagId(tagId);

                // 确定要解除关联的文章列表
                Set<Long> removePostIds;
                List<Object[]> postsToRemoveRows;
                if (postIds != null && !postIds.isEmpty()) {
                    // 选择性解除：只移除指定的文章关联
                    Set<Long> targetPostIds = new HashSet<>(postIds);
                    postsToRemoveRows = new ArrayList<>();
                    for (Object[] row : postRows) {
                        Long postId = (Long) row[0];
                        if (targetPostIds.contains(postId)) {
                            postsToRemoveRows.add(row);
                        }
                    }
                    if (postsToRemoveRows.isEmpty()) {
                        failures.add(new AdminBatchActionResponse.FailureItem(tagId,
                                "标签「" + tagName + "」与指定的 " + targetPostIds.size() + " 篇文章均无关联"));
                        continue;
                    }
                } else {
                    // 全部解除：移除标签与所有文章的关联
                    postsToRemoveRows = postRows;
                }

                removePostIds = new HashSet<>();
                for (Object[] row : postsToRemoveRows) {
                    removePostIds.add((Long) row[0]);
                }

                // 收集受影响的文章信息
                String affectedPostsJson = "[]";
                if (!postsToRemoveRows.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        ArrayNode array = mapper.createArrayNode();
                        for (Object[] row : postsToRemoveRows) {
                            ObjectNode node = mapper.createObjectNode();
                            node.put("postId", (Long) row[0]);
                            node.put("postTitle", (String) row[1]);
                            array.add(node);
                        }
                        affectedPostsJson = mapper.writeValueAsString(array);
                    } catch (Exception e) {
                        logger.warn("序列化受影响文章列表失败: {}", e.getMessage());
                    }
                }

                // 创建软删除表单
                AdminForm form = new AdminForm();
                form.setTitle(formTitle != null ? formTitle : "标签关联移除通知");
                form.setFormType(AdminFormType.TAG_SOFT_DELETION);
                form.setReason(reason);
                form.setExtraFields(extraFields);
                form.setTagId(tagId);
                form.setTagName(tagName);
                form.setAffectedPosts(affectedPostsJson);
                form.setTargetUser(tagCreator);
                form.setAdmin(admin);
                form.setSent(true);
                form.setSentAt(LocalDateTime.now());

                adminFormRepository.save(form);

                // 发送通知
                notificationService.createTagRemovedNotification(admin, tagCreator, tagName, reason);

                // 软删除：通过原生方式批量移除指定的文章关联
                tagRepository.removePostTagAssociations(tagId, removePostIds);

                successIds.add(tagId);
                
                // 记录审计日志
                String softDelDefault = "软删除标签 #" + tagId;
                String softDelTitle = (formTitle != null && !formTitle.trim().isEmpty())
                        ? formTitle : softDelDefault;
                adminLogService.log(AdminLogType.TAG_SOFT_DELETE,
                        softDelTitle, reason, extraFields,
                        admin, null, null, null, null, tagId, tagName, null, null, null, null, null);
                
                logger.info("管理员 {} 软删除标签 {} ({})，解除 {} 篇文章关联，理由: {}",
                        admin.getUsername(), tagId, tagName, removePostIds.size(), reason);

                // 刷新持久化上下文，确保批量操作中每个标签的变更独立生效
                entityManager.flush();

            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(tagId, e.getMessage()));
            } catch (Exception e) {
                logger.error("软删除标签 {} 失败: {}", tagId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(tagId, "操作失败: " + e.getMessage()));
            }
        }

        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse hardDeleteTags(List<Long> tagIds, String formTitle,
                                                    String reason, String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();

        for (Long tagId : tagIds) {
            try {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到标签: " + tagId));

                User tagCreator = tag.getCreatedBy();
                String tagName = tag.getName();

                // 创建硬删除表单
                AdminForm form = new AdminForm();
                form.setTitle(formTitle != null ? formTitle : "标签删除通知");
                form.setFormType(AdminFormType.TAG_HARD_DELETION);
                form.setReason(reason);
                form.setExtraFields(extraFields);
                form.setTagId(tagId);
                form.setTagName(tagName);
                form.setTargetUser(tagCreator);
                form.setAdmin(admin);
                form.setSent(true);
                form.setSentAt(LocalDateTime.now());

                adminFormRepository.save(form);

                // 发送通知
                notificationService.createTagDeletedNotification(admin, tagCreator, tagName, reason);

                // 硬删除：先移除所有文章关联（通过原生查询），再删除标签本身
                tagRepository.removeAllPostTagAssociations(tagId);
                tagRepository.delete(tag);

                successIds.add(tagId);
                
                // 记录审计日志
                String hardDelDefault = "硬删除标签 #" + tagId;
                String hardDelTitle = (formTitle != null && !formTitle.trim().isEmpty())
                        ? formTitle : hardDelDefault;
                adminLogService.log(AdminLogType.TAG_HARD_DELETE,
                        hardDelTitle, reason, extraFields,
                        admin, null, null, null, null, tagId, tagName, null, null, null, null, null);
                
                logger.info("管理员 {} 硬删除标签 {} ({}), 理由: {}", admin.getUsername(), tagId, tagName, reason);

                // 刷新持久化上下文，确保批量操作中每个标签的变更独立生效
                entityManager.flush();

            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(tagId, e.getMessage()));
            } catch (Exception e) {
                logger.error("硬删除标签 {} 失败: {}", tagId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(tagId, "操作失败: " + e.getMessage()));
            }
        }

        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }
}
