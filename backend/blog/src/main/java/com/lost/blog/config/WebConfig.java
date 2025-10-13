package com.lost.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:uploads/avatars/}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径，使上传的头像可以通过URL访问
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + uploadDir);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置CORS，允许前端访问
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8081")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
