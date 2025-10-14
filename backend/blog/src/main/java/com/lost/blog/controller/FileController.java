package com.lost.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // 配置文件上传的根目录
    private final String uploadDir = "uploads/avatars/";
    
    // 允许的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");
    
    // 文件大小限制（5MB）
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // 图片尺寸限制
    private static final int MAX_WIDTH = 2000;
    private static final int MAX_HEIGHT = 2000;
    private static final int MIN_WIDTH = 50;
    private static final int MIN_HEIGHT = 50;

    @PostMapping("/upload/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }

        try {
            // 1. 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("文件大小不能超过5MB");
            }
            
            // 2. 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                return ResponseEntity.badRequest().body("只支持JPG和PNG格式的图片");
            }
            
            // 3. 验证文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest().body("只支持JPG和PNG格式的图片");
            }
            
            // 4. 验证图片尺寸
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return ResponseEntity.badRequest().body("无效的图片文件");
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            if (width < MIN_WIDTH || height < MIN_HEIGHT) {
                return ResponseEntity.badRequest()
                    .body(String.format("图片尺寸太小，最小尺寸为%dx%d像素", MIN_WIDTH, MIN_HEIGHT));
            }
            
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                return ResponseEntity.badRequest()
                    .body(String.format("图片尺寸太大，最大尺寸为%dx%d像素", MAX_WIDTH, MAX_HEIGHT));
            }

            // 5. 生成唯一的文件名
            String filename = UUID.randomUUID().toString() + extension;
            
            // 6. 创建上传目录（如果不存在）
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 7. 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 8. 构建文件URL
            String fileUrl = "/uploads/avatars/" + filename;

            // 9. 返回文件URL
            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败: " + e.getMessage());
        }
    }
}
