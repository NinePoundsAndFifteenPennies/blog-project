package com.lost.blog.repository;

import com.lost.blog.model.Notification;
import com.lost.blog.model.NotificationType;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 获取用户的所有通知（分页）
     */
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    /**
     * 获取用户特定类型的通知（分页）
     */
    Page<Notification> findByRecipientAndTypeInOrderByCreatedAtDesc(
            User recipient, List<NotificationType> types, Pageable pageable);

    /**
     * 获取用户未读通知数量
     */
    long countByRecipientAndReadAtIsNull(User recipient);

    /**
     * 获取用户特定类型的未读通知数量
     */
    long countByRecipientAndTypeInAndReadAtIsNull(User recipient, List<NotificationType> types);

    /**
     * 获取用户最近的通知
     */
    List<Notification> findTop10ByRecipientOrderByCreatedAtDesc(User recipient);

    /**
     * 将用户所有未读通知标记为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.readAt IS NULL")
    int markAllAsRead(@Param("recipient") User recipient);

    /**
     * 将用户特定类型的未读通知标记为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.type IN :types AND n.readAt IS NULL")
    int markAsReadByTypes(@Param("recipient") User recipient, @Param("types") List<NotificationType> types);

    /**
     * 检查是否已存在相同的通知（防止重复通知）
     */
    boolean existsByRecipientAndActorAndTypeAndPostAndComment(
            User recipient, User actor, NotificationType type, 
            com.lost.blog.model.Post post, com.lost.blog.model.Comment comment);
}
