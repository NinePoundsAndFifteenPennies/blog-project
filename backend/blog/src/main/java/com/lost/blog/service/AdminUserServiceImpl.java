package com.lost.blog.service;

import com.lost.blog.dto.AdminUserQueryRequest;
import com.lost.blog.dto.AdminUserResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.Role;
import com.lost.blog.model.User;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 管理员用户服务实现类
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public AdminUserServiceImpl(UserRepository userRepository, 
                               PostRepository postRepository,
                               CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Page<AdminUserResponse> searchUsers(AdminUserQueryRequest query, Pageable pageable) {
        // 解析日期范围
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(query.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                startDate = date.atStartOfDay();
            } catch (DateTimeParseException e) {
                logger.warn("Invalid start date format: {}", query.getStartDate());
            }
        }
        
        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(query.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                endDate = date.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid end date format: {}", query.getEndDate());
            }
        }

        Page<User> users = userRepository.searchUsers(
                query.getUsername(),
                query.getEmail(),
                query.getRole(),
                query.getEnabled(),
                startDate,
                endDate,
                pageable
        );

        return users.map(this::toAdminUserResponse);
    }

    @Override
    public AdminUserResponse getUserDetail(Long userId) {
        User user = findById(userId);
        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, Boolean enabled) {
        User user = findById(userId);
        user.setEnabled(enabled);
        user = userRepository.save(user);
        logger.info("User {} status updated to: {}", userId, enabled ? "enabled" : "disabled");
        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Long userId, Role role) {
        User user = findById(userId);
        user.setRole(role);
        user = userRepository.save(user);
        logger.info("User {} role updated to: {}", userId, role);
        return toAdminUserResponse(user);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));
    }

    /**
     * 将User实体转换为AdminUserResponse DTO
     */
    private AdminUserResponse toAdminUserResponse(User user) {
        Long postCount = postRepository.countByUser(user);
        Long commentCount = commentRepository.countByUser(user);
        
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getRole() != null ? user.getRole().name() : "USER",
                user.getEnabled() != null ? user.getEnabled() : true,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                postCount,
                commentCount
        );
    }
}
