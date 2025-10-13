package com.lost.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    // 文件上传目录，可以通过配置文件配置，默认为 uploads/avatars/
    @Value("${file.upload-dir:uploads/avatars/}")
    private String uploadDir;
    
    @PostMapping("/upload/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }
        
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());
            
            // 返回文件访问URL
            String fileUrl = "/uploads/avatars/" + newFilename;
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        }
    }
}
