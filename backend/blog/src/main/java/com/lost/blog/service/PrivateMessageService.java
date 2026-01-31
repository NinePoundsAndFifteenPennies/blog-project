package com.lost.blog.service;

import com.lost.blog.dto.ConversationResponse;
import com.lost.blog.dto.PrivateMessageRequest;
import com.lost.blog.dto.PrivateMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 私信服务接口
 * 提供私信功能的核心业务逻辑
 */
public interface PrivateMessageService {

    /**
     * 发送私信
     * @param receiverId 接收者ID
     * @param request 消息内容
     * @param currentUser 当前登录用户
     * @return 发送的消息
     */
    PrivateMessageResponse sendMessage(Long receiverId, PrivateMessageRequest request, UserDetails currentUser);

    /**
     * 获取与某用户的对话消息列表
     * @param partnerId 对话伙伴ID
     * @param pageable 分页参数
     * @param currentUser 当前登录用户
     * @return 消息列表
     */
    Page<PrivateMessageResponse> getConversation(Long partnerId, Pageable pageable, UserDetails currentUser);

    /**
     * 获取会话列表
     * @param currentUser 当前登录用户
     * @return 会话列表
     */
    List<ConversationResponse> getConversations(UserDetails currentUser);

    /**
     * 标记与某用户的消息为已读
     * @param partnerId 对话伙伴ID
     * @param currentUser 当前登录用户
     */
    void markAsRead(Long partnerId, UserDetails currentUser);

    /**
     * 获取未读消息总数
     * @param currentUser 当前登录用户
     * @return 未读消息数
     */
    long getUnreadCount(UserDetails currentUser);
    
    /**
     * 获取来自特定用户的未读消息数
     * @param senderId 发送者ID
     * @param currentUser 当前登录用户
     * @return 未读消息数
     */
    long getUnreadCountFromUser(Long senderId, UserDetails currentUser);

    /**
     * 检查是否可以给用户发送消息
     * @param receiverId 接收者ID
     * @param currentUser 当前登录用户
     * @return 是否可以发送
     */
    boolean canSendMessage(Long receiverId, UserDetails currentUser);
}
