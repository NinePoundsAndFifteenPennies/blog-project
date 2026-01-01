package com.lost.blog.service;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import com.lost.blog.model.Tag;
import com.lost.blog.model.User;
import com.lost.blog.repository.PostRepository;
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

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;
    private final com.lost.blog.repository.CommentRepository commentRepository;
    private final com.lost.blog.repository.LikeRepository likeRepository;
    private final com.lost.blog.repository.CategoryRepository categoryRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           TagRepository tagRepository,
                           PostMapper postMapper,
                           com.lost.blog.repository.CommentRepository commentRepository,
                           com.lost.blog.repository.LikeRepository likeRepository,
                           com.lost.blog.repository.CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.postMapper = postMapper;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.categoryRepository = categoryRepository;
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
        post.setDraft(postRequest.getDraft() != null ? postRequest.getDraft() : false);
        post.setUser(user);

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
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id, UserDetails currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 草稿文章权限检查
        if (post.getDraft()) {
            // 如果是草稿，必须是作者本人才能查看
            if (currentUser == null) {
                logger.warn("匿名用户尝试访问草稿文章，ID: {}", id);
                throw new AccessDeniedException("草稿文章需要登录查看");
            }
            if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
                logger.warn("用户 {} 尝试访问他人草稿，文章ID: {}", currentUser.getUsername(), id);
                throw new AccessDeniedException("无权查看该草稿");
            }
        }

        return postMapper.toResponse(post, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable, UserDetails currentUser) {
        // 只返回已发布的文章（draft = false）
        Page<Post> postsPage = postRepository.findByDraftFalse(pageable);
        logger.debug("查询已发布文章列表，页码: {}，数量: {}",
                pageable.getPageNumber(), postsPage.getTotalElements());
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

        // 更新字段
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        post.setDraft(willBeDraft);

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
        
        // 只返回该用户已发布的文章（不包括草稿）
        Page<Post> postsPage = postRepository.findByUserAndDraftFalse(user, pageable);
        logger.debug("查询用户 {} 的已发布文章，总数: {}",
                username, postsPage.getTotalElements());
        
        return postsPage.map(post -> postMapper.toResponse(post, currentUser));
    }
}