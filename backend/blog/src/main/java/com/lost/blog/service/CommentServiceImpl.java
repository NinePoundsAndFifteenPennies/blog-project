package com.lost.blog.service;

import com.lost.blog.dto.CommentRequest;
import com.lost.blog.dto.CommentResponse;
import com.lost.blog.dto.ReplyRequest;
import com.lost.blog.exception.AccessDeniedException;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.CommentMapper;
import com.lost.blog.model.Comment;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.LikeRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Value("${comment.max-nesting-level:3}")
    private int maxNestingLevel;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final LikeService likeService;
    private final LikeRepository likeRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                             PostRepository postRepository,
                             UserRepository userRepository,
                             CommentMapper commentMapper,
                             LikeService likeService,
                             LikeRepository likeRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.likeService = likeService;
        this.likeRepository = likeRepository;
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

        CommentResponse response = commentMapper.toResponse(savedComment);
        response.setLikeCount(0);  // New comment has no likes
        response.setLiked(false);
        return response;
    }

    @Override
    @Transactional
    public CommentResponse createReply(Long commentId, ReplyRequest replyRequest, UserDetails currentUser) {
        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 获取父评论
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论ID: " + commentId));

        // 检查父评论所属文章是否为草稿
        if (parentComment.getPost().getDraft()) {
            logger.warn("用户 {} 尝试回复草稿文章的评论ID: {}", user.getUsername(), commentId);
            throw new AccessDeniedException("不能回复草稿文章的评论");
        }

        // 计算回复层级
        int newLevel = (parentComment.getLevel() != null ? parentComment.getLevel() : 0) + 1;

        // 检查层级限制
        if (newLevel > maxNestingLevel) {
            logger.warn("用户 {} 尝试创建超过最大层级的回复，当前层级: {}", user.getUsername(), newLevel);
            throw new AccessDeniedException("回复层级过深，无法继续回复（最大层级: " + maxNestingLevel + "）");
        }

        // 验证replyToUserId或replyToUsername（如果提供）
        User replyToUser = null;
        if (replyRequest.getReplyToUserId() != null) {
            replyToUser = userRepository.findById(replyRequest.getReplyToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("未找到被回复的用户ID: " + replyRequest.getReplyToUserId()));
        } else if (replyRequest.getReplyToUsername() != null && !replyRequest.getReplyToUsername().isEmpty()) {
            // If userId not provided but username is, look up by username
            replyToUser = userRepository.findByUsername(replyRequest.getReplyToUsername())
                    .orElse(null);  // Don't throw error if username not found, just set to null
        }

        // 创建子评论
        Comment reply = new Comment();
        reply.setContent(replyRequest.getContent());
        reply.setUser(user);
        reply.setPost(parentComment.getPost()); // 继承父评论的文章
        reply.setParent(parentComment);
        reply.setReplyToUser(replyToUser);
        reply.setLevel(newLevel);

        Comment savedReply = commentRepository.save(reply);
        logger.info("用户 {} 创建了子评论ID: {} 回复评论ID: {}", user.getUsername(), savedReply.getId(), commentId);

        CommentResponse response = commentMapper.toResponse(savedReply);
        response.setLikeCount(0);  // New reply has no likes
        response.setLiked(false);
        return response;
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

        CommentResponse response = commentMapper.toResponse(updatedComment);
        response.setLikeCount(likeService.getCommentLikeCount(commentId));
        response.setLiked(likeService.isCommentLikedByUser(commentId, currentUser));
        return response;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, UserDetails currentUser) {
        // 1. 获取要删除的评论实体
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论ID: " + commentId));

        // 2. 验证用户权限 (逻辑保持不变)
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        boolean isCommentAuthor = comment.getUser().getId().equals(user.getId());
        boolean isPostAuthor = comment.getPost().getUser().getId().equals(user.getId());

        if (!isCommentAuthor && !isPostAuthor) {
            logger.warn("用户 {} 尝试删除不属于自己的评论ID: {}", user.getUsername(), commentId);
            throw new AccessDeniedException("您没有权限删除此评论");
        }

        // 3. 直接删除该评论
        // 因为 Comment 实体中已正确配置了 cascade = CascadeType.ALL,
        // JPA/Hibernate 现在知道当这个 comment 被删除时，
        // 必须先删除所有引用它的 likes 和 replies (子评论)。
        // 整条删除链会由JPA自动、安全、正确地执行。
        commentRepository.delete(comment);

        logger.info("用户 {} 成功删除评论ID: {}。JPA将根据实体定义自动处理所有级联删除。", user.getUsername(), commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable, UserDetails currentUser) {
        // 获取文章
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章ID: " + postId));

        // 获取顶层评论（parent_id为null，按创建时间升序排列）
        Page<Comment> comments = commentRepository.findByPostAndParentIsNull(post, pageable);

        return comments.map(comment -> {
            CommentResponse response = commentMapper.toResponse(comment);
            // Set like count and user's like status
            response.setLikeCount(likeService.getCommentLikeCount(comment.getId()));
            response.setLiked(likeService.isCommentLikedByUser(comment.getId(), currentUser));
            // Set reply count (recursively count all descendants)
            response.setReplyCount(countRepliesRecursively(comment));
            return response;
        });
    }

    /**
     * 递归统计评论的所有子孙评论数量
     */
    private long countRepliesRecursively(Comment comment) {
        long count = 0;
        List<Comment> children = commentRepository.findByParent(comment);
        count += children.size();
        for (Comment child : children) {
            count += countRepliesRecursively(child);
        }
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getReplies(Long commentId, Pageable pageable, UserDetails currentUser) {
        // 获取父评论
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论ID: " + commentId));

        // 获取直接子评论（按创建时间升序排列）
        Page<Comment> replies = commentRepository.findByParent(parentComment, pageable);

        return replies.map(reply -> {
            CommentResponse response = commentMapper.toResponse(reply);
            // Set like count and user's like status
            response.setLikeCount(likeService.getCommentLikeCount(reply.getId()));
            response.setLiked(likeService.isCommentLikedByUser(reply.getId(), currentUser));
            // Sub-comments don't need replyCount in the response (or set to 0)
            response.setReplyCount(0L);
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getMyComments(UserDetails currentUser, Pageable pageable) {
        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        // 获取用户的所有评论
        Page<Comment> comments = commentRepository.findByUser(user, pageable);

        return comments.map(comment -> {
            CommentResponse response = commentMapper.toResponse(comment);
            // Set like count and user's like status
            response.setLikeCount(likeService.getCommentLikeCount(comment.getId()));
            response.setLiked(likeService.isCommentLikedByUser(comment.getId(), currentUser));
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到文章ID: " + postId));

        return commentRepository.countByPost(post);
    }
}
