package com.lost.blog.controller;

import com.lost.blog.dto.LikeResponse;
import com.lost.blog.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@Tag(name = "点赞管理", description = "文章点赞功能")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // Like a post
    @PostMapping
    @Operation(summary = "点赞文章", description = "给指定文章点赞（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.likePost(postId, currentUser);
        boolean isLiked = true;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // Unlike a post
    @DeleteMapping
    @Operation(summary = "取消点赞文章", description = "取消对指定文章的点赞（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<LikeResponse> unlikePost(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.unlikePost(postId, currentUser);
        boolean isLiked = false;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // Get like information for a post
    @GetMapping
    @Operation(summary = "获取文章点赞信息", description = "获取文章的点赞数和当前用户点赞状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取点赞信息"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<LikeResponse> getLikeInfo(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.getLikeCount(postId);
        boolean isLiked = likeService.isPostLikedByUser(postId, currentUser);
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }
}
