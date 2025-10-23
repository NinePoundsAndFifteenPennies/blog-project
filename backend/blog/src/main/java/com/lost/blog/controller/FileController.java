package com.lost.blog.controller;

import com.lost.blog.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("file") MultipartFile file) {
        
        // 验证用户身份
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("未登录或token无效，请先登录");
        }

        try {
            String fileUrl = fileService.uploadAvatar(currentUser.getUsername(), file);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/update/avatar")
    public ResponseEntity<?> updateAvatar(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("file") MultipartFile file) {
        
        // 验证用户身份
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("未登录或token无效，请先登录");
        }

        try {
            String fileUrl = fileService.updateAvatar(currentUser.getUsername(), file);
            
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件更新失败: " + e.getMessage());
        }
    }
}
