package com.lost.blog.interceptor;
import com.lost.blog.service.ActiveUserService;
import com.lost.blog.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户活跃追踪拦截器
 * 拦截所有已认证用户的请求，记录其活跃状态
 */
@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    private final ActiveUserService activeUserService;
    private final JwtUtil jwtUtil;

    public UserActivityInterceptor(ActiveUserService activeUserService, JwtUtil jwtUtil) {
        this.activeUserService = activeUserService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取JWT token
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                // 解析token获取用户ID
                Long userId = jwtUtil.getUserIdFromToken(token);

                if (userId != null) {
                    // 记录用户活跃
                    activeUserService.recordUserActivity(userId);
                }
            } catch (Exception e) {
                // Token解析失败，忽略（用户未登录或token无效）
            }
        }

        return true;
    }
}
