package com.lost.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import com.lost.blog.dto.AvatarUrlRequest;
import com.lost.blog.dto.JwtAuthenticationResponse;
import com.lost.blog.dto.LoginRequest;
import com.lost.blog.dto.UserRegistrationRequest;
import com.lost.blog.dto.UserResponse;
import com.lost.blog.mapper.UserMapper;
import com.lost.blog.model.User;
import com.lost.blog.security.JwtTokenProvider;
import com.lost.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户注册、登录、认证相关接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数验证失败或用户名已存在")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        // 直接调用，如果service抛出异常，会被GlobalExceptionHandler捕获
        userService.registerUser(
                registrationRequest.getUsername(),
                registrationRequest.getPassword(),
                registrationRequest.getEmail()
        );
        return new ResponseEntity<>("用户注册成功！", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，获取JWT Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 根据rememberMe参数生成不同过期时间的token
        String jwt = tokenProvider.generateToken(authentication, loginRequest.isRememberMe());
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    // -------- 获取当前用户信息 --------
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户信息"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public ResponseEntity<?> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        // 返回完整的用户信息
        User user = userService.findByUsername(currentUser.getUsername());
        UserResponse userResponse = UserMapper.toUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    // -------- 保存用户头像 --------
    @PostMapping("/me/avatar")
    @Operation(summary = "更新用户头像", description = "更新当前用户的头像URL",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "头像更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public ResponseEntity<?> saveUserAvatar(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody AvatarUrlRequest avatarUrlRequest) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        
        logger.info("Received avatar URL: {}", avatarUrlRequest.getAvatarUrl());
        logger.info("Current user: {}", currentUser.getUsername());
        
        // 更新用户头像
        User updatedUser = userService.updateUserAvatar(
                currentUser.getUsername(), 
                avatarUrlRequest.getAvatarUrl()
        );
        
        logger.info("Avatar updated successfully for user: {}", currentUser.getUsername());
        
        UserResponse userResponse = UserMapper.toUserResponse(updatedUser);
        return ResponseEntity.ok(userResponse);
    }

    // -------- 刷新Token --------
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新Token", description = "刷新当前用户的JWT Token",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token刷新成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public ResponseEntity<?> refreshToken(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails currentUser,
            @RequestBody(required = false) LoginRequest refreshRequest) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        
        // 从当前认证信息重新生成token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 如果请求中包含rememberMe标志，使用它；否则默认为false（短期token）
        boolean rememberMe = refreshRequest != null && refreshRequest.isRememberMe();
        String jwt = tokenProvider.generateToken(authentication, rememberMe);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }
}
