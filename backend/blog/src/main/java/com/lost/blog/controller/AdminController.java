package com.lost.blog.controller;

import com.lost.blog.dto.*;
import com.lost.blog.mapper.UserMapper;
import com.lost.blog.model.Role;
import com.lost.blog.model.User;
import com.lost.blog.security.JwtTokenProvider;
import com.lost.blog.service.AdminCommentService;
import com.lost.blog.service.AdminLogService;
import com.lost.blog.service.AdminPostService;
import com.lost.blog.service.AdminReportService;
import com.lost.blog.service.AdminTagService;
import com.lost.blog.service.AdminCategoryService;
import com.lost.blog.service.AdminUserService;
import com.lost.blog.service.DashboardService;
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
    private final AdminTagService adminTagService;
    private final AdminCategoryService adminCategoryService;
    private final AdminLogService adminLogService;
    private final AdminReportService adminReportService;
    private final DashboardService dashboardService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AdminController(UserService userService,
                          AdminUserService adminUserService,
                          AdminPostService adminPostService,
                          AdminCommentService adminCommentService,
                          AdminTagService adminTagService,
                          AdminCategoryService adminCategoryService,
                          AdminLogService adminLogService,
                          AdminReportService adminReportService,
                          DashboardService dashboardService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.adminUserService = adminUserService;
        this.adminPostService = adminPostService;
        this.adminCommentService = adminCommentService;
        this.adminTagService = adminTagService;
        this.adminCategoryService = adminCategoryService;
        this.adminLogService = adminLogService;
        this.adminReportService = adminReportService;
        this.dashboardService = dashboardService;
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
     * 管理员仪表盘数据
     * 返回核心统计、趋势图表数据、热门文章等
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(dashboardService.getDashboardData(currentUser.getUsername()));
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
        
        // 只能禁用或启用非管理员用户
        if (targetUser.getRole() == Role.ADMIN) {
            return ResponseEntity.badRequest().body("不能修改管理员的状态");
        }
        
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminUserResponse user = adminUserService.updateUserStatus(id, request.getEnabled(),
                request.getFormTitle(), request.getReason(), request.getExtraFields(), admin);
        logger.info("管理员 {} 将用户 {} 状态更新为: {}", 
                currentUser.getUsername(), id, request.getEnabled() ? "启用" : "禁用");
        
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

    // ======================= 标签管理接口 =======================

    /**
     * 获取标签列表（支持分页和多条件搜索，按热度排序）
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param name 标签名称搜索（模糊匹配）
     * @param createdBy 创建者用户名搜索（模糊匹配）
     * @param startDate 创建开始日期（yyyy-MM-dd）
     * @param endDate 创建结束日期（yyyy-MM-dd）
     */
    @GetMapping("/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminTagResponse>> getTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AdminTagQueryRequest query = new AdminTagQueryRequest();
        query.setName(name);
        query.setCreatedBy(createdBy);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminTagResponse> tags = adminTagService.searchTags(query, pageable);
        
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取标签详细信息
     * 
     * @param id 标签ID
     */
    @GetMapping("/tags/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminTagResponse> getTagDetail(@PathVariable Long id) {
        AdminTagResponse tag = adminTagService.getTagDetail(id);
        return ResponseEntity.ok(tag);
    }

    /**
     * 管理员创建标签
     * 
     * @param tagRequest 标签创建请求
     */
    @PostMapping("/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminTagResponse> createTag(
            @Valid @RequestBody TagRequest tagRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminTagResponse tag = adminTagService.createTag(tagRequest, admin);
        logger.info("管理员 {} 创建标签: {}", currentUser.getUsername(), tagRequest.getName());
        return new ResponseEntity<>(tag, HttpStatus.CREATED);
    }

    /**
     * 管理员更新标签
     * 
     * @param id 标签ID
     * @param tagRequest 标签更新请求
     */
    @PutMapping("/tags/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminTagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest tagRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminTagResponse tag = adminTagService.updateTag(id, tagRequest, admin);
        logger.info("管理员 {} 更新标签 {}", currentUser.getUsername(), id);
        return ResponseEntity.ok(tag);
    }

    /**
     * 执行标签操作（软删除/硬删除）
     * 支持批量操作
     * 
     * @param request 操作请求
     */
    @PostMapping("/tags/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executeTagAction(
            @Valid @RequestBody AdminTagActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        User admin = userService.findByUsername(currentUser.getUsername());
        String action = request.getAction().toUpperCase();
        
        // 验证删除操作需要理由
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("删除操作需要填写理由");
        }
        
        AdminBatchActionResponse result;
        
        switch (action) {
            case "SOFT_DELETE":
                result = adminTagService.softDeleteTags(
                        request.getTagIds(),
                        request.getPostIds(),
                        request.getFormTitle(),
                        request.getReason(),
                        request.getExtraFields(),
                        admin);
                logger.info("管理员 {} 批量软删除 {} 个标签", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            case "HARD_DELETE":
                result = adminTagService.hardDeleteTags(
                        request.getTagIds(),
                        request.getFormTitle(),
                        request.getReason(),
                        request.getExtraFields(),
                        admin);
                logger.info("管理员 {} 批量硬删除 {} 个标签", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            default:
                return ResponseEntity.badRequest().body("不支持的操作类型: " + action);
        }
        
        return ResponseEntity.ok(result);
    }

    // ======================= 分类管理接口 =======================

    /**
     * 获取分类列表（支持分页和多条件搜索，按文章数排序）
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param name 分类名称搜索（模糊匹配）
     * @param createdBy 创建者用户名搜索（模糊匹配）
     * @param startDate 创建开始日期（yyyy-MM-dd）
     * @param endDate 创建结束日期（yyyy-MM-dd）
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminCategoryResponse>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AdminCategoryQueryRequest query = new AdminCategoryQueryRequest();
        query.setName(name);
        query.setCreatedBy(createdBy);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminCategoryResponse> categories = adminCategoryService.searchCategories(query, pageable);
        
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取分类详细信息
     * 
     * @param id 分类ID
     */
    @GetMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminCategoryResponse> getCategoryDetail(@PathVariable Long id) {
        AdminCategoryResponse category = adminCategoryService.getCategoryDetail(id);
        return ResponseEntity.ok(category);
    }

    /**
     * 管理员创建分类
     * 
     * @param categoryRequest 分类创建请求
     */
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminCategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminCategoryResponse category = adminCategoryService.createCategory(categoryRequest, admin);
        logger.info("管理员 {} 创建分类: {}", currentUser.getUsername(), categoryRequest.getName());
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    /**
     * 管理员更新分类
     * 
     * @param id 分类ID
     * @param categoryRequest 分类更新请求
     */
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminCategoryResponse category = adminCategoryService.updateCategory(id, categoryRequest, admin);
        logger.info("管理员 {} 更新分类 {}", currentUser.getUsername(), id);
        return ResponseEntity.ok(category);
    }

    /**
     * 按标题搜索文章（用于分类管理时添加文章）
     * 
     * @param title 文章标题关键词
     */
    @GetMapping("/categories/search-posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminCategoryResponse.PostInfo>> searchPostsForCategory(
            @RequestParam String title) {
        List<AdminCategoryResponse.PostInfo> posts = adminCategoryService.searchPostsByTitle(title);
        return ResponseEntity.ok(posts);
    }

    /**
     * 将文章归入指定分类
     * 
     * @param id 分类ID
     * @param postId 文章ID
     */
    @PostMapping("/categories/{id}/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminCategoryResponse> assignPostToCategory(
            @PathVariable Long id,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails currentUser) {
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminCategoryResponse category = adminCategoryService.assignPostToCategory(id, postId, admin);
        logger.info("管理员 {} 将文章 {} 归入分类 {}", currentUser.getUsername(), postId, id);
        return ResponseEntity.ok(category);
    }

    /**
     * 将文章从指定分类移除
     * 
     * @param id 分类ID
     * @param postId 文章ID
     */
    @DeleteMapping("/categories/{id}/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminCategoryResponse> removePostFromCategory(
            @PathVariable Long id,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails currentUser) {
        User admin = userService.findByUsername(currentUser.getUsername());
        AdminCategoryResponse category = adminCategoryService.removePostFromCategory(id, postId, admin);
        logger.info("管理员 {} 将文章 {} 从分类 {} 移除", currentUser.getUsername(), postId, id);
        return ResponseEntity.ok(category);
    }

    /**
     * 执行分类操作（删除）
     * 支持批量操作，删除分类时会清除文章表中对应的分类ID
     * 
     * @param request 操作请求
     */
    @PostMapping("/categories/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executeCategoryAction(
            @Valid @RequestBody AdminCategoryActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        User admin = userService.findByUsername(currentUser.getUsername());
        String action = request.getAction().toUpperCase();
        
        // 验证删除操作需要理由
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("删除操作需要填写理由");
        }
        
        AdminBatchActionResponse result;
        
        switch (action) {
            case "DELETE":
                result = adminCategoryService.deleteCategories(
                        request.getCategoryIds(),
                        request.getReason(),
                        request.getExtraFields(),
                        admin);
                logger.info("管理员 {} 批量删除 {} 个分类", currentUser.getUsername(), result.getSuccessCount());
                break;
                
            default:
                return ResponseEntity.badRequest().body("不支持的操作类型: " + action);
        }
        
        return ResponseEntity.ok(result);
    }

    // ======================= 操作日志接口 =======================

    /**
     * 获取操作日志列表（支持分页和多条件搜索）
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param operationType 操作类型过滤
     * @param adminUsername 管理员用户名搜索（模糊匹配）
     * @param title 操作标题搜索（模糊匹配）
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminLogResponse>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String adminUsername,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AdminLogQueryRequest query = new AdminLogQueryRequest();
        query.setOperationType(operationType);
        query.setAdminUsername(adminUsername);
        query.setTitle(title);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminLogResponse> logs = adminLogService.searchLogs(query, pageable);
        
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取操作日志详情
     * 
     * @param id 日志ID
     */
    @GetMapping("/logs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminLogResponse> getLogDetail(@PathVariable Long id) {
        AdminLogResponse log = adminLogService.getLogDetail(id);
        return ResponseEntity.ok(log);
    }

    // ======================= 举报管理接口 =======================

    /**
     * 获取举报列表（支持分页和多条件搜索）
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String reporterUsername,
            @RequestParam(required = false) String reportedUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        AdminReportQueryRequest query = new AdminReportQueryRequest();
        query.setStatus(status);
        query.setTargetType(targetType);
        query.setReporterUsername(reporterUsername);
        query.setReportedUsername(reportedUsername);
        query.setStartDate(startDate);
        query.setEndDate(endDate);

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminReportResponse> reports = adminReportService.searchReports(query, pageable);

        return ResponseEntity.ok(reports);
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminReportResponse> getReportDetail(@PathVariable Long id) {
        AdminReportResponse report = adminReportService.getReportDetail(id);
        return ResponseEntity.ok(report);
    }

    /**
     * 处理举报（通过/驳回）
     */
    @PostMapping("/reports/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executeReportAction(
            @Valid @RequestBody AdminReportActionRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("处理举报需要填写理由");
        }

        adminReportService.processReport(request, currentUser);

        String action = request.getAction().toUpperCase();
        String message = "APPROVE".equals(action) ? "举报已通过处理" : "举报已驳回处理";
        logger.info("管理员 {} {} 举报 #{}", currentUser.getUsername(), message, request.getReportId());

        return ResponseEntity.ok().body(message);
    }

    /**
     * 获取待处理举报数量
     */
    @GetMapping("/reports/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getPendingReportCount() {
        return ResponseEntity.ok(adminReportService.getPendingCount());
    }
}
