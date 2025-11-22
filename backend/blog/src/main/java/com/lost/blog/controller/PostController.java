package com.lost.blog.controller;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import com.lost.blog.service.ExportService;
import com.lost.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final ExportService exportService;

    @Autowired
    public PostController(PostService postService, ExportService exportService) {
        this.postService = postService;
        this.exportService = exportService;
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
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        Page<PostResponse> posts = postService.getAllPosts(pageable, currentUser);
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

    // 导出文章为Markdown格式（需要登录）
    @GetMapping("/{id}/export/markdown")
    public ResponseEntity<Resource> exportAsMarkdown(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("需要登录才能导出文章");
        }
        
        Post post = postService.getPostEntityById(id);
        
        ByteArrayResource resource = exportService.exportAsMarkdown(post);
        String filename = encodeFilename(post.getTitle() + ".md");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/markdown"))
                .body(resource);
    }

    // 导出文章为PDF格式（需要登录）
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<Resource> exportAsPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("需要登录才能导出文章");
        }
        
        Post post = postService.getPostEntityById(id);
        
        ByteArrayResource resource = exportService.exportAsPdf(post);
        String filename = encodeFilename(post.getTitle() + ".pdf");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    // 导出文章为HTML格式（需要登录）
    @GetMapping("/{id}/export/html")
    public ResponseEntity<Resource> exportAsHtml(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("需要登录才能导出文章");
        }
        
        Post post = postService.getPostEntityById(id);
        
        ByteArrayResource resource = exportService.exportAsHtml(post);
        String filename = encodeFilename(post.getTitle() + ".html");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    /**
     * 对文件名进行URL编码
     */
    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return filename;
        }
    }
}