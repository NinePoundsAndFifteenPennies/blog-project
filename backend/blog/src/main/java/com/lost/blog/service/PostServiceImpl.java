package com.lost.blog.service;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import com.lost.blog.model.PostStatus;
import com.lost.blog.model.PostViewLog;
import com.lost.blog.model.Tag;
import com.lost.blog.model.User;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.PostViewLogRepository;
import com.lost.blog.repository.TagRepository;
import com.lost.blog.repository.UserRepository;
import com.lost.blog.mapper.PostMapper;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.exception.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final PostViewLogRepository postViewLogRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;
    private final com.lost.blog.repository.CommentRepository commentRepository;
    private final com.lost.blog.repository.LikeRepository likeRepository;
    private final com.lost.blog.repository.CategoryRepository categoryRepository;
    private final com.lost.blog.repository.NotificationRepository notificationRepository;
    private final FileService fileService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           PostViewLogRepository postViewLogRepository,
                           UserRepository userRepository,
                           TagRepository tagRepository,
                           PostMapper postMapper,
                           com.lost.blog.repository.CommentRepository commentRepository,
                           com.lost.blog.repository.LikeRepository likeRepository,
                           com.lost.blog.repository.CategoryRepository categoryRepository,
                           com.lost.blog.repository.NotificationRepository notificationRepository,
                           FileService fileService) {
        this.postRepository = postRepository;
        this.postViewLogRepository = postViewLogRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.postMapper = postMapper;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.categoryRepository = categoryRepository;
        this.notificationRepository = notificationRepository;
        this.fileService = fileService;
    }

    @Override
    @Transactional
    public PostResponse createPost(PostRequest postRequest, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 检查同一用户下是否已有相同标题的文章
        if (postRepository.existsByTitleAndUser(postRequest.getTitle(), user)) {
            logger.warn("用户 {} 尝试创建重复标题的文章: {}", user.getUsername(), postRequest.getTitle());
            throw new RuntimeException("错误：您已有同名文章！");
        }

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        boolean isDraft = postRequest.getDraft() != null ? postRequest.getDraft() : false;
        post.setDraft(isDraft);
        post.setCoverImageUrl(postRequest.getCoverImageUrl());
        post.setUser(user);
        
        // 设置初始状态
        if (isDraft) {
            post.setStatus(PostStatus.DRAFT);
        } else {
            // 非草稿则提交审核
            post.setStatus(PostStatus.PENDING_REVIEW);
        }

        // 处理标签
        if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
            Set<Tag> tags = processTags(postRequest.getTags(), user);
            post.setTags(tags);
        }

        // 处理分类
        if (postRequest.getCategory() != null && !postRequest.getCategory().trim().isEmpty()) {
            com.lost.blog.model.Category category = processCategory(postRequest.getCategory().trim(), user);
            post.setCategory(category);
        }

        Post savedPost = postRepository.save(post);
        logger.info("用户 {} 创建了新文章，ID: {}，是否草稿: {}",
                user.getUsername(), savedPost.getId(), savedPost.getDraft());

        return postMapper.toResponse(savedPost, currentUser);
    }

    @Override
    @Transactional
    public PostResponse getPostById(Long id, UserDetails currentUser, String ip, String userAgent, String referer) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 如果文章作者被禁用，隐藏其文章（除非是本人查看）
        User author = post.getUser();
        if (author.getEnabled() != null && !author.getEnabled()) {
            // 检查是否为作者本人
            boolean isAuthor = currentUser != null && 
                    author.getUsername().equals(currentUser.getUsername());
            if (!isAuthor) {
                logger.debug("文章 {} 的作者已被禁用，隐藏文章", id);
                throw new ResourceNotFoundException("未找到ID为: " + id + " 的文章");
            }
        }

        // 检查是否为作者本人
        boolean isAuthor = currentUser != null && 
                author.getUsername().equals(currentUser.getUsername());

        // 草稿文章权限检查
        if (post.getDraft()) {
            // 如果是草稿，必须是作者本人才能查看
            if (currentUser == null) {
                logger.warn("匿名用户尝试访问草稿文章，ID: {}", id);
                throw new AccessDeniedException("草稿文章需要登录查看");
            }
            if (!isAuthor) {
                logger.warn("用户 {} 尝试访问他人草稿，文章ID: {}", currentUser.getUsername(), id);
                throw new AccessDeniedException("无权查看该草稿");
            }
        }

        // 非作者查看待审核或被拒绝的文章时的处理
        PostStatus currentStatus = post.getStatus();
        if (!isAuthor) {
            // PENDING_REVIEW、REJECTED 状态的文章对外不可见
            if (currentStatus == PostStatus.PENDING_REVIEW || currentStatus == PostStatus.REJECTED) {
                throw new ResourceNotFoundException("未找到ID为: " + id + " 的文章");
            }
            
            // PENDING_REVISION 状态 - 对外展示旧版本内容
            if (currentStatus == PostStatus.PENDING_REVISION) {
                // 使用旧版本内容创建响应（不修改实体）
                String displayTitle = post.getPreviousTitle() != null ? post.getPreviousTitle() : post.getTitle();
                String displayContent = post.getPreviousContent() != null ? post.getPreviousContent() : post.getContent();
                PostResponse response = postMapper.toResponse(post, currentUser);
                response.setTitle(displayTitle);
                response.setContent(displayContent);
                return response;
            }
        }

        // --- 浏览量统计逻辑 START ---
        // 只对已发布的文章计算浏览量
        // 注意：存在极小的竞态条件可能性（两个并发请求同时通过检查），
        // 但对于浏览量统计而言，偶尔的轻微过计数是可接受的，
        // 使用悲观锁会显著影响性能，不值得权衡。
        if (!post.getDraft() && ip != null && !ip.isEmpty()) {
            // 定义防刷时间：1小时 (也就是过去一小时内，同一个IP看同一篇文章不重复计数)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

            // 使用 JPA 方法名查询检查是否存在
            boolean alreadyViewed = postViewLogRepository.existsByPostAndIpAndCreateTimeAfter(post, ip, oneHourAgo);

            if (!alreadyViewed) {
                // 获取当前登录用户的ID（未登录则为null）
                Long userId = null;
                if (currentUser != null) {
                    userId = userRepository.findByUsername(currentUser.getUsername())
                            .map(User::getId)
                            .orElse(null);
                }

                // 1. 记录流水 (为了后台统计)
                PostViewLog log = new PostViewLog(post, ip, userAgent, userId, referer);
                postViewLogRepository.save(log);

                // 2. 增加文章总数 (为了前台展示)
                post.setViewCount(post.getViewCount() + 1);
                postRepository.save(post);
                
                logger.debug("文章 {} 浏览量+1，IP: {}", id, ip);
            }
        }
        // --- 浏览量统计逻辑 END ---

        return postMapper.toResponse(post, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(String sortBy, String order, Pageable pageable, UserDetails currentUser) {
        Page<Post> postsPage;
        
        if ("hotness".equalsIgnoreCase(sortBy)) {
            // 按热度排序
            boolean ascending = "asc".equalsIgnoreCase(order);
            postsPage = postRepository.findByDraftFalseOrderByHotness(ascending, pageable);
            logger.debug("查询已发布文章列表（按热度{}），页码: {}，数量: {}",
                    ascending ? "升序" : "降序", pageable.getPageNumber(), postsPage.getTotalElements());
        } else {
            // 按时间排序（默认）
            boolean ascending = "asc".equalsIgnoreCase(order);
            postsPage = postRepository.findByDraftFalseOrderByCreatedAt(ascending, pageable);
            logger.debug("查询已发布文章列表（按时间{}），页码: {}，数量: {}",
                    ascending ? "升序" : "降序", pageable.getPageNumber(), postsPage.getTotalElements());
        }
        
        return postsPage.map(post -> postMapper.toResponse(post, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(UserDetails currentUser, Pageable pageable) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));
        
        Page<Post> postsPage = postRepository.findByUser(user, pageable);
        logger.debug("查询用户 {} 的所有文章（包括草稿），总数: {}",
                user.getUsername(), postsPage.getTotalElements());
        
        return postsPage.map(post -> postMapper.toResponse(post, currentUser));
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest postRequest, UserDetails currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 权限校验
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            logger.warn("用户 {} 尝试修改他人文章，ID: {}", currentUser.getUsername(), id);
            throw new AccessDeniedException("无权修改该文章");
        }

        // 标题重复检查（只在标题被修改时检查）
        if (!post.getTitle().equals(postRequest.getTitle())) {
            if (postRepository.existsByTitleAndUserAndIdNot(
                    postRequest.getTitle(),
                    post.getUser(),
                    id)) {
                logger.warn("用户 {} 尝试将文章改为重复标题: {}",
                        currentUser.getUsername(), postRequest.getTitle());
                throw new RuntimeException("您已有标题为「" + postRequest.getTitle() + "」的文章！");
            }
        }

        // 记录草稿状态变化
        boolean wasDraft = post.getDraft();
        boolean willBeDraft = postRequest.getDraft() != null ? postRequest.getDraft() : false;
        PostStatus currentStatus = post.getStatus();

        // 处理封面图片更新 - 删除旧的封面图片（如果有变化）
        String oldCoverImageUrl = post.getCoverImageUrl();
        String newCoverImageUrl = postRequest.getCoverImageUrl();
        
        // 如果封面图片发生变化且旧的不为空，删除旧文件
        if (oldCoverImageUrl != null && !oldCoverImageUrl.isEmpty() && 
            !oldCoverImageUrl.equals(newCoverImageUrl)) {
            try {
                fileService.deleteImage(currentUser.getUsername(), oldCoverImageUrl);
                logger.info("删除旧封面图片: {}", oldCoverImageUrl);
            } catch (Exception e) {
                logger.warn("删除旧封面图片失败: {}, 错误: {}", oldCoverImageUrl, e.getMessage());
                // 继续执行，不影响文章更新
            }
        }

        // 检查是否有实质内容变化（用于已发布文章的重审逻辑）
        boolean contentChanged = !java.util.Objects.equals(post.getTitle(), postRequest.getTitle()) ||
                                 !java.util.Objects.equals(post.getContent(), postRequest.getContent());
        boolean shouldResubmitForReview = contentChanged && !willBeDraft;

        // 处理已发布文章的修改重审逻辑
        if (currentStatus == PostStatus.PUBLISHED && shouldResubmitForReview) {
            // 保存旧版本内容，用于审核期间展示
            post.setPreviousTitle(post.getTitle());
            post.setPreviousContent(post.getContent());
            // 更新状态为"发布后修改待审核"
            post.setStatus(PostStatus.PENDING_REVISION);
            logger.info("已发布文章 {} 被修改，状态变更为 PENDING_REVISION", id);
        }
        
        // 处理被拒绝文章的修改重审逻辑
        if (currentStatus == PostStatus.REJECTED && shouldResubmitForReview) {
            // 被拒绝的文章修改后，重新提交审核
            post.setStatus(PostStatus.PENDING_REVIEW);
            post.setRejectionFormId(null);  // 清除之前的拒绝表单关联
            logger.info("被拒绝文章 {} 被修改，状态变更为 PENDING_REVIEW", id);
        }

        // 更新字段
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        post.setDraft(willBeDraft);
        post.setCoverImageUrl(newCoverImageUrl);

        // 处理草稿状态变化时的状态更新
        if (wasDraft && !willBeDraft) {
            // 从草稿变为非草稿，提交审核
            if (post.getStatus() == PostStatus.DRAFT) {
                post.setStatus(PostStatus.PENDING_REVIEW);
                logger.info("文章 {} 从草稿提交审核，状态变更为 PENDING_REVIEW", id);
            }
        } else if (!wasDraft && willBeDraft) {
            // 从已发布变为草稿
            post.setStatus(PostStatus.DRAFT);
            // 清除之前的内容备份
            post.setPreviousTitle(null);
            post.setPreviousContent(null);
            logger.info("文章 {} 变为草稿，状态变更为 DRAFT", id);
        }

        // 更新标签
        if (postRequest.getTags() != null) {
            Set<Tag> tags = processTags(postRequest.getTags(), post.getUser());
            post.setTags(tags);
        }

        // 更新分类
        if (postRequest.getCategory() != null && !postRequest.getCategory().trim().isEmpty()) {
            com.lost.blog.model.Category category = processCategory(postRequest.getCategory().trim(), post.getUser());
            post.setCategory(category);
        } else {
            // 如果请求中category为null或空字符串，则移除分类（软删除）
            post.setCategory(null);
        }

        Post updatedPost = postRepository.save(post);

        // 记录状态变更
        if (wasDraft && !willBeDraft) {
            logger.info("文章 {} 从草稿变为已发布", id);
        } else if (!wasDraft && willBeDraft) {
            logger.info("文章 {} 从已发布变为草稿", id);
        }

        return postMapper.toResponse(updatedPost, currentUser);
    }

    @Override
    @Transactional
    public void deletePost(Long id, UserDetails currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            logger.warn("用户 {} 尝试删除他人文章，ID: {}", currentUser.getUsername(), id);
            throw new AccessDeniedException("无权删除该文章");
        }

        // 解除所有通知对该文章及其评论的引用（防止外键约束错误）
        notificationRepository.nullifyAllReferencesForPost(post);
        logger.info("解除文章ID: {} 相关通知的外键引用", id);

        // 先删除该文章的所有点赞（级联删除）
        likeRepository.deleteByPost(post);
        logger.info("删除文章ID: {} 的所有文章点赞", id);

        // 获取该文章的所有评论，并删除每个评论的点赞
        var comments = commentRepository.findByPost(post, org.springframework.data.domain.Pageable.unpaged());
        comments.forEach(comment -> {
            likeRepository.deleteByComment(comment);
            logger.info("删除评论ID: {} 的所有点赞", comment.getId());
        });

        // 再删除该文章的所有评论（级联删除）
        commentRepository.deleteByPost(post);
        logger.info("删除文章ID: {} 的所有评论", id);

        // 删除该文章的所有浏览日志
        postViewLogRepository.deleteByPost(post);
        logger.info("删除文章ID: {} 的所有浏览日志", id);

        // 删除封面图片（如果有）
        if (post.getCoverImageUrl() != null && !post.getCoverImageUrl().isEmpty()) {
            try {
                fileService.deleteImage(currentUser.getUsername(), post.getCoverImageUrl());
                logger.info("删除文章ID: {} 的封面图片: {}", id, post.getCoverImageUrl());
            } catch (Exception e) {
                logger.warn("删除文章封面图片失败: {}, 错误: {}", post.getCoverImageUrl(), e.getMessage());
                // 继续执行，不影响文章删除
            }
        }

        postRepository.delete(post);
        logger.info("用户 {} 删除了文章，ID: {}，标题: {}",
                currentUser.getUsername(), id, post.getTitle());
    }

    /**
     * 处理标签：查找已存在的标签或创建新标签
     */
    private Set<Tag> processTags(Set<String> tagNames, User creator) {
        Set<Tag> tags = new HashSet<>();
        
        if (tagNames == null || tagNames.isEmpty()) {
            return tags;
        }

        for (String tagName : tagNames) {
            // 去除首尾空格
            String trimmedName = tagName.trim();
            if (trimmedName.isEmpty()) {
                continue;
            }

            // 查找或创建标签
            Tag tag = tagRepository.findByName(trimmedName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(trimmedName);
                        newTag.setCreatedBy(creator);
                        // 自动分配随机可见颜色（避免太白或太黑）
                        newTag.setColor(generateRandomVisibleColor());
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }

        return tags;
    }

    /**
     * 生成随机可见颜色
     * 避免太亮（接近白色）或太暗（接近黑色）的颜色
     */
    private String generateRandomVisibleColor() {
        java.util.Random random = new java.util.Random();
        // 生成RGB值，范围在60-220之间，避免太暗或太亮
        int r = 60 + random.nextInt(161);  // 60-220
        int g = 60 + random.nextInt(161);  // 60-220
        int b = 60 + random.nextInt(161);  // 60-220
        
        // 确保颜色有足够的饱和度，不要太灰
        // 如果三个值太接近，重新生成让其中一个更突出
        if (Math.abs(r - g) < 40 && Math.abs(g - b) < 40 && Math.abs(r - b) < 40) {
            int[] values = {r, g, b};
            int indexToBoost = random.nextInt(3);
            values[indexToBoost] = Math.min(220, values[indexToBoost] + 60);
            r = values[0];
            g = values[1];
            b = values[2];
        }
        
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * 处理分类：查找已存在的分类或创建新分类
     */
    private com.lost.blog.model.Category processCategory(String categoryName, User creator) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }

        String trimmedName = categoryName.trim();
        
        // 查找或创建分类
        return categoryRepository.findByName(trimmedName)
                .orElseGet(() -> {
                    com.lost.blog.model.Category newCategory = new com.lost.blog.model.Category();
                    newCategory.setName(trimmedName);
                    newCategory.setCreatedBy(creator);
                    // 自动分配随机可见颜色
                    newCategory.setColor(generateRandomVisibleColor());
                    return categoryRepository.save(newCategory);
                });
    }

    @Override
    @Transactional
    public PostResponse removeTagFromPost(Long postId, String tagName, UserDetails currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + postId + " 的文章"));

        // 权限校验：只有文章作者可以移除标签
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            logger.warn("用户 {} 尝试从他人文章移除标签，文章ID: {}", currentUser.getUsername(), postId);
            throw new AccessDeniedException("无权修改该文章");
        }

        // 查找并移除标签
        Tag tagToRemove = tagRepository.findByName(tagName)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在：" + tagName));

        post.getTags().remove(tagToRemove);
        Post updatedPost = postRepository.save(post);
        
        logger.info("用户 {} 从文章 {} 中移除了标签 {}", currentUser.getUsername(), postId, tagName);
        
        return postMapper.toResponse(updatedPost, currentUser);
    }

    @Override
    @Transactional
    public PostResponse removeCategoryFromPost(Long postId, UserDetails currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + postId + " 的文章"));

        // 权限校验：只有文章作者可以移除分类
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            logger.warn("用户 {} 尝试从他人文章移除分类，文章ID: {}", currentUser.getUsername(), postId);
            throw new AccessDeniedException("无权修改该文章");
        }

        post.setCategory(null);
        Post updatedPost = postRepository.save(post);
        
        logger.info("用户 {} 从文章 {} 中移除了分类", currentUser.getUsername(), postId);
        
        return postMapper.toResponse(updatedPost, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUsername(String username, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + username));
        
        // 如果用户被禁用，返回空页面（隐藏被封禁用户的文章）
        if (user.getEnabled() != null && !user.getEnabled()) {
            logger.debug("用户 {} 已被禁用，隐藏其文章", username);
            return Page.empty(pageable);
        }
        
        // 只返回该用户已发布的文章（不包括草稿）
        Page<Post> postsPage = postRepository.findByUserAndDraftFalse(user, pageable);
        logger.debug("查询用户 {} 的已发布文章，总数: {}",
                username, postsPage.getTotalElements());
        
        return postsPage.map(post -> postMapper.toResponse(post, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(
            String keyword,
            String author,
            String title,
            String tag,
            String sortBy,
            String order,
            Pageable pageable,
            UserDetails currentUser) {
        
        // 预处理搜索词：去除首尾空格，处理特殊字符
        String normalizedKeyword = normalizeSearchTerm(keyword);
        String normalizedAuthor = normalizeSearchTerm(author);
        String normalizedTitle = normalizeSearchTerm(title);
        String normalizedTag = normalizeSearchTerm(tag);
        
        logger.debug("执行搜索：keyword={}, author={}, title={}, tag={}, sortBy={}, order={}",
                normalizedKeyword, normalizedAuthor, normalizedTitle, normalizedTag, sortBy, order);
        
        Page<Post> postsPage;
        boolean ascending = "asc".equalsIgnoreCase(order);
        
        // 根据排序方式选择不同的查询方法
        if ("hotness".equalsIgnoreCase(sortBy)) {
            if (ascending) {
                postsPage = postRepository.searchPostsByHotnessAsc(
                        normalizedKeyword, normalizedAuthor, normalizedTitle, normalizedTag, pageable);
            } else {
                postsPage = postRepository.searchPostsByHotnessDesc(
                        normalizedKeyword, normalizedAuthor, normalizedTitle, normalizedTag, pageable);
            }
        } else {
            // 默认按时间排序
            if (ascending) {
                postsPage = postRepository.searchPostsByTimeAsc(
                        normalizedKeyword, normalizedAuthor, normalizedTitle, normalizedTag, pageable);
            } else {
                postsPage = postRepository.searchPostsByTimeDesc(
                        normalizedKeyword, normalizedAuthor, normalizedTitle, normalizedTag, pageable);
            }
        }
        
        logger.debug("搜索结果：共 {} 条记录", postsPage.getTotalElements());
        
        return postsPage.map(post -> postMapper.toResponse(post, currentUser));
    }

    /**
     * 预处理搜索词
     * - 去除首尾空格
     * - 转义SQL特殊字符（%和_）以防止意外的模式匹配
     * - 移除URL中的协议前缀和常见标点符号，提升匹配效果
     * - 空字符串转换为null，让SQL查询可以正确处理
     */
    private String normalizeSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        
        String normalized = term.trim();
        
        // 转义SQL LIKE的特殊字符
        normalized = normalized.replace("\\", "\\\\");
        normalized = normalized.replace("%", "\\%");
        normalized = normalized.replace("_", "\\_");
        
        // 移除常见的URL协议前缀（如果用户搜索URL相关内容）
        normalized = normalized.replaceAll("^(https?://|www\\.)", "");
        
        // 移除首尾的常见标点符号（保留中间的）
        normalized = normalized.replaceAll("^[.,;:!?\"'`()\\[\\]{}]+", "");
        normalized = normalized.replaceAll("[.,;:!?\"'`()\\[\\]{}]+$", "");
        
        return normalized.isEmpty() ? null : normalized;
    }
}