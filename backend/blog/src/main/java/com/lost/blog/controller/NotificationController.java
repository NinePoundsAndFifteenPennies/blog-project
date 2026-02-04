package com.lost.blog.controller;

import com.lost.blog.dto.AdminFormResponse;
import com.lost.blog.dto.NotificationResponse;
import com.lost.blog.model.AdminForm;
import com.lost.blog.model.User;
import com.lost.blog.repository.AdminFormRepository;
import com.lost.blog.repository.UserRepository;
import com.lost.blog.service.NotificationService;
import com.lost.blog.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 * 提供通知相关的REST API
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AdminFormRepository adminFormRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                  AdminFormRepository adminFormRepository,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.adminFormRepository = adminFormRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取通知列表
     * GET /api/notifications?filter=all|comments|likes|follows
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "all") String filter,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<NotificationResponse> notifications = notificationService.getNotifications(filter, pageable, currentUser);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 获取最近的通知（用于下拉预览）
     * GET /api/notifications/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NotificationResponse>> getRecentNotifications(
            @AuthenticationPrincipal UserDetails currentUser) {
        List<NotificationResponse> notifications = notificationService.getRecentNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 获取未读通知数量
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails currentUser) {
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 标记单个通知为已读
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * 标记所有通知为已读
     * PUT /api/notifications/read?filter=all|comments|likes|follows
     */
    @PutMapping("/read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @RequestParam(defaultValue = "all") String filter,
            @AuthenticationPrincipal UserDetails currentUser) {
        int count = notificationService.markAllAsRead(filter, currentUser);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }

    /**
     * 获取用户文章的拒绝/删除表单详情
     * 用于用户查看自己文章被拒绝或删除的原因
     * GET /api/notifications/forms/post/{postId}
     */
    @GetMapping("/forms/post/{postId}")
    public ResponseEntity<AdminFormResponse> getFormByPostId(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户"));
        
        AdminForm form = adminFormRepository.findFirstByTargetUserAndPostIdOrderByCreatedAtDesc(user, postId);
        if (form == null) {
            throw new ResourceNotFoundException("未找到该文章的表单记录");
        }
        
        AdminFormResponse response = AdminFormResponse.fromEntity(form);
        // 隐藏管理员具体信息
        response.setAdminId(null);
        response.setAdminUsername(null);
        response.setAdminNickname(null);
        
        return ResponseEntity.ok(response);
    }
}
