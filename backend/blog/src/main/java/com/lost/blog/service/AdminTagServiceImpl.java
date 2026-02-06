package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.*;
import com.lost.blog.repository.*;
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

/**
 * 管理员标签服务实现类
 */
@Service
public class AdminTagServiceImpl implements AdminTagService {

    private static final Logger logger = LoggerFactory.getLogger(AdminTagServiceImpl.class);

    private final TagRepository tagRepository;
    private final AdminFormRepository adminFormRepository;
    private final NotificationService notificationService;

    @Autowired
    public AdminTagServiceImpl(TagRepository tagRepository,
                               AdminFormRepository adminFormRepository,
                               NotificationService notificationService) {
        this.tagRepository = tagRepository;
        this.adminFormRepository = adminFormRepository;
        this.notificationService = notificationService;
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
        Tag tag = tagRepository.findByIdWithPosts(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到标签: " + tagId));

        return AdminTagResponse.fromEntity(tag);
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

        return AdminTagResponse.fromEntity(savedTag, 0L);
    }

    @Override
    @Transactional
    public AdminTagResponse updateTag(Long tagId, TagRequest tagRequest) {
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
        logger.info("管理员更新标签 {}: {}", tagId, updatedTag.getName());

        return AdminTagResponse.fromEntity(updatedTag);
    }

    @Override
    @Transactional
    public AdminBatchActionResponse softDeleteTags(List<Long> tagIds, String formTitle,
                                                    String reason, String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();

        for (Long tagId : tagIds) {
            try {
                Tag tag = tagRepository.findByIdWithPosts(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到标签: " + tagId));

                User tagCreator = tag.getCreatedBy();
                String tagName = tag.getName();

                // 创建软删除表单
                AdminForm form = new AdminForm();
                form.setTitle(formTitle != null ? formTitle : "标签关联移除通知");
                form.setFormType(AdminFormType.TAG_SOFT_DELETION);
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
                notificationService.createTagRemovedNotification(admin, tagCreator, tagName, reason);

                // 软删除：只移除标签与所有文章的关联（post_tags表中的记录）
                if (tag.getPosts() != null && !tag.getPosts().isEmpty()) {
                    var posts = new HashSet<>(tag.getPosts());
                    for (var post : posts) {
                        post.getTags().remove(tag);
                    }
                    tag.getPosts().clear();
                    tagRepository.save(tag);
                }

                successIds.add(tagId);
                logger.info("管理员 {} 软删除标签 {} ({}), 理由: {}", admin.getUsername(), tagId, tagName, reason);

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
                Tag tag = tagRepository.findByIdWithPosts(tagId)
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

                // 硬删除：先移除所有文章关联，再删除标签本身
                if (tag.getPosts() != null && !tag.getPosts().isEmpty()) {
                    var posts = new HashSet<>(tag.getPosts());
                    for (var post : posts) {
                        post.getTags().remove(tag);
                    }
                }

                tagRepository.delete(tag);
                successIds.add(tagId);
                logger.info("管理员 {} 硬删除标签 {} ({}), 理由: {}", admin.getUsername(), tagId, tagName, reason);

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
