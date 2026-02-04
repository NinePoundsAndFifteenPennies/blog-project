package com.lost.blog.controller;

import com.lost.blog.dto.AdminFormResponse;
import com.lost.blog.dto.NotificationResponse;
import com.lost.blog.model.AdminForm;
import com.lost.blog.model.Notification;
import com.lost.blog.model.NotificationType;
import com.lost.blog.model.User;
import com.lost.blog.repository.AdminFormRepository;
import com.lost.blog.repository.NotificationRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 * 提供通知相关的REST API
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    /** 表单查找时间窗口（分钟） - 用于在通知附近时间范围内查找关联的管理表单 */
    private static final int FORM_LOOKUP_WINDOW_MINUTES = 1;

    private final NotificationService notificationService;
    private final AdminFormRepository adminFormRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                  AdminFormRepository adminFormRepository,
                                  UserRepository userRepository,
                                  NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.adminFormRepository = adminFormRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
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

    /**
     * 根据通知ID获取关联的表单详情
     * 用于文章被删除后仍能查看拒绝/删除原因
     * GET /api/notifications/{notificationId}/form
     */
    @GetMapping("/{notificationId}/form")
    public ResponseEntity<AdminFormResponse> getFormByNotificationId(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户"));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到通知"));
        
        // 验证通知是属于当前用户的
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("无权访问此通知");
        }
        
        // 只处理拒绝和删除类型的通知
        if (notification.getType() != NotificationType.POST_REJECTED && 
            notification.getType() != NotificationType.POST_DELETED) {
            throw new ResourceNotFoundException("此通知类型无关联表单");
        }
        
        AdminForm form = null;
        
        // 首先尝试通过postId查找（如果文章还存在）
        if (notification.getPost() != null) {
            form = adminFormRepository.findFirstByTargetUserAndPostIdOrderByCreatedAtDesc(
                    user, notification.getPost().getId());
        }
        
        // 如果找不到，通过通知时间范围查找（文章已被删除的情况）
        if (form == null) {
            LocalDateTime notificationTime = notification.getCreatedAt();
            // 在通知创建前后指定时间窗口内查找匹配的表单
            LocalDateTime startTime = notificationTime.minusMinutes(FORM_LOOKUP_WINDOW_MINUTES);
            LocalDateTime endTime = notificationTime.plusMinutes(FORM_LOOKUP_WINDOW_MINUTES);
            
            List<AdminForm> forms = adminFormRepository
                    .findByTargetUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startTime, endTime);
            
            if (!forms.isEmpty()) {
                form = forms.get(0);
            }
        }
        
        if (form == null) {
            throw new ResourceNotFoundException("未找到关联的表单记录");
        }
        
        AdminFormResponse response = AdminFormResponse.fromEntity(form);
        // 隐藏管理员具体信息
        response.setAdminId(null);
        response.setAdminUsername(null);
        response.setAdminNickname(null);
        
        return ResponseEntity.ok(response);
    }
}
