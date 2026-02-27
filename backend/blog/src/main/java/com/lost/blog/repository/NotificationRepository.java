package com.lost.blog.repository;

import com.lost.blog.model.Notification;
import com.lost.blog.model.NotificationType;
import com.lost.blog.model.User;
import com.lost.blog.model.Post;
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
     * 获取用户的所有通知（分页，排除已删除）
     */
    Page<Notification> findByRecipientAndDeletedAtIsNullOrderByCreatedAtDesc(User recipient, Pageable pageable);

    /**
     * 获取用户特定类型的通知（分页，排除已删除）
     */
    Page<Notification> findByRecipientAndTypeInAndDeletedAtIsNullOrderByCreatedAtDesc(
            User recipient, List<NotificationType> types, Pageable pageable);

    /**
     * 获取用户未读通知数量（排除已删除）
     */
    long countByRecipientAndReadAtIsNullAndDeletedAtIsNull(User recipient);

    /**
     * 获取用户最近的通知（排除已删除）
     */
    List<Notification> findTop10ByRecipientAndDeletedAtIsNullOrderByCreatedAtDesc(User recipient);

    /**
     * 将用户所有未读通知标记为已读（排除已删除）
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.readAt IS NULL AND n.deletedAt IS NULL")
    int markAllAsRead(@Param("recipient") User recipient);

    /**
     * 将用户特定类型的未读通知标记为已读（排除已删除）
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.type IN :types AND n.readAt IS NULL AND n.deletedAt IS NULL")
    int markAsReadByTypes(@Param("recipient") User recipient, @Param("types") List<NotificationType> types);

    /**
     * 软删除：批量标记通知为已删除
     */
    @Modifying
    @Query("UPDATE Notification n SET n.deletedAt = CURRENT_TIMESTAMP WHERE n.id IN :ids AND n.recipient = :recipient AND n.deletedAt IS NULL")
    int softDeleteByIds(@Param("ids") List<Long> ids, @Param("recipient") User recipient);

    /**
     * 检查是否已存在相同的通知（防止重复通知）
     */
    boolean existsByRecipientAndActorAndTypeAndPostAndComment(
            User recipient, User actor, NotificationType type, 
            Post post, com.lost.blog.model.Comment comment);

    /**
     * 将指定文章的通知中的文章引用置空（用于删除文章前解除外键约束）
     */
    @Modifying
    @Query("UPDATE Notification n SET n.post = NULL WHERE n.post = :post")
    int nullifyPostReferences(@Param("post") Post post);

    /**
     * 将指定文章相关的所有通知中的评论和文章引用置空（用于删除文章及其评论前解除外键约束）
     */
    @Modifying
    @Query("UPDATE Notification n SET n.post = NULL, n.comment = NULL WHERE n.post = :post")
    int nullifyAllReferencesForPost(@Param("post") Post post);

    /**
     * 将指定评论的通知中的评论引用置空（用于删除评论前解除外键约束）
     */
    @Modifying
    @Query("UPDATE Notification n SET n.comment = NULL WHERE n.comment = :comment")
    int nullifyCommentReference(@Param("comment") com.lost.blog.model.Comment comment);

    /**
     * 将指定评论的所有后代评论（各层级子评论）的通知引用置空。
     * 由于JPQL不支持递归查询，此方法处理直接子评论。
     * 需要在服务层递归调用以处理所有层级。
     */
    @Modifying
    @Query("UPDATE Notification n SET n.comment = NULL WHERE n.comment.id IN " +
           "(SELECT c.id FROM Comment c WHERE c.parent = :parentComment)")
    int nullifyDirectChildCommentReferences(@Param("parentComment") com.lost.blog.model.Comment parentComment);
}
