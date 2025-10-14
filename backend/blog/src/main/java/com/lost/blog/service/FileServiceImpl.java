package com.lost.blog.service;

import com.lost.blog.model.User;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final UserRepository userRepository;
    
    // 配置文件上传的根目录
    private static final String UPLOAD_BASE_DIR = "uploads";
    
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

    @Autowired
    public FileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String uploadAvatar(String username, MultipartFile file) {
        // 检查用户是否已有头像
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            throw new RuntimeException("用户已有头像，请使用更新接口替换头像");
        }
        
        return saveAvatarFile(user.getId(), file);
    }

    @Override
    public String updateAvatar(String username, MultipartFile file) {
        // 获取用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 如果用户已有头像，删除旧头像文件
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            deleteAvatar(user.getAvatarUrl());
        }
        
        return saveAvatarFile(user.getId(), file);
    }

    @Override
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }
        
        try {
            // 从URL中提取文件路径
            // avatarUrl格式: /uploads/{userId}/avatars/{filename}
            String filePath = avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl;
            Path path = Paths.get(filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            // 记录错误但不抛出异常，因为删除旧文件失败不应阻止上传新文件
            System.err.println("删除旧头像失败: " + e.getMessage());
        }
    }

    private String saveAvatarFile(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 1. 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("文件大小不能超过5MB");
            }
            
            // 2. 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw new RuntimeException("只支持JPG和PNG格式的图片");
            }
            
            // 3. 验证文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new RuntimeException("只支持JPG和PNG格式的图片");
            }
            
            // 4. 验证图片尺寸
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new RuntimeException("无效的图片文件");
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            if (width < MIN_WIDTH || height < MIN_HEIGHT) {
                throw new RuntimeException(
                    String.format("图片尺寸太小，最小尺寸为%dx%d像素", MIN_WIDTH, MIN_HEIGHT));
            }
            
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                throw new RuntimeException(
                    String.format("图片尺寸太大，最大尺寸为%dx%d像素", MAX_WIDTH, MAX_HEIGHT));
            }

            // 5. 生成唯一的文件名
            String filename = UUID.randomUUID().toString() + extension;
            
            // 6. 创建用户专属的上传目录: uploads/{userId}/avatars/
            String userUploadDir = UPLOAD_BASE_DIR + "/" + userId + "/avatars/";
            Path uploadPath = Paths.get(userUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 7. 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 8. 构建文件URL
            String fileUrl = "/" + userUploadDir + filename;

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}
