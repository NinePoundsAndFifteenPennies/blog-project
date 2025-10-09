package com.lost.blog.service;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import com.lost.blog.repository.PostRepository;
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

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           PostMapper postMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postMapper = postMapper;
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

        Post savedPost = postRepository.save(post);
        logger.info("用户 {} 创建了新文章，ID: {}，是否草稿: {}",
                user.getUsername(), savedPost.getId(), savedPost.getDraft());

        return postMapper.toResponse(savedPost);
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

        return postMapper.toResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        // 只返回已发布的文章（draft = false）
        Page<Post> postsPage = postRepository.findByDraftFalse(pageable);
        logger.debug("查询已发布文章列表，页码: {}，数量: {}",
                pageable.getPageNumber(), postsPage.getTotalElements());
        // 调试：输出每篇文章的草稿状态
        postsPage.getContent().forEach(post -> 
            logger.debug("文章ID: {}, 标题: {}, 草稿状态: {}", 
                post.getId(), post.getTitle(), post.getDraft())
        );
        return postsPage.map(postMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(UserDetails currentUser, Pageable pageable) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));
        
        Page<Post> postsPage = postRepository.findByUser(user, pageable);
        logger.info("查询用户 {} 的所有文章（包括草稿），总数: {}",
                user.getUsername(), postsPage.getTotalElements());
        
        // 调试：输出每篇文章的草稿状态
        postsPage.getContent().forEach(post -> 
            logger.debug("用户文章 - ID: {}, 标题: {}, 草稿状态: {}", 
                post.getId(), post.getTitle(), post.getDraft())
        );
        
        return postsPage.map(postMapper::toResponse);
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

        Post updatedPost = postRepository.save(post);

        // 记录状态变更
        if (wasDraft && !willBeDraft) {
            logger.info("文章 {} 从草稿变为已发布", id);
        } else if (!wasDraft && willBeDraft) {
            logger.info("文章 {} 从已发布变为草稿", id);
        }

        return postMapper.toResponse(updatedPost);
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

        postRepository.delete(post);
        logger.info("用户 {} 删除了文章，ID: {}，标题: {}",
                currentUser.getUsername(), id, post.getTitle());
    }
}