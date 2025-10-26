package com.lost.blog.config;

import com.lost.blog.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod; // 导入 HttpMethod

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CustomUserDetailsService 被 JwtAuthenticationFilter 依赖，所以这里不再需要直接注入

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 注入我们自定义的JWT过滤器

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- 权限规则调整 ---
                        // 允许对登录和注册接口的匿名访问
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        // 允许对所有文章相关的GET请求的匿名访问（列表和详情）
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                        // 允许对点赞信息的GET请求的匿名访问（查看点赞数量和状态）
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/likes").permitAll()
                        // 允许对评论点赞信息的GET请求的匿名访问
                        .requestMatchers(HttpMethod.GET, "/api/comments/*/likes").permitAll()
                        // 允许对标签的GET和POST请求的匿名访问（查看和创建标签）
                        .requestMatchers(HttpMethod.GET, "/api/tags", "/api/tags/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tags").permitAll()
                        // 允许对上传文件（包括头像）的匿名访问
                        .requestMatchers("/uploads/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}