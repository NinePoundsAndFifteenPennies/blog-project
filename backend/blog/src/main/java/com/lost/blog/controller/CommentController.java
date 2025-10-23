package com.lost.blog.controller;

import com.lost.blog.dto.CommentRequest;
import com.lost.blog.dto.CommentResponse;
import com.lost.blog.dto.LikeResponse;
import com.lost.blog.dto.ReplyRequest;
import com.lost.blog.service.CommentService;
import com.lost.blog.service.LikeService;
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
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse comment = commentService.createComment(postId, commentRequest, currentUser);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    // 创建子评论（回复评论）
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentResponse> createReply(
            @PathVariable Long commentId,
            @Valid @RequestBody ReplyRequest replyRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse reply = commentService.createReply(commentId, replyRequest, currentUser);
        return new ResponseEntity<>(reply, HttpStatus.CREATED);
    }

    // 更新评论
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse comment = commentService.updateComment(commentId, commentRequest, currentUser);
        return ResponseEntity.ok(comment);
    }

    // 删除评论
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails currentUser) {
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok("评论删除成功");
    }

    // 获取文章的所有顶层评论（分页）
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, pageable, currentUser);
        return ResponseEntity.ok(comments);
    }

    // 获取评论的所有回复（子评论）
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<Page<CommentResponse>> getReplies(
            @PathVariable Long commentId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<CommentResponse> replies = commentService.getReplies(commentId, pageable, currentUser);
        return ResponseEntity.ok(replies);
    }

    // 获取当前用户的所有评论（分页）
    @GetMapping("/comments/my")
    public ResponseEntity<Page<CommentResponse>> getMyComments(
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getMyComments(currentUser, pageable);
        return ResponseEntity.ok(comments);
    }

    // 点赞评论
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponse> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.likeComment(commentId, currentUser);
        boolean isLiked = true;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // 取消点赞评论
    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponse> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.unlikeComment(commentId, currentUser);
        boolean isLiked = false;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // 获取评论的点赞信息
    @GetMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponse> getCommentLikeInfo(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.getCommentLikeCount(commentId);
        boolean isLiked = likeService.isCommentLikedByUser(commentId, currentUser);
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }
}
