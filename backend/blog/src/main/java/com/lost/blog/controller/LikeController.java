package com.lost.blog.controller;

import com.lost.blog.dto.LikeResponse;
import com.lost.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // Like a post
    @PostMapping
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.likePost(postId, currentUser);
        boolean isLiked = true;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // Unlike a post
    @DeleteMapping
    public ResponseEntity<LikeResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.unlikePost(postId, currentUser);
        boolean isLiked = false;
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }

    // Get like information for a post
    @GetMapping
    public ResponseEntity<LikeResponse> getLikeInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails currentUser) {
        long likeCount = likeService.getLikeCount(postId);
        boolean isLiked = likeService.isPostLikedByUser(postId, currentUser);
        return ResponseEntity.ok(new LikeResponse(likeCount, isLiked));
    }
}
