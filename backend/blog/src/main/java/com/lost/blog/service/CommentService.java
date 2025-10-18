package com.lost.blog.service;

import com.lost.blog.dto.CommentRequest;
import com.lost.blog.dto.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface CommentService {

    /**
     * 创建评论
     * @param postId 文章ID
     * @param commentRequest 评论请求数据
     * @param currentUser 当前登录用户
     * @return 创建的评论响应数据
     */
    CommentResponse createComment(Long postId, CommentRequest commentRequest, UserDetails currentUser);

    /**
     * 更新评论
     * @param commentId 评论ID
     * @param commentRequest 评论更新数据
     * @param currentUser 当前登录用户
     * @return 更新后的评论响应数据
     */
    CommentResponse updateComment(Long commentId, CommentRequest commentRequest, UserDetails currentUser);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param currentUser 当前登录用户
     */
    void deleteComment(Long commentId, UserDetails currentUser);

    /**
     * 获取文章的所有评论（分页）
     * @param postId 文章ID
     * @param pageable 分页信息
     * @param currentUser 当前登录用户（可为null，用于判断点赞状态）
     * @return 分页后的评论响应数据
     */
    Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable, UserDetails currentUser);

    /**
     * 获取当前用户的所有评论（分页）
     * @param currentUser 当前登录用户
     * @param pageable 分页信息
     * @return 分页后的评论响应数据
     */
    Page<CommentResponse> getMyComments(UserDetails currentUser, Pageable pageable);

    /**
     * 获取文章的评论数
     * @param postId 文章ID
     * @return 评论数量
     */
    long getCommentCount(Long postId);
}
