package com.lost.blog.interceptor;

import com.lost.blog.model.User;
import com.lost.blog.repository.UserRepository;
import com.lost.blog.security.JwtTokenProvider;
import com.lost.blog.service.ActiveUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户活跃追踪拦截器
 * 拦截所有已认证用户的请求，记录其活跃状态到Redis
 */
@Component
public class UserActivityInterceptor implements HandlerInterceptor {
    
    private final ActiveUserService activeUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    public UserActivityInterceptor(ActiveUserService activeUserService, 
                                   JwtTokenProvider jwtTokenProvider,
                                   UserRepository userRepository) {
        this.activeUserService = activeUserService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取JWT token
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                // 验证token
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromJWT(token);
                    
                    // 如果token中没有userId（旧版本token），则通过username查询
                    if (userId == null) {
                        String username = jwtTokenProvider.getUsernameFromJWT(token);
                        User user = userRepository.findByUsername(username).orElse(null);
                        if (user != null) {
                            userId = user.getId();
                        }
                    }
                    
                    if (userId != null) {
                        // 记录用户活跃到Redis
                        activeUserService.recordUserActivity(userId);
                    }
                }
            } catch (Exception e) {
                // Token解析失败，忽略（用户未登录或token无效）
                // 不影响正常请求流程
            }
        }
        
        return true;
    }
}
