package com.lost.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 可通过 -Dfile.upload-dir=... 覆盖，默认值 "uploads"
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 解析上传目录为磁盘路径（优先找到真实存在的目录）
        String resolved = resolveUploadDir(uploadDir);
        if (!resolved.endsWith(File.separator)) {
            resolved = resolved + File.separator;
        }
        // registry expects "file:" + absolutePath
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resolved);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // 如果配置是绝对路径直接使用；否则尝试在当前工作目录及上级目录查找该相对路径，最多上溯 3 层
    private String resolveUploadDir(String configured) {
        Path configuredPath = Paths.get(configured);
        if (configuredPath.isAbsolute()) {
            return configuredPath.toAbsolutePath().toString();
        }

        Path cwd = Paths.get(System.getProperty("user.dir"));
        // try current dir, parent, parent^2, parent^3
        for (int i = 0; i < 4; i++) {
            Path candidate = cwd;
            for (int j = 0; j < i; j++) {
                if (candidate.getParent() != null) {
                    candidate = candidate.getParent();
                }
            }
            candidate = candidate.resolve(configuredPath);
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath().toString();
            }
        }
        // fallback to cwd/configured
        return cwd.resolve(configuredPath).toAbsolutePath().toString();
    }
}
