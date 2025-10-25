package com.lost.blog.controller;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "文章管理", description = "文章的CRUD操作接口")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 创建一篇新文章
    @PostMapping
    @Operation(summary = "创建文章", description = "创建一篇新的文章（需要登录）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "文章创建成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "400", description = "参数验证失败")
    })
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest postRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse createdPost = postService.createPost(postRequest, currentUser);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // 获取一篇文章（传入 currentUser 用于草稿权限检查）
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情", description = "根据ID获取文章详情（草稿只能作者查看）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取文章"),
            @ApiResponse(responseCode = "404", description = "文章不存在"),
            @ApiResponse(responseCode = "403", description = "无权访问该草稿")
    })
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse post = postService.getPostById(id, currentUser);
        return ResponseEntity.ok(post);
    }

    // 获取所有已发布的文章（分页）
    @GetMapping
    @Operation(summary = "获取文章列表", description = "分页获取所有已发布的文章")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取文章列表")
    })
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        Page<PostResponse> posts = postService.getAllPosts(pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    // 获取当前用户的所有文章（包括草稿）
    @GetMapping("/my")
    @Operation(summary = "获取我的文章", description = "获取当前用户的所有文章（包括草稿）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取文章列表"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            Pageable pageable) {
        Page<PostResponse> posts = postService.getMyPosts(currentUser, pageable);
        return ResponseEntity.ok(posts);
    }

    // 更新一篇文章
    @PutMapping("/{id}")
    @Operation(summary = "更新文章", description = "更新指定ID的文章（只能作者修改）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "文章更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权修改该文章"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest postRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse updatedPost = postService.updatePost(id, postRequest, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    // 删除一篇文章
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章", description = "删除指定ID的文章（只能作者删除）",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "文章删除成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权删除该文章"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<String> deletePost(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        postService.deletePost(id, currentUser);
        return ResponseEntity.ok("文章删除成功");
    }
}