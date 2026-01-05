package com.lost.blog.service;

import com.lost.blog.model.User;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final UserRepository userRepository;

    // 不再硬编码，支持通过配置覆盖
    @Value("${file.upload-dir:uploads}")
    private String uploadBaseDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            throw new RuntimeException("用户已有头像，请使用更新接口替换头像");
        }

        return saveAvatarFile(user.getId(), file);
    }

    @Override
    public String updateAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

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
            String filePath = avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl;
            Path path = Paths.get(filePath);
            if (!path.isAbsolute()) {
                // resolve relative to resolved base dir
                path = resolveBaseDir().resolve(filePath).normalize();
            }
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            System.err.println("删除旧头像失败: " + e.getMessage());
        }
    }

    private String saveAvatarFile(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("文件大小不能超过5MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw new RuntimeException("只支持JPG和PNG格式的图片");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new RuntimeException("只支持JPG和PNG格式的图片");
            }

            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new RuntimeException("无效的图片文件");
            }

            int width = image.getWidth();
            int height = image.getHeight();
            if (width < MIN_WIDTH || height < MIN_HEIGHT) {
                throw new RuntimeException(String.format("图片尺寸太小，最小尺寸为%dx%d像素", MIN_WIDTH, MIN_HEIGHT));
            }
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                throw new RuntimeException(String.format("图片尺寸太大，最大尺寸为%dx%d像素", MAX_WIDTH, MAX_HEIGHT));
            }

            String filename = UUID.randomUUID().toString() + extension;

            // base dir resolved to an absolute Path
            Path baseDir = resolveBaseDir();
            Path uploadPath = baseDir.resolve(String.valueOf(userId)).resolve("avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 直接返回 URL 字符串（不依赖 Path.toString）
            return "/uploads/" + userId + "/avatars/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    // 解析并返回 uploads 的绝对路径（如果配置是相对路径，则尝试向上查找真实目录）
    private Path resolveBaseDir() {
        Path configured = Paths.get(uploadBaseDir);
        if (configured.isAbsolute()) {
            return configured.toAbsolutePath().normalize();
        }

        Path cwd = Paths.get(System.getProperty("user.dir"));
        for (int i = 0; i < 4; i++) {
            Path candidate = cwd;
            for (int j = 0; j < i; j++) {
                if (candidate.getParent() != null) candidate = candidate.getParent();
            }
            candidate = candidate.resolve(configured);
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        // fallback: create/use cwd/configured
        return cwd.resolve(configured).toAbsolutePath().normalize();
    }
}