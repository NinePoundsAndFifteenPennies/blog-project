package com.lost.blog.config;

import com.lost.blog.interceptor.UserActivityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserActivityInterceptor userActivityInterceptor;

    public WebMvcConfig(UserActivityInterceptor userActivityInterceptor) {
        this.userActivityInterceptor = userActivityInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/api/auth/**",       // 排除认证相关接口
                        "/api/statistics",    // 排除统计接口（公开访问）
                        "/error"              // 排除错误页面
                );
    }
}