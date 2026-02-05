package com.lost.blog.controller;

import com.lost.blog.dto.*;
import com.lost.blog.mapper.UserMapper;
import com.lost.blog.model.Role;
import com.lost.blog.model.User;
import com.lost.blog.security.JwtTokenProvider;
import com.lost.blog.service.AdminCommentService;
import com.lost.blog.service.AdminPostService;
import com.lost.blog.service.AdminUserService;
import com.lost.blog.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.util.List;

/**
 * 管理员控制器
 * 处理管理员登录、获取管理员信息、用户管理、文章管理、评论管理等请求
 * 
 * 注意：管理员的token刷新请使用 /api/users/refresh-token 接口，
 * 该接口对所有已认证用户通用。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final AdminUserService adminUserService;
    private final AdminPostService adminPostService;
    private final AdminCommentService adminCommentService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AdminController(UserService userService,
                          AdminUserService adminUserService,
                          AdminPostService adminPostService,
                          AdminCommentService adminCommentService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.adminUserService = adminUserService;
        this.adminPostService = adminPostService;
        this.adminCommentService = adminCommentService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 管理员登录接口
     * 验证用户凭证并检查是否具有管理员角色
     * 
     * 此接口会在登录时验证用户是否为管理员，非管理员会返回403错误。
     * 前端也可以使用普通登录接口 /api/users/login，登录后根据用户角色决定跳转目标。
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

    // ======================= 用户管理接口 =======================

    /**
     * 获取用户列表（支持分页和多条件搜索）
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param username 用户名搜索（模糊匹配）
     * @param email 邮箱搜索（模糊匹配）
     * @param role 角色过滤（USER/ADMIN）
     * @param enabled 状态过滤（true/false）
     * @param startDate 注册开始日期（yyyy-MM-dd）
     * @param endDate 注册结束日期（yyyy-MM-dd）
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AdminUserQueryRequest query = new AdminUserQueryRequest();
        query.setUsername(username);
        query.setEmail(email);
        query.setRole(role);
        query.setEnabled(enabled);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminUserResponse> users = adminUserService.searchUsers(query, pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * 获取用户详细信息（包含发文数、评论数统计）
     * 
     * @param id 用户ID
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> getUserDetail(@PathVariable Long id) {
        AdminUserResponse user = adminUserService.getUserDetail(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户状态（启用/禁用）
     * 
     * @param id 用户ID
     * @param request 状态更新请求
     */
    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        // 防止管理员禁用自己
        User targetUser = adminUserService.findById(id);
        if (targetUser.getUsername().equals(currentUser.getUsername())) {
            return ResponseEntity.badRequest().body("不能禁用自己的账户");
        }
        
        AdminUserResponse user = adminUserService.updateUserStatus(id, request.getEnabled());
        logger.info("管理员 {} 将用户 {} 状态更新为: {}", 
                currentUser.getUsername(), id, request.getEnabled() ? "启用" : "禁用");
        
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户角色
     * 
     * @param id 用户ID
     * @param request 角色更新请求
     */
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserRoleRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        // 防止管理员修改自己的角色
        User targetUser = adminUserService.findById(id);
        if (targetUser.getUsername().equals(currentUser.getUsername())) {
            return ResponseEntity.badRequest().body("不能修改自己的角色");
        }
        
        Role role = Role.valueOf(request.getRole());
        AdminUserResponse user = adminUserService.updateUserRole(id, role);
        logger.info("管理员 {} 将用户 {} 角色更新为: {}", 
                currentUser.getUsername(), id, request.getRole());
        
        return ResponseEntity.ok(user);
    }

    // ======================= 文章管理接口 =======================

    /**
     * 获取文章列表（支持分页和多条件搜索）
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param title 标题搜索（模糊匹配）
     * @param author 作者用户名/昵称搜索（模糊匹配）
     * @param status 状态过滤：DRAFT/PENDING_REVIEW/PUBLISHED/REJECTED/PENDING_REVISION
     * @param categoryId 分类ID过滤
     * @param tag 标签名称过滤
     * @param startDate 创建开始日期（yyyy-MM-dd）
     * @param endDate 创建结束日期（yyyy-MM-dd）
     */
    @GetMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminPostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AdminPostQueryRequest query = new AdminPostQueryRequest();
        query.setTitle(title);
        query.setAuthor(author);
        query.setStatus(status);
        query.setCategoryId(categoryId);
        query.setTag(tag);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminPostResponse> posts = adminPostService.searchPosts(query, pageable);
        
        return ResponseEntity.ok(posts);
    }

    /**
     * 获取文章详细信息
     * 
     * @param id 文章ID
     */
    @GetMapping("/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPostResponse> getPostDetail(@PathVariable Long id) {
        AdminPostResponse post = adminPostService.getPostDetail(id);
        return ResponseEntity.ok(post);
    }

    /**
     * 执行文章操作（审核通过/拒绝/删除）
     * 支持批量操作
     * 
     * @param request 操作请求
     */
    @PostMapping("/posts/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executePostAction(
            @Valid @RequestBody AdminPostActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        User admin = userService.findByUsername(currentUser.getUsername());
        String action = request.getAction().toUpperCase();
        
        // 验证需要理由的操作
        if (("REJECT".equals(action) || "DELETE".equals(action)) && 
            (request.getReason() == null || request.getReason().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(action.equals("REJECT") ? 
                    "拒绝操作需要填写理由" : "删除操作需要填写理由");
        }
        
        AdminBatchActionResponse result;
        
        switch (action) {
            case "APPROVE":
                result = adminPostService.approvePosts(request.getPostIds(), admin);
                logger.info("管理员 {} 批量审核通过 {} 篇文章", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            case "REJECT":
                result = adminPostService.rejectPosts(
                        request.getPostIds(), 
                        request.getFormTitle(), 
                        request.getReason(), 
                        request.getExtraFields(), 
                        admin);
                logger.info("管理员 {} 批量拒绝 {} 篇文章", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            case "DELETE":
                result = adminPostService.deletePosts(
                        request.getPostIds(), 
                        request.getFormTitle(), 
                        request.getReason(), 
                        request.getExtraFields(), 
                        admin);
                logger.info("管理员 {} 批量删除 {} 篇文章", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            default:
                return ResponseEntity.badRequest().body("不支持的操作类型: " + action);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取表单详情
     * 
     * @param id 表单ID
     */
    @GetMapping("/forms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminFormResponse> getFormDetail(@PathVariable Long id) {
        AdminFormResponse form = adminPostService.getFormDetail(id);
        return ResponseEntity.ok(form);
    }

    /**
     * 获取文章关联的表单列表
     * 
     * @param postId 文章ID
     */
    @GetMapping("/posts/{postId}/forms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminFormResponse>> getPostForms(@PathVariable Long postId) {
        List<AdminFormResponse> forms = adminPostService.getPostForms(postId);
        return ResponseEntity.ok(forms);
    }

    // ======================= 评论管理接口 =======================

    /**
     * 获取评论列表（支持分页和多条件搜索）
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param content 评论内容搜索（模糊匹配）
     * @param author 作者用户名/昵称搜索（模糊匹配）
     * @param postTitle 文章标题搜索（模糊匹配）
     * @param status 状态过滤：PENDING/APPROVED
     * @param startDate 创建开始日期（yyyy-MM-dd）
     * @param endDate 创建结束日期（yyyy-MM-dd）
     * @param includeReplies 是否包含子评论（默认true）
     */
    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminCommentResponse>> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String postTitle,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "true") Boolean includeReplies) {
        
        AdminCommentQueryRequest query = new AdminCommentQueryRequest();
        query.setContent(content);
        query.setAuthor(author);
        query.setPostTitle(postTitle);
        query.setStatus(status);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setIncludeReplies(includeReplies);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminCommentResponse> comments = adminCommentService.searchComments(query, pageable);
        
        return ResponseEntity.ok(comments);
    }

    /**
     * 获取评论详细信息
     * 
     * @param id 评论ID
     */
    @GetMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminCommentResponse> getCommentDetail(@PathVariable Long id) {
        AdminCommentResponse comment = adminCommentService.getCommentDetail(id);
        return ResponseEntity.ok(comment);
    }

    /**
     * 执行评论操作（审核通过/删除）
     * 支持批量操作
     * 
     * @param request 操作请求
     */
    @PostMapping("/comments/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executeCommentAction(
            @Valid @RequestBody AdminCommentActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        User admin = userService.findByUsername(currentUser.getUsername());
        String action = request.getAction().toUpperCase();
        
        // 验证删除操作需要理由
        if ("DELETE".equals(action) && 
            (request.getReason() == null || request.getReason().trim().isEmpty())) {
            return ResponseEntity.badRequest().body("删除操作需要填写理由");
        }
        
        AdminBatchActionResponse result;
        
        switch (action) {
            case "APPROVE":
                result = adminCommentService.approveComments(request.getCommentIds(), admin);
                logger.info("管理员 {} 批量审核通过 {} 条评论", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            case "DELETE":
                result = adminCommentService.deleteComments(
                        request.getCommentIds(), 
                        request.getFormTitle(), 
                        request.getReason(), 
                        request.getExtraFields(), 
                        admin);
                logger.info("管理员 {} 批量删除 {} 条评论", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            default:
                return ResponseEntity.badRequest().body("不支持的操作类型: " + action);
        }
        
        return ResponseEntity.ok(result);
    }
}
