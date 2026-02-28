package com.lost.blog.service;

import com.lost.blog.dto.AdminUserQueryRequest;
import com.lost.blog.dto.AdminUserResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.AdminForm;
import com.lost.blog.model.AdminFormType;
import com.lost.blog.model.AdminLogType;
import com.lost.blog.model.Role;
import com.lost.blog.model.User;
import com.lost.blog.repository.AdminFormRepository;
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
    private final AdminFormRepository adminFormRepository;
    private final AdminLogService adminLogService;

    @Autowired
    public AdminUserServiceImpl(UserRepository userRepository,
                               PostRepository postRepository,
                               CommentRepository commentRepository,
                               AdminFormRepository adminFormRepository,
                               AdminLogService adminLogService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.adminFormRepository = adminFormRepository;
        this.adminLogService = adminLogService;
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
    public AdminUserResponse updateUserStatus(Long userId, Boolean enabled, String formTitle,
                                               String reason, String extraFields, User admin) {
        User user = findById(userId);
        user.setEnabled(enabled);
        user = userRepository.save(user);

        // 创建状态变更表单记录
        String defaultTitle = enabled ? "用户启用记录" : "用户禁用记录";
        String defaultReason = enabled ? "管理员启用了用户 " + user.getUsername() : "管理员禁用了用户 " + user.getUsername();

        AdminForm form = new AdminForm();
        form.setTitle(formTitle != null && !formTitle.trim().isEmpty() ? formTitle : defaultTitle);
        form.setFormType(AdminFormType.USER_STATUS_CHANGE);
        form.setReason(reason != null && !reason.trim().isEmpty() ? reason : defaultReason);
        form.setExtraFields(extraFields);
        form.setTargetUser(user);
        form.setAdmin(admin);
        form.setSent(false);
        adminFormRepository.save(form);

        // 记录审计日志
        String statusText = enabled ? "启用" : "禁用";
        String logDefault = statusText + "用户: " + user.getUsername();
        String logTitle = (formTitle != null && !formTitle.trim().isEmpty())
                ? formTitle : logDefault;
        adminLogService.log(AdminLogType.USER_STATUS_CHANGE,
                logTitle, reason, extraFields,
                admin, null, null, null, null, null, null, null, null,
                user.getId(), user.getUsername(), null);

        logger.info("User {} status updated to: {}", userId, enabled ? "enabled" : "disabled");
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
