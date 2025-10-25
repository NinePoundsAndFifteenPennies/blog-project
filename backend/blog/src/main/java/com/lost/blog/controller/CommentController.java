package com.lost.blog.controller;

import com.lost.blog.dto.CommentRequest;
import com.lost.blog.dto.CommentResponse;
import com.lost.blog.dto.LikeResponse;
import com.lost.blog.dto.ReplyRequest;
import com.lost.blog.service.CommentService;
import com.lost.blog.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "评论管理", description = "评论和回复的CRUD操作及点赞功能")
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    @Autowired
    public CommentController(CommentService commentService, LikeService likeService) {
        this.commentService = commentService;
        this.likeService = likeService;
    }

    // 在指定文章下创建评论
    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "创建评论", description = "在指定文章下创建新评论（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "评论创建成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest commentRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse comment = commentService.createComment(postId, commentRequest, currentUser);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    // 创建子评论（回复评论）
    @PostMapping("/comments/{commentId}/replies")
    @Operation(summary = "创建回复", description = "回复指定评论（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "回复创建成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "父评论不存在")
    })
    public ResponseEntity<CommentResponse> createReply(
            @PathVariable Long commentId,
            @Valid @RequestBody ReplyRequest replyRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse reply = commentService.createReply(commentId, replyRequest, currentUser);
        return new ResponseEntity<>(reply, HttpStatus.CREATED);
    }

    // 更新评论
    @PutMapping("/comments/{commentId}")
    @Operation(summary = "更新评论", description = "更新指定评论内容（只能作者修改）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "评论更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权修改该评论"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse comment = commentService.updateComment(commentId, commentRequest, currentUser);
        return ResponseEntity.ok(comment);
    }

    // 删除评论
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除评论", description = "删除指定评论（只能作者删除，级联删除子评论）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "评论删除成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权删除该评论"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok("评论删除成功");
    }

    // 获取文章的所有顶层评论（分页）
    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "获取文章评论", description = "分页获取指定文章的所有顶层评论")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取评论列表"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, pageable, currentUser);
        return ResponseEntity.ok(comments);
    }

    // 获取评论的所有回复（子评论）
    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "获取评论回复", description = "分页获取指定评论的所有回复")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取回复列表"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    public ResponseEntity<Page<CommentResponse>> getReplies(
            @PathVariable Long commentId,
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Page<CommentResponse> replies = commentService.getReplies(commentId, pageable, currentUser);
        return ResponseEntity.ok(replies);
    }

    // 获取当前用户的所有评论（分页）
    @GetMapping("/comments/my")
    @Operation(summary = "获取我的评论", description = "获取当前用户的所有评论（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取评论列表"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public ResponseEntity<Page<CommentResponse>> getMyComments(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getMyComments(currentUser, pageable);
        return ResponseEntity.ok(comments);
    }

    // 点赞评论
    @PostMapping("/comments/{commentId}/likes")
    @Operation(summary = "点赞评论", description = "给指定评论点赞（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    public ResponseEntity<LikeResponse> likeComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.likeComment(commentId, currentUser);
        boolean isLiked = true;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // 取消点赞评论
    @DeleteMapping("/comments/{commentId}/likes")
    @Operation(summary = "取消点赞评论", description = "取消对指定评论的点赞（需登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    public ResponseEntity<LikeResponse> unlikeComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.unlikeComment(commentId, currentUser);
        boolean isLiked = false;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // 获取评论的点赞信息
    @GetMapping("/comments/{commentId}/likes")
    @Operation(summary = "获取评论点赞信息", description = "获取评论的点赞数和当前用户点赞状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取点赞信息"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    public ResponseEntity<LikeResponse> getCommentLikeInfo(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.getCommentLikeCount(commentId);
        boolean isLiked = likeService.isCommentLikedByUser(commentId, currentUser);
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }
}
