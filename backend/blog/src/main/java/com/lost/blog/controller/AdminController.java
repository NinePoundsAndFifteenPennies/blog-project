package com.lost.blog.controller;

import com.lost.blog.dto.JwtAuthenticationResponse;
import com.lost.blog.dto.LoginRequest;
import com.lost.blog.dto.UserResponse;
import com.lost.blog.mapper.UserMapper;
import com.lost.blog.model.Role;
import com.lost.blog.model.User;
import com.lost.blog.security.JwtTokenProvider;
import com.lost.blog.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 * 处理管理员登录、获取管理员信息等请求
 * 
 * 注意：管理员的token刷新请使用 /api/users/refresh-token 接口，
 * 该接口对所有已认证用户通用。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AdminController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 管理员登录接口（可选）
     * 验证用户凭证并检查是否具有管理员角色
     * 
     * 注意：也可以使用普通登录接口 /api/users/login，
     * 登录后前端根据用户角色决定跳转目标。
     */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {
        // 先进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 验证用户是否为管理员
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user.getRole() != Role.ADMIN) {
            logger.warn("非管理员用户尝试登录管理后台: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("您没有管理员权限");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT token
        String jwt = tokenProvider.generateToken(authentication, loginRequest.isRememberMe());
        logger.info("管理员登录成功: {}", loginRequest.getUsername());
        
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentAdmin(@AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findByUsername(currentUser.getUsername());
        UserResponse userResponse = UserMapper.toUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * 管理员仪表盘数据（示例接口）
     * 可以根据需要扩展返回更多统计数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserDetails currentUser) {
        // 返回简单的仪表盘数据，后续可以扩展
        return ResponseEntity.ok(java.util.Map.of(
            "message", "欢迎进入管理后台",
            "admin", currentUser.getUsername()
        ));
    }
}
