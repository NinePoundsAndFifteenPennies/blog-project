package com.lost.blog.repository;

import com.lost.blog.model.PrivateMessage;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 私信消息数据访问接口
 */
@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    /**
     * 获取两个用户之间的所有消息（按时间升序）
     * @param user1 用户1
     * @param user2 用户2
     * @param pageable 分页参数
     * @return 消息列表
     */
    @Query("SELECT m FROM PrivateMessage m " +
           "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
           "   OR (m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.createdAt ASC")
    Page<PrivateMessage> findConversation(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

    /**
     * 检查是否存在发送者发给接收者的未被回复的消息
     * 用于防骚扰机制：非朋友关系下，A给B发消息后，在B回复前A不能再发消息
     * @param sender 发送者
     * @param receiver 接收者
     * @return 是否存在未回复的消息
     */
    @Query("SELECT COUNT(m) > 0 FROM PrivateMessage m " +
           "WHERE m.sender = :sender AND m.receiver = :receiver " +
           "AND NOT EXISTS (SELECT m2 FROM PrivateMessage m2 " +
           "                WHERE m2.sender = :receiver AND m2.receiver = :sender " +
           "                AND m2.createdAt > m.createdAt)")
    boolean existsUnrepliedMessage(@Param("sender") User sender, @Param("receiver") User receiver);

    /**
     * 统计用户未读消息数量
     * @param receiver 接收者
     * @return 未读消息数量
     */
    @Query("SELECT COUNT(m) FROM PrivateMessage m WHERE m.receiver = :receiver AND m.readAt IS NULL")
    long countUnreadMessages(@Param("receiver") User receiver);

    /**
     * 标记与某用户的所有消息为已读
     * @param receiver 接收者（当前用户）
     * @param sender 发送者
     */
    @Modifying
    @Query("UPDATE PrivateMessage m SET m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.receiver = :receiver AND m.sender = :sender AND m.readAt IS NULL")
    void markAsRead(@Param("receiver") User receiver, @Param("sender") User sender);

    /**
     * 获取用户的所有对话列表（每个对话只显示最新一条消息）
     * @param user 当前用户
     * @return 对话伙伴ID列表
     */
    @Query("SELECT DISTINCT CASE " +
           "  WHEN m.sender = :user THEN m.receiver.id " +
           "  ELSE m.sender.id END " +
           "FROM PrivateMessage m " +
           "WHERE m.sender = :user OR m.receiver = :user")
    List<Long> findConversationPartnerIds(@Param("user") User user);

    /**
     * 获取与某用户对话的最新一条消息
     * @param user1 用户1
     * @param user2 用户2
     * @return 最新消息
     */
    @Query("SELECT m FROM PrivateMessage m " +
           "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
           "   OR (m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    PrivateMessage findLatestMessage(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 统计与某用户的未读消息数
     * @param receiver 接收者
     * @param sender 发送者
     * @return 未读消息数
     */
    @Query("SELECT COUNT(m) FROM PrivateMessage m " +
           "WHERE m.receiver = :receiver AND m.sender = :sender AND m.readAt IS NULL")
    long countUnreadFromUser(@Param("receiver") User receiver, @Param("sender") User sender);
}
