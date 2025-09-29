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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper; // 注入Mapper

    // 最小改动：在构造函数中加入 PostMapper 参数并赋值
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
        // --- 新增的检查逻辑 ---
        if (postRepository.existsByTitle(postRequest.getTitle())) {
            throw new RuntimeException("错误：文章标题已存在！");
        }

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        post.setUser(user);

        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        return postMapper.toResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAll(pageable);
        return postsPage.map(postMapper::toResponse);
    }


    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest postRequest, UserDetails currentUser) {
        // 1. 根据ID从数据库中找到要更新的文章
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 2. --- 核心权限校验 ---
        // 检查文章的作者用户名是否和当前登录的用户名一致
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("无权修改该文章");
        }

        // 3. 更新文章内容
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        // `updatedAt` 字段会因为 @PreUpdate 注解自动更新

        // 4. 保存更新后的文章
        Post updatedPost = postRepository.save(post);

        // 5. 返回更新后的DTO
        return postMapper.toResponse(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id, UserDetails currentUser) {
        // 1. 找到文章
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 2. --- 同样的核心权限校验 ---
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("无权删除该文章");
        }

        // 3. 执行删除
        postRepository.delete(post);
    }
}


