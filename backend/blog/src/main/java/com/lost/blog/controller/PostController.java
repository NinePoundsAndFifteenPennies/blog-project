package com.lost.blog.controller;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
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
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest request) {
        // 获取 IP 地址（优先使用 X-Forwarded-For 头，适配 Nginx 代理）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // 获取设备信息
        String userAgent = request.getHeader("User-Agent");
        
        // 获取来源URL（用于流量来源分析）
        String referer = request.getHeader("Referer");

        PostResponse post = postService.getPostById(id, currentUser, ip, userAgent, referer);
        return ResponseEntity.ok(post);
    }

    // 获取所有已发布的文章（分页）
    // sortBy: "time" (按时间排序，默认) 或 "hotness" (按热度排序)
    // order: "desc" (降序，默认) 或 "asc" (升序)
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(required = false, defaultValue = "time") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<PostResponse> posts = postService.getAllPosts(sortBy, order, pageable, currentUser);
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

    // 从文章中移除指定标签（软删除）
    @DeleteMapping("/{id}/tags/{tagName}")
    public ResponseEntity<PostResponse> removeTagFromPost(
            @PathVariable Long id,
            @PathVariable String tagName,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse updatedPost = postService.removeTagFromPost(id, tagName, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    // 从文章中移除分类（软删除）
    @DeleteMapping("/{id}/category")
    public ResponseEntity<PostResponse> removeCategoryFromPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponse updatedPost = postService.removeCategoryFromPost(id, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    // 获取指定用户的已发布文章（公开接口）
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<PostResponse>> getPostsByUsername(
            @PathVariable String username,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<PostResponse> posts = postService.getPostsByUsername(username, pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    /**
     * 搜索文章
     * 支持多维度搜索：关键词（标题+内容）、作者、标题、标签
     * 搜索结果支持按时间或热度排序
     * 
     * @param keyword 通用关键词，匹配标题和内容
     * @param author 作者用户名或昵称（模糊匹配）
     * @param title 标题关键词（模糊匹配）
     * @param tag 标签名称（模糊匹配）
     * @param sortBy 排序方式：time（默认）或 hotness
     * @param order 排序顺序：desc（默认）或 asc
     * @param pageable 分页参数
     * @param currentUser 当前登录用户（用于判断点赞状态）
     * @return 分页搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, defaultValue = "time") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<PostResponse> posts = postService.searchPosts(
                keyword, author, title, tag, sortBy, order, pageable, currentUser);
        return ResponseEntity.ok(posts);
    }
}