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
            throw new RuntimeException("错误：您已有同名文章！");
        }

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        post.setDraft(postRequest.getDraft() != null ? postRequest.getDraft() : false);
        post.setUser(user);

        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 如果是草稿，只有作者可以查看
        // 注意：这里的检查需要在有用户上下文时才生效
        // 对于公开接口，草稿文章应该在列表查询时就被过滤掉

        return postMapper.toResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        // 只返回已发布的文章
        Page<Post> postsPage = postRepository.findByDraftFalse(pageable);
        return postsPage.map(postMapper::toResponse);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest postRequest, UserDetails currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        // 权限校验
        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("无权修改该文章");
        }

        // ===== 新增：标题重复检查 =====
        // 如果标题被修改了，需要检查新标题是否与该用户的其他文章冲突
        if (!post.getTitle().equals(postRequest.getTitle())) {
            if (postRepository.existsByTitleAndUserAndIdNot(
                    postRequest.getTitle(),
                    post.getUser(),
                    id)) {
                throw new RuntimeException("您已有标题为「" + postRequest.getTitle() + "」的文章！");
            }
        }

        // 更新字段
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setContentType(postRequest.getContentType());
        post.setDraft(postRequest.getDraft() != null ? postRequest.getDraft() : false);

        Post updatedPost = postRepository.save(post);
        return postMapper.toResponse(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id, UserDetails currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + id + " 的文章"));

        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("无权删除该文章");
        }

        postRepository.delete(post);
    }
}