package com.lost.blog.service;

import com.lost.blog.dto.NotificationResponse;
import com.lost.blog.dto.AdminFormResponse;
import com.lost.blog.model.Comment;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 获取用户的通知列表
     * @param filter 过滤器: "all", "comments", "likes", "follows"
     * @param pageable 分页参数
     * @param currentUser 当前用户
     * @return 通知列表
     */
    Page<NotificationResponse> getNotifications(String filter, Pageable pageable, UserDetails currentUser);

    /**
     * 获取用户的未读通知数量
     * @param currentUser 当前用户
     * @return 未读通知数量
     */
    long getUnreadCount(UserDetails currentUser);

    /**
     * 获取用户最近的通知（用于下拉预览）
     * @param currentUser 当前用户
     * @return 最近的通知列表
     */
    List<NotificationResponse> getRecentNotifications(UserDetails currentUser);

    /**
     * 标记单个通知为已读
     * @param notificationId 通知ID
     * @param currentUser 当前用户
     */
    void markAsRead(Long notificationId, UserDetails currentUser);

    /**
     * 标记所有通知为已读
     * @param filter 过滤器: "all", "comments", "likes", "follows"
     * @param currentUser 当前用户
     * @return 标记为已读的通知数量
     */
    int markAllAsRead(String filter, UserDetails currentUser);

    /**
     * 获取文章关联的管理表单
     * @param postId 文章ID
     * @param currentUser 当前用户
     * @return 表单详情
     */
    AdminFormResponse getFormByPostId(Long postId, UserDetails currentUser);

    /**
     * 根据通知ID获取关联的管理表单
     * @param notificationId 通知ID
     * @param currentUser 当前用户
     * @return 表单详情
     */
    AdminFormResponse getFormByNotificationId(Long notificationId, UserDetails currentUser);

    // --- 创建通知的方法 ---

    /**
     * 创建文章被点赞的通知
     */
    void createPostLikedNotification(User actor, Post post);

    /**
     * 创建文章被评论的通知
     */
    void createPostCommentedNotification(User actor, Post post, Comment comment, String commentContent);

    /**
     * 创建被关注的通知
     */
    void createFollowedNotification(User actor, User followed);

    /**
     * 创建评论被点赞的通知
     */
    void createCommentLikedNotification(User actor, Comment comment);

    /**
     * 创建评论被回复的通知
     */
    void createCommentRepliedNotification(User actor, Comment parentComment, Comment reply, String replyContent);

    /**
     * 创建收到私信的通知
     */
    void createMessageReceivedNotification(User sender, User receiver);

    /**
     * 创建文章审核通过的通知
     */
    void createPostApprovedNotification(User admin, Post post);

    /**
     * 创建文章审核拒绝的通知
     */
    void createPostRejectedNotification(User admin, Post post, String reason);

    /**
     * 创建文章被删除的通知
     */
    void createPostDeletedNotification(User admin, User author, String postTitle, String reason);

    /**
     * 创建评论被删除的通知
     */
    void createCommentDeletedNotification(User admin, User author, String postTitle, String commentPreview, String reason);

    /**
     * 创建标签被移除关联的通知（软删除）
     */
    void createTagRemovedNotification(User admin, User tagCreator, String tagName, String reason);

    /**
     * 创建标签被删除的通知（硬删除）
     */
    void createTagDeletedNotification(User admin, User tagCreator, String tagName, String reason);

    /**
     * 创建分类被删除的通知
     */
    void createCategoryDeletedNotification(User admin, User categoryCreator, String categoryName, String reason);
}
