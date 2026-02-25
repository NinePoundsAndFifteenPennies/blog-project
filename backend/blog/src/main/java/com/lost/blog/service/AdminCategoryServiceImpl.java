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
import java.util.List;

/**
 * 管理员分类服务实现类
 */
@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final AdminFormRepository adminFormRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public AdminCategoryServiceImpl(CategoryRepository categoryRepository,
                                     PostRepository postRepository,
                                     AdminFormRepository adminFormRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
        this.adminFormRepository = adminFormRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCategoryResponse> searchCategories(AdminCategoryQueryRequest query, Pageable pageable) {
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

        Page<Object[]> results = categoryRepository.adminSearchCategories(
                query.getName(),
                query.getCreatedBy(),
                startDateTime,
                endDateTime,
                pageable
        );

        return results.map(row -> {
            Category category = (Category) row[0];
            Long postCount = (Long) row[1];
            return AdminCategoryResponse.fromEntity(category, postCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCategoryResponse getCategoryDetail(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到分类: " + categoryId));

        // 查询关联的文章列表
        List<Object[]> postRows = categoryRepository.findPostsByCategoryId(categoryId);
        AdminCategoryResponse response = AdminCategoryResponse.fromEntity(category, (long) postRows.size());

        if (!postRows.isEmpty()) {
            List<AdminCategoryResponse.PostInfo> postInfos = new ArrayList<>();
            for (Object[] row : postRows) {
                Long postId = (Long) row[0];
                String postTitle = (String) row[1];
                postInfos.add(new AdminCategoryResponse.PostInfo(postId, postTitle));
            }
            response.setPosts(postInfos);
        }

        return response;
    }

    @Override
    @Transactional
    public AdminCategoryResponse createCategory(CategoryRequest categoryRequest, User admin) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("分类名称已存在：" + categoryRequest.getName());
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setColor(categoryRequest.getColor());
        category.setIcon(categoryRequest.getIcon());
        category.setSortOrder(categoryRequest.getSortOrder());
        category.setCreatedBy(admin);

        Category savedCategory = categoryRepository.save(category);
        logger.info("管理员 {} 创建分类: {}", admin.getUsername(), savedCategory.getName());

        return AdminCategoryResponse.fromEntity(savedCategory, 0L);
    }

    @Override
    @Transactional
    public AdminCategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到分类: " + categoryId));

        // 如果修改了分类名称，需要检查新名称是否已被使用
        if (!category.getName().equals(categoryRequest.getName()) &&
            categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("分类名称已存在：" + categoryRequest.getName());
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setColor(categoryRequest.getColor());
        category.setIcon(categoryRequest.getIcon());
        category.setSortOrder(categoryRequest.getSortOrder());

        Category updatedCategory = categoryRepository.save(category);
        logger.info("管理员更新分类 {}: {}", categoryId, updatedCategory.getName());

        // 重新获取文章数量
        List<Object[]> postRows = categoryRepository.findPostsByCategoryId(categoryId);
        return AdminCategoryResponse.fromEntity(updatedCategory, (long) postRows.size());
    }

    @Override
    @Transactional
    public AdminCategoryResponse assignPostToCategory(Long categoryId, Long postId, User admin) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到分类: " + categoryId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + postId));

        post.setCategory(category);
        postRepository.save(post);
        logger.info("管理员 {} 将文章 {} 归入分类 {}", admin.getUsername(), postId, categoryId);

        // 重新获取详情
        return getCategoryDetail(categoryId);
    }

    @Override
    @Transactional
    public AdminCategoryResponse removePostFromCategory(Long categoryId, Long postId, User admin) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到分类: " + categoryId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + postId));

        if (post.getCategory() == null || !post.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("文章不属于该分类");
        }

        post.setCategory(null);
        postRepository.save(post);
        logger.info("管理员 {} 将文章 {} 从分类 {} 移除", admin.getUsername(), postId, categoryId);

        // 重新获取详情
        return getCategoryDetail(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<AdminCategoryResponse.PostInfo> searchPostsByTitle(String title) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        List<Object[]> results = postRepository.searchPostsByTitle(title, pageable);
        List<AdminCategoryResponse.PostInfo> postInfos = new ArrayList<>();
        for (Object[] row : results) {
            Long postId = (Long) row[0];
            String postTitle = (String) row[1];
            postInfos.add(new AdminCategoryResponse.PostInfo(postId, postTitle));
        }
        return postInfos;
    }

    @Override
    @Transactional
    public AdminBatchActionResponse deleteCategories(List<Long> categoryIds, String reason,
                                                      String extraFields, User admin) {
        List<Long> successIds = new ArrayList<>();
        List<AdminBatchActionResponse.FailureItem> failures = new ArrayList<>();

        for (Long categoryId : categoryIds) {
            try {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到分类: " + categoryId));

                User categoryCreator = category.getCreatedBy();
                String categoryName = category.getName();

                // 获取关联的文章列表
                List<Object[]> postRows = categoryRepository.findPostsByCategoryId(categoryId);

                // 收集受影响的文章信息
                String affectedPostsJson = "[]";
                if (!postRows.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        ArrayNode array = mapper.createArrayNode();
                        for (Object[] row : postRows) {
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

                // 创建删除表单
                AdminForm form = new AdminForm();
                form.setTitle("分类删除通知");
                form.setFormType(AdminFormType.CATEGORY_DELETION);
                form.setReason(reason);
                form.setExtraFields(extraFields);
                form.setCategoryId(categoryId);
                form.setCategoryName(categoryName);
                form.setAffectedPosts(affectedPostsJson);
                form.setTargetUser(categoryCreator);
                form.setAdmin(admin);
                form.setSent(false);

                adminFormRepository.save(form);

                // 先将所有使用该分类的文章的category字段设置为null
                List<Post> posts = postRepository.findByCategory(category);
                for (Post post : posts) {
                    post.setCategory(null);
                }
                postRepository.saveAll(posts);

                // 然后删除分类
                categoryRepository.delete(category);

                successIds.add(categoryId);
                logger.info("管理员 {} 删除分类 {} ({})，清除 {} 篇文章关联，理由: {}",
                        admin.getUsername(), categoryId, categoryName, posts.size(), reason);

                // 刷新持久化上下文，确保批量操作中每个分类的变更独立生效
                entityManager.flush();

            } catch (ResourceNotFoundException e) {
                failures.add(new AdminBatchActionResponse.FailureItem(categoryId, e.getMessage()));
            } catch (Exception e) {
                logger.error("删除分类 {} 失败: {}", categoryId, e.getMessage());
                failures.add(new AdminBatchActionResponse.FailureItem(categoryId, "操作失败: " + e.getMessage()));
            }
        }

        return new AdminBatchActionResponse(successIds.size(), failures.size(), successIds, failures);
    }
}
