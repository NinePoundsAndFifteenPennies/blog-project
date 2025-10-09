package com.lost.blog.controller;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 创建一篇新文章
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest,
                                                   @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse createdPost = postService.createPost(postRequest, currentUser);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // 获取一篇文章（传入 currentUser 用于草稿权限检查）
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse post = postService.getPostById(id, currentUser);
        return ResponseEntity.ok(post);
    }

    // 获取所有已发布的文章（分页）
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(Pageable pageable) {
        Page<PostResponse> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // 获取当前用户的所有文章（包括草稿）
    @GetMapping("/my")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<PostResponse> posts = postService.getMyPosts(currentUser, pageable);
        return ResponseEntity.ok(posts);
    }

    // 更新一篇文章
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest postRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse updatedPost = postService.updatePost(id, postRequest, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    // 删除一篇文章
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        postService.deletePost(id, currentUser);
        return ResponseEntity.ok("文章删除成功");
    }
}