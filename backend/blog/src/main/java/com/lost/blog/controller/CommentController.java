package com.lost.blog.controller;

import com.lost.blog.dto.CommentRequest;
import com.lost.blog.dto.CommentResponse;
import com.lost.blog.service.CommentService;
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

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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

    // 获取文章的所有评论（分页）
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    // 获取当前用户的所有评论（分页）
    @GetMapping("/comments/my")
    public ResponseEntity<Page<CommentResponse>> getMyComments(
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getMyComments(currentUser, pageable);
        return ResponseEntity.ok(comments);
    }
}
