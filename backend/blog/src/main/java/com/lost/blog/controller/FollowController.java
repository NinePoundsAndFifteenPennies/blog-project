package com.lost.blog.controller;

import com.lost.blog.dto.FollowResponse;
import com.lost.blog.dto.FollowStatsResponse;
import com.lost.blog.dto.FollowUserResponse;
import com.lost.blog.dto.FollowVisibilityRequest;
import com.lost.blog.dto.FollowVisibilityResponse;
import com.lost.blog.service.FollowService;
import com.lost.blog.service.FollowVisibilityService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 关注功能控制器
 * 
 * API 设计：
 * - POST   /api/users/{userId}/follow              - 关注用户
 * - DELETE /api/users/{userId}/follow              - 取消关注
 * - GET    /api/users/{userId}/follow/stats        - 获取关注统计
 * - GET    /api/users/{userId}/following           - 获取关注列表
 * - GET    /api/users/{userId}/followers           - 获取粉丝列表
 * - GET    /api/users/{userId}/friends             - 获取朋友列表
 * - GET    /api/follow/visibility                  - 获取当前用户的可见性设置
 * - PUT    /api/follow/visibility                  - 更新当前用户的可见性设置
 */
@RestController
@Validated
public class FollowController {

    private static final int MAX_PAGE_SIZE = 100;

    private final FollowService followService;
    private final FollowVisibilityService visibilityService;

    @Autowired
    public FollowController(FollowService followService, FollowVisibilityService visibilityService) {
        this.followService = followService;
        this.visibilityService = visibilityService;
    }

    /**
     * 关注用户
     * @param userId 要关注的用户ID
     * @param currentUser 当前登录用户
     * @return 关注操作结果
     */
    @PostMapping("/api/users/{userId}/follow")
    public ResponseEntity<FollowResponse> followUser(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {
        FollowResponse response = followService.followUser(userId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 取消关注
     * @param userId 要取消关注的用户ID
     * @param currentUser 当前登录用户
     * @return 取消关注操作结果
     */
    @DeleteMapping("/api/users/{userId}/follow")
    public ResponseEntity<FollowResponse> unfollowUser(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {
        FollowResponse response = followService.unfollowUser(userId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的关注统计信息
     * @param userId 用户ID
     * @param currentUser 当前登录用户（可选，用于判断关注关系和可见性）
     * @return 关注统计信息（如果无权查看，计数会返回-1）
     */
    @GetMapping("/api/users/{userId}/follow/stats")
    public ResponseEntity<FollowStatsResponse> getFollowStats(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {
        FollowStatsResponse stats = followService.getFollowStats(userId, currentUser);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取用户的关注列表（该用户关注了谁）
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param size 每页数量（1-100）
     * @param currentUser 当前登录用户（可选，用于判断朋友关系和可见性）
     * @return 关注列表（如果无权查看，返回空列表）
     */
    @GetMapping("/api/users/{userId}/following")
    public ResponseEntity<Page<FollowUserResponse>> getFollowingList(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserDetails currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowUserResponse> followingList = followService.getFollowingList(userId, pageable, currentUser);
        return ResponseEntity.ok(followingList);
    }

    /**
     * 获取用户的粉丝列表（谁关注了该用户）
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param size 每页数量（1-100）
     * @param currentUser 当前登录用户（可选，用于判断朋友关系和可见性）
     * @return 粉丝列表（如果无权查看，返回空列表）
     */
    @GetMapping("/api/users/{userId}/followers")
    public ResponseEntity<Page<FollowUserResponse>> getFollowerList(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserDetails currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowUserResponse> followerList = followService.getFollowerList(userId, pageable, currentUser);
        return ResponseEntity.ok(followerList);
    }

    /**
     * 获取用户的朋友列表（互相关注的用户）
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param size 每页数量（1-100）
     * @param currentUser 当前登录用户（可选，用于可见性检查）
     * @return 朋友列表（如果无权查看，返回空列表）
     */
    @GetMapping("/api/users/{userId}/friends")
    public ResponseEntity<Page<FollowUserResponse>> getFriendList(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserDetails currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowUserResponse> friendList = followService.getFriendList(userId, pageable, currentUser);
        return ResponseEntity.ok(friendList);
    }

    // ========== 可见性设置相关接口 ==========

    /**
     * 获取当前用户的关注可见性设置
     * @param currentUser 当前登录用户
     * @return 可见性设置
     */
    @GetMapping("/api/follow/visibility")
    public ResponseEntity<FollowVisibilityResponse> getVisibilitySettings(
            @AuthenticationPrincipal UserDetails currentUser) {
        FollowVisibilityResponse response = visibilityService.getVisibilitySettings(currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新当前用户的关注可见性设置
     * @param request 可见性设置请求
     * @param currentUser 当前登录用户
     * @return 更新后的可见性设置
     */
    @PutMapping("/api/follow/visibility")
    public ResponseEntity<FollowVisibilityResponse> updateVisibilitySettings(
            @RequestBody FollowVisibilityRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        FollowVisibilityResponse response = visibilityService.updateVisibilitySettings(request, currentUser);
        return ResponseEntity.ok(response);
    }
}
