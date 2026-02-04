package com.lost.blog.service;

import com.lost.blog.dto.NotificationResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.*;
import com.lost.blog.repository.NotificationRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + userDetails.getUsername()));
    }

    private List<NotificationType> getTypesForFilter(String filter) {
        switch (filter.toLowerCase()) {
            case "comments":
                return List.of(NotificationType.POST_COMMENTED, NotificationType.COMMENT_REPLIED);
            case "likes":
                return List.of(NotificationType.POST_LIKED, NotificationType.COMMENT_LIKED);
            case "follows":
                return List.of(NotificationType.FOLLOWED);
            case "messages":
                return List.of(NotificationType.MESSAGE_RECEIVED);
            case "system":
                return List.of(NotificationType.POST_APPROVED, NotificationType.POST_REJECTED, NotificationType.POST_DELETED);
            default:
                return null; // all types
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String filter, Pageable pageable, UserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        List<NotificationType> types = getTypesForFilter(filter);

        Page<Notification> notifications;
        if (types == null) {
            notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable);
        } else {
            notifications = notificationRepository.findByRecipientAndTypeInOrderByCreatedAtDesc(user, types, pageable);
        }

        return notifications.map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        return notificationRepository.countByRecipientAndReadAtIsNull(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications(UserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        List<Notification> notifications = notificationRepository.findTop10ByRecipientOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, UserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到通知: " + notificationId));

        // Check if the notification belongs to the current user
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权操作此通知");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public int markAllAsRead(String filter, UserDetails currentUser) {
        User user = getCurrentUser(currentUser);
        List<NotificationType> types = getTypesForFilter(filter);

        if (types == null) {
            return notificationRepository.markAllAsRead(user);
        } else {
            return notificationRepository.markAsReadByTypes(user, types);
        }
    }

    // --- 创建通知的方法 ---

    @Override
    @Transactional
    public void createPostLikedNotification(User actor, Post post) {
        User recipient = post.getUser();
        
        // Don't notify if actor is the author
        if (actor.getId().equals(recipient.getId())) {
            return;
        }

        // Check if notification already exists
        if (notificationRepository.existsByRecipientAndActorAndTypeAndPostAndComment(
                recipient, actor, NotificationType.POST_LIKED, post, null)) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.POST_LIKED);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setPost(post);
        notificationRepository.save(notification);

        logger.info("创建通知: {} 点赞了 {} 的文章 {}", actor.getUsername(), recipient.getUsername(), post.getId());
    }

    @Override
    @Transactional
    public void createPostCommentedNotification(User actor, Post post, Comment comment, String commentContent) {
        User recipient = post.getUser();
        
        // Don't notify if actor is the author
        if (actor.getId().equals(recipient.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.POST_COMMENTED);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setPost(post);
        notification.setComment(comment);
        notification.setContent(truncateContent(commentContent, 100));
        notificationRepository.save(notification);

        logger.info("创建通知: {} 评论了 {} 的文章 {}", actor.getUsername(), recipient.getUsername(), post.getId());
    }

    @Override
    @Transactional
    public void createFollowedNotification(User actor, User followed) {
        // Check if notification already exists
        if (notificationRepository.existsByRecipientAndActorAndTypeAndPostAndComment(
                followed, actor, NotificationType.FOLLOWED, null, null)) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.FOLLOWED);
        notification.setRecipient(followed);
        notification.setActor(actor);
        notificationRepository.save(notification);

        logger.info("创建通知: {} 关注了 {}", actor.getUsername(), followed.getUsername());
    }

    @Override
    @Transactional
    public void createCommentLikedNotification(User actor, Comment comment) {
        User recipient = comment.getUser();
        
        // Don't notify if actor is the comment author
        if (actor.getId().equals(recipient.getId())) {
            return;
        }

        // Check if notification already exists
        if (notificationRepository.existsByRecipientAndActorAndTypeAndPostAndComment(
                recipient, actor, NotificationType.COMMENT_LIKED, comment.getPost(), comment)) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.COMMENT_LIKED);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setPost(comment.getPost());
        notification.setComment(comment);
        notificationRepository.save(notification);

        logger.info("创建通知: {} 点赞了 {} 的评论 {}", actor.getUsername(), recipient.getUsername(), comment.getId());
    }

    @Override
    @Transactional
    public void createCommentRepliedNotification(User actor, Comment parentComment, Comment reply, String replyContent) {
        User recipient = parentComment.getUser();
        
        // Don't notify if actor is the parent comment author
        if (actor.getId().equals(recipient.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.COMMENT_REPLIED);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setPost(parentComment.getPost());
        notification.setComment(reply);
        notification.setContent(truncateContent(replyContent, 100));
        notificationRepository.save(notification);

        logger.info("创建通知: {} 回复了 {} 的评论 {}", actor.getUsername(), recipient.getUsername(), parentComment.getId());
    }

    @Override
    @Transactional
    public void createMessageReceivedNotification(User sender, User receiver) {
        // For messages, we create a new notification each time
        Notification notification = new Notification();
        notification.setType(NotificationType.MESSAGE_RECEIVED);
        notification.setRecipient(receiver);
        notification.setActor(sender);
        notificationRepository.save(notification);

        logger.info("创建通知: {} 给 {} 发送了私信", sender.getUsername(), receiver.getUsername());
    }

    @Override
    @Transactional
    public void createPostApprovedNotification(User admin, Post post) {
        User recipient = post.getUser();
        
        // 不需要给管理员自己发通知
        if (admin.getId().equals(recipient.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.POST_APPROVED);
        notification.setRecipient(recipient);
        notification.setActor(admin);
        notification.setPost(post);
        notification.setContent("您的文章「" + truncateContent(post.getTitle(), 50) + "」已通过审核");
        notificationRepository.save(notification);

        logger.info("创建通知: 管理员 {} 审核通过了 {} 的文章 {}", admin.getUsername(), recipient.getUsername(), post.getId());
    }

    @Override
    @Transactional
    public void createPostRejectedNotification(User admin, Post post, String reason) {
        User recipient = post.getUser();
        
        // 不需要给管理员自己发通知
        if (admin.getId().equals(recipient.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.POST_REJECTED);
        notification.setRecipient(recipient);
        notification.setActor(admin);
        notification.setPost(post);
        notification.setContent("您的文章「" + truncateContent(post.getTitle(), 30) + "」未通过审核: " + truncateContent(reason, 50));
        notificationRepository.save(notification);

        logger.info("创建通知: 管理员 {} 拒绝了 {} 的文章 {}", admin.getUsername(), recipient.getUsername(), post.getId());
    }

    @Override
    @Transactional
    public void createPostDeletedNotification(User admin, User author, String postTitle, String reason) {
        // 不需要给管理员自己发通知
        if (admin.getId().equals(author.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.POST_DELETED);
        notification.setRecipient(author);
        notification.setActor(admin);
        notification.setContent("您的文章「" + truncateContent(postTitle, 30) + "」因违规被删除: " + truncateContent(reason, 50));
        notificationRepository.save(notification);

        logger.info("创建通知: 管理员 {} 删除了 {} 的文章「{}」", admin.getUsername(), author.getUsername(), postTitle);
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return null;
        }
        // Remove markdown formatting for cleaner display
        String cleanContent = content.replaceAll("[#*`>\\[\\]()!]", "").trim();
        if (cleanContent.length() <= maxLength) {
            return cleanContent;
        }
        return cleanContent.substring(0, maxLength - 3) + "...";
    }
}
