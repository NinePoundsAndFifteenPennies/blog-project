package com.lost.blog.service;

import com.lost.blog.model.Comment;
import com.lost.blog.model.Like;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.LikeRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.UserRepository;
import com.lost.blog.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeServiceImpl implements LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeServiceImpl.class);

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public LikeServiceImpl(LikeRepository likeRepository,
                           PostRepository postRepository,
                           UserRepository userRepository,
                           CommentRepository commentRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public long likePost(Long postId, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + postId + " 的文章"));

        // Check if user has already liked this post
        if (likeRepository.existsByUserAndPost(user, post)) {
            logger.info("用户 {} 已经点赞过文章 {}", user.getUsername(), postId);
            // Return current like count without creating duplicate
            return likeRepository.countByPost(post);
        }

        // Create new like
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);

        long likeCount = likeRepository.countByPost(post);
        logger.info("用户 {} 点赞了文章 {}，当前点赞数: {}", user.getUsername(), postId, likeCount);

        return likeCount;
    }

    @Override
    @Transactional
    public long unlikePost(Long postId, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + postId + " 的文章"));

        // Check if user has liked this post
        if (!likeRepository.existsByUserAndPost(user, post)) {
            logger.info("用户 {} 未点赞过文章 {}，无需取消", user.getUsername(), postId);
            // Return current like count
            return likeRepository.countByPost(post);
        }

        // Delete the like
        likeRepository.deleteByUserAndPost(user, post);

        long likeCount = likeRepository.countByPost(post);
        logger.info("用户 {} 取消点赞文章 {}，当前点赞数: {}", user.getUsername(), postId, likeCount);

        return likeCount;
    }

    @Override
    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + postId + " 的文章"));

        return likeRepository.countByPost(post);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Long postId, UserDetails currentUser) {
        if (currentUser == null) {
            return false;
        }

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElse(null);

        if (user == null) {
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + postId + " 的文章"));

        return likeRepository.existsByUserAndPost(user, post);
    }

    @Override
    @Transactional
    public long likeComment(Long commentId, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + commentId + " 的评论"));

        // Check if user has already liked this comment
        if (likeRepository.existsByUserAndComment(user, comment)) {
            logger.info("用户 {} 已经点赞过评论 {}", user.getUsername(), commentId);
            // Return current like count without creating duplicate
            return likeRepository.countByComment(comment);
        }

        // Create new like
        Like like = new Like();
        like.setUser(user);
        like.setComment(comment);
        likeRepository.save(like);

        long likeCount = likeRepository.countByComment(comment);
        logger.info("用户 {} 点赞了评论 {}，当前点赞数: {}", user.getUsername(), commentId, likeCount);

        return likeCount;
    }

    @Override
    @Transactional
    public long unlikeComment(Long commentId, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + commentId + " 的评论"));

        // Check if user has liked this comment
        if (!likeRepository.existsByUserAndComment(user, comment)) {
            logger.info("用户 {} 未点赞过评论 {}，无需取消", user.getUsername(), commentId);
            // Return current like count
            return likeRepository.countByComment(comment);
        }

        // Delete the like
        likeRepository.deleteByUserAndComment(user, comment);

        long likeCount = likeRepository.countByComment(comment);
        logger.info("用户 {} 取消点赞评论 {}，当前点赞数: {}", user.getUsername(), commentId, likeCount);

        return likeCount;
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentLikeCount(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + commentId + " 的评论"));

        return likeRepository.countByComment(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCommentLikedByUser(Long commentId, UserDetails currentUser) {
        if (currentUser == null) {
            return false;
        }

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElse(null);

        if (user == null) {
            return false;
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + commentId + " 的评论"));

        return likeRepository.existsByUserAndComment(user, comment);
    }
}
