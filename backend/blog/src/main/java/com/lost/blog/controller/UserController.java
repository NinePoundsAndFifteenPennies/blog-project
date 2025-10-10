package com.lost.blog.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import com.lost.blog.dto.JwtAuthenticationResponse;
import com.lost.blog.dto.LoginRequest;
import com.lost.blog.dto.UserRegistrationRequest;
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
public class UserController {

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

    // -------- 把 getCurrentUser 放到类的内部（非常关键） --------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        // 如果你想返回更多用户信息，可以返回自定义 DTO
        return ResponseEntity.ok("当前登录用户是: " + currentUser.getUsername());
    }

    // -------- 刷新Token --------
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal UserDetails currentUser,
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
