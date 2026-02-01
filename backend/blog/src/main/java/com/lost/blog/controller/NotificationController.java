package com.lost.blog.controller;

import com.lost.blog.dto.NotificationResponse;
import com.lost.blog.service.NotificationService;
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

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
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
}
