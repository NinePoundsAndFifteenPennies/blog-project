package com.lost.blog.controller;

import com.lost.blog.dto.ConversationResponse;
import com.lost.blog.dto.PrivateMessageRequest;
import com.lost.blog.dto.PrivateMessageResponse;
import com.lost.blog.service.PrivateMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 私信控制器
 * 提供私信相关的REST API
 */
@RestController
@RequestMapping("/api/messages")
public class PrivateMessageController {

    private final PrivateMessageService messageService;

    @Autowired
    public PrivateMessageController(PrivateMessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 发送私信
     * POST /api/messages/{receiverId}
     */
    @PostMapping("/{receiverId}")
    public ResponseEntity<PrivateMessageResponse> sendMessage(
            @PathVariable Long receiverId,
            @Valid @RequestBody PrivateMessageRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        PrivateMessageResponse message = messageService.sendMessage(receiverId, request, currentUser);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    /**
     * 获取与某用户的对话消息
     * GET /api/messages/conversation/{partnerId}
     */
    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<Page<PrivateMessageResponse>> getConversation(
            @PathVariable Long partnerId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<PrivateMessageResponse> messages = messageService.getConversation(partnerId, pageable, currentUser);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取所有会话列表
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal UserDetails currentUser) {
        List<ConversationResponse> conversations = messageService.getConversations(currentUser);
        return ResponseEntity.ok(conversations);
    }

    /**
     * 标记与某用户的消息为已读
     * PUT /api/messages/read/{partnerId}
     */
    @PutMapping("/read/{partnerId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal UserDetails currentUser) {
        messageService.markAsRead(partnerId, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取未读消息数
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails currentUser) {
        long count = messageService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 检查是否可以给用户发送消息
     * GET /api/messages/can-send/{receiverId}
     */
    @GetMapping("/can-send/{receiverId}")
    public ResponseEntity<Map<String, Boolean>> canSendMessage(
            @PathVariable Long receiverId,
            @AuthenticationPrincipal UserDetails currentUser) {
        boolean canSend = messageService.canSendMessage(receiverId, currentUser);
        return ResponseEntity.ok(Map.of("canSend", canSend));
    }
}
