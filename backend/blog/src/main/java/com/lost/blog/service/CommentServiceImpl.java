package com.lost.blog.service;

import com.lost.blog.dto.CommentRequest;
import com.lost.blog.dto.CommentResponse;
import com.lost.blog.exception.AccessDeniedException;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.CommentMapper;
import com.lost.blog.model.Comment;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                             PostRepository postRepository,
                             UserRepository userRepository,
                             CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest commentRequest, UserDetails currentUser) {
        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 获取文章
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章ID: " + postId));

        // 检查文章是否为草稿
        if (post.getDraft()) {
            logger.warn("用户 {} 尝试评论草稿文章ID: {}", user.getUsername(), postId);
            throw new AccessDeniedException("不能对草稿文章进行评论");
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setUser(user);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        logger.info("用户 {} 创建了评论ID: {} 在文章ID: {}", user.getUsername(), savedComment.getId(), postId);

        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest commentRequest, UserDetails currentUser) {
        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 获取评论
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论ID: " + commentId));

        // 检查权限：只有评论作者可以编辑
        if (!comment.getUser().getId().equals(user.getId())) {
            logger.warn("用户 {} 尝试编辑不属于自己的评论ID: {}", user.getUsername(), commentId);
            throw new AccessDeniedException("您没有权限编辑此评论");
        }

        // 更新评论内容
        comment.setContent(commentRequest.getContent());
        Comment updatedComment = commentRepository.save(comment);
        logger.info("用户 {} 更新了评论ID: {}", user.getUsername(), commentId);

        return commentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, UserDetails currentUser) {
        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 获取评论
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论ID: " + commentId));

        // 检查权限：评论作者或文章作者可以删除
        boolean isCommentAuthor = comment.getUser().getId().equals(user.getId());
        boolean isPostAuthor = comment.getPost().getUser().getId().equals(user.getId());

        if (!isCommentAuthor && !isPostAuthor) {
            logger.warn("用户 {} 尝试删除不属于自己的评论ID: {}", user.getUsername(), commentId);
            throw new AccessDeniedException("您没有权限删除此评论");
        }

        commentRepository.delete(comment);
        logger.info("用户 {} 删除了评论ID: {}", user.getUsername(), commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        // 获取文章
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章ID: " + postId));

        // 获取评论（按创建时间升序排列，最早的在前）
        Page<Comment> comments = commentRepository.findByPost(post, pageable);

        return comments.map(commentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getMyComments(UserDetails currentUser, Pageable pageable) {
        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 获取用户的所有评论
        Page<Comment> comments = commentRepository.findByUser(user, pageable);

        return comments.map(commentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章ID: " + postId));

        return commentRepository.countByPost(post);
    }
}
