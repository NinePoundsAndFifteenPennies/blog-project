package com.lost.blog.service;

import com.lost.blog.dto.ConversationResponse;
import com.lost.blog.dto.PrivateMessageRequest;
import com.lost.blog.dto.PrivateMessageResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.PrivateMessage;
import com.lost.blog.model.User;
import com.lost.blog.repository.FollowRepository;
import com.lost.blog.repository.PrivateMessageRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 私信服务实现类
 * 
 * 设计说明：
 * - 防骚扰机制：非朋友关系下，A发送给B一条消息后，在B回复前A无法再发消息
 * - 朋友（互相关注）可以无限制发送消息
 * - 支持已读状态跟踪
 */
@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageServiceImpl.class);

    private final PrivateMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Autowired
    public PrivateMessageServiceImpl(PrivateMessageRepository messageRepository,
                                      UserRepository userRepository,
                                      FollowRepository followRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Override
    @Transactional
    public PrivateMessageResponse sendMessage(Long receiverId, PrivateMessageRequest request, UserDetails currentUser) {
        User sender = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + receiverId + " 的用户"));

        // 防止给自己发消息
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("不能给自己发送私信");
        }

        // 检查是否可以发送消息（防骚扰机制）
        if (!canSendMessageInternal(sender, receiver)) {
            throw new IllegalStateException("在对方回复前，您无法发送第二条消息");
        }

        // 创建消息
        PrivateMessage message = new PrivateMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        message = messageRepository.save(message);

        logger.info("用户 {} 给用户 {} 发送了一条私信", sender.getUsername(), receiver.getUsername());

        return toResponse(message, sender.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrivateMessageResponse> getConversation(Long partnerId, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + partnerId + " 的用户"));

        Page<PrivateMessage> messages = messageRepository.findConversation(user, partner, pageable);

        return messages.map(msg -> toResponse(msg, user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        List<Long> partnerIds = messageRepository.findConversationPartnerIds(user);
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) continue;

            PrivateMessage lastMessage = messageRepository.findLatestMessage(user, partner);
            if (lastMessage == null) continue;

            long unreadCount = messageRepository.countUnreadFromUser(user, partner);
            boolean isFriend = isFriends(user, partner);

            ConversationResponse response = new ConversationResponse();
            response.setPartnerId(partner.getId());
            response.setPartnerUsername(partner.getUsername());
            response.setPartnerNickname(partner.getNickname());
            response.setPartnerAvatarUrl(partner.getAvatarUrl());
            response.setLastMessageContent(truncateContent(lastMessage.getContent()));
            response.setLastMessageTime(lastMessage.getCreatedAt());
            response.setLastMessageSentByMe(lastMessage.getSender().getId().equals(user.getId()));
            response.setUnreadCount(unreadCount);
            response.setFriend(isFriend);

            conversations.add(response);
        }

        // 按最后消息时间排序（最新的在前）
        conversations.sort(Comparator.comparing(ConversationResponse::getLastMessageTime).reversed());

        return conversations;
    }

    @Override
    @Transactional
    public void markAsRead(Long partnerId, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + partnerId + " 的用户"));

        messageRepository.markAsRead(user, partner);
        logger.debug("用户 {} 将来自用户 {} 的消息标记为已读", user.getUsername(), partner.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        return messageRepository.countUnreadMessages(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCountFromUser(Long senderId, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + senderId + " 的用户"));

        return messageRepository.countUnreadFromUser(user, sender);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSendMessage(Long receiverId, UserDetails currentUser) {
        User sender = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        if (sender == null) return false;

        User receiver = userRepository.findById(receiverId).orElse(null);
        if (receiver == null) return false;

        if (sender.getId().equals(receiver.getId())) return false;

        return canSendMessageInternal(sender, receiver);
    }

    /**
     * 检查是否可以发送消息（内部方法）
     * 防骚扰机制：非朋友关系下，A发送给B一条消息后，在B回复前A无法再发消息
     */
    private boolean canSendMessageInternal(User sender, User receiver) {
        // 如果是朋友（互相关注），可以无限制发送
        if (isFriends(sender, receiver)) {
            return true;
        }

        // 非朋友关系：检查是否有未回复的消息
        return !messageRepository.existsUnrepliedMessage(sender, receiver);
    }

    /**
     * 检查两个用户是否是朋友（互相关注）
     */
    private boolean isFriends(User user1, User user2) {
        return followRepository.existsByFollowerAndFollowed(user1, user2)
            && followRepository.existsByFollowerAndFollowed(user2, user1);
    }

    /**
     * 截断消息内容用于预览
     */
    private String truncateContent(String content) {
        if (content == null) return "";
        if (content.length() <= 50) return content;
        return content.substring(0, 50) + "...";
    }

    /**
     * 将实体转换为响应DTO
     */
    private PrivateMessageResponse toResponse(PrivateMessage message, Long currentUserId) {
        PrivateMessageResponse response = new PrivateMessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderUsername(message.getSender().getUsername());
        response.setSenderNickname(message.getSender().getNickname());
        response.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        response.setReceiverId(message.getReceiver().getId());
        response.setReceiverUsername(message.getReceiver().getUsername());
        response.setReceiverNickname(message.getReceiver().getNickname());
        response.setReceiverAvatarUrl(message.getReceiver().getAvatarUrl());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        response.setReadAt(message.getReadAt());
        response.setRead(message.isRead());
        response.setSentByMe(message.getSender().getId().equals(currentUserId));
        return response;
    }
}
