# 项目架构说明

本文档详细说明项目的架构设计、代码组织和技术选型。

## 整体架构

本项目采用**前后端分离架构**，遵循 RESTful API 设计规范：

- **前端**: Vue.js SPA，负责用户界面和交互
- **后端**: Spring Boot REST API，负责业务逻辑和数据处理
- **数据库**: MySQL，存储用户、文章等数据
- **缓存**: Redis（可选），用于活跃用户追踪等高性能场景
- **认证**: JWT 无状态认证

```
┌─────────────┐      HTTP/JSON       ┌─────────────┐
│   前端 Vue   │ ◄──────────────────► │  后端 API   │
│   (Port     │                       │  (Port      │
│    3000)    │                       │   8080)     │
└─────────────┘                       └──────┬──────┘
                                             │
                         ┌───────────────────┼───────────────────┐
                         ▼                   ▼                   ▼
                  ┌─────────────┐     ┌─────────────┐    ┌─────────────┐
                  │   MySQL     │     │   Redis     │    │ File System │
                  │  Database   │     │  (可选)     │    │ (头像存储)   │
                  └─────────────┘     └─────────────┘    └─────────────┘
```

## 后端架构

### 三层架构设计

后端采用经典的三层架构模式，实现高内聚、低耦合：

1. **Controller 层** - 处理 HTTP 请求，参数验证，返回响应
2. **Service 层** - 实现业务逻辑，事务管理
3. **Repository 层** - 数据访问，数据库操作

```
Controller ──► Service ──► Repository ──► Database
    │              │
    ▼              ▼
   DTO          Entity
```

### 文件组织结构

后端代码结构遵循 Spring Boot 标准，按功能模块组织：

| 类型 | 路径/文件 | 说明 |
|------|-----------|------|
| 启动类 | **BlogApplication.java** | Spring Boot 应用启动入口 |
| 配置层 | **config/** | 存放全局配置类，如安全配置 |
| 配置类 | └── SecurityConfig.java | Spring Security 核心配置 |
| 配置类 | └── WebConfig.java | Web MVC 配置（静态资源等） |
| 配置类 | └── RedisConfig.java | Redis 配置（可选，用于活跃用户追踪） |
| 配置类 | └── WebMvcConfig.java | Web MVC 拦截器配置 |
| 控制层 (Controller) | **controller/** | 处理 API 请求，负责调用服务层并返回响应 |
| 控制器 | └── UserController.java | 提供用户注册、登录、获取/更新个人信息、获取公开用户资料的 API |
| 控制器 | └── PostController.java | 提供文章 CRUD API |
| 控制器 | └── FileController.java | 提供头像上传和更新 API |
| 控制器 | └── LikeController.java | 提供文章点赞 API |
| 控制器 | └── CommentController.java | 提供评论 CRUD API |
| 控制器 | └── TagController.java | 提供标签 CRUD API |
| 控制器 | └── CategoryController.java | 提供分类 CRUD API |
| 控制器 | └── StatisticsController.java | 提供社区统计 API（用户总数、文章总数、活跃用户、今日访问） |
| 控制器 | └── FollowController.java | 提供关注功能 API（关注/取消关注、列表查询、可见性设置） |
| 控制器 | └── PrivateMessageController.java | 提供私信功能 API（发送消息、获取对话、未读数统计） |
| 控制器 | └── NotificationController.java | 提供通知功能 API（通知列表、未读数、标记已读） |
| 数据传输对象 | **dto/** | 定义请求和响应的数据模型 |
| DTO | └── UserRegistrationRequest.java | 用户注册请求体 |
| DTO | └── LoginRequest.java | 用户登录请求体 |
| DTO | └── PostRequest.java | 文章创建/更新请求体 |
| DTO | └── PostResponse.java | 文章响应体（含作者昵称） |
| DTO | └── UserResponse.java | 用户信息响应体（含昵称等完整资料） |
| DTO | └── UserProfileUpdateRequest.java | 用户资料更新请求体 |
| DTO | └── AvatarUrlRequest.java | 头像URL请求体 |
| DTO | └── JwtAuthenticationResponse.java | JWT 认证响应 |
| DTO | └── ErrorResponse.java | 标准化错误响应 |
| DTO | └── LikeResponse.java | 点赞响应体 |
| DTO | └── CommentRequest.java | 评论创建/更新请求体 |
| DTO | └── CommentResponse.java | 评论响应体 |
| DTO | └── ReplyRequest.java | 子评论创建请求体 |
| DTO | └── TagRequest.java | 标签创建/更新请求体 |
| DTO | └── TagResponse.java | 标签响应体 |
| DTO | └── CategoryRequest.java | 分类创建/更新请求体 |
| DTO | └── CategoryResponse.java | 分类响应体 |
| DTO | └── StatisticsResponse.java | 社区统计响应体 |
| DTO | └── FollowResponse.java | 关注操作响应体 |
| DTO | └── FollowStatsResponse.java | 关注统计响应体 |
| DTO | └── FollowUserResponse.java | 关注用户信息响应体 |
| DTO | └── FollowVisibilityRequest.java | 可见性设置请求体 |
| DTO | └── FollowVisibilityResponse.java | 可见性设置响应体 |
| DTO | └── VisibilitySettingDto.java | 单项可见性设置DTO |
| DTO | └── PrivateMessageRequest.java | 私信发送请求体 |
| DTO | └── PrivateMessageResponse.java | 私信响应体 |
| DTO | └── ConversationResponse.java | 会话列表响应体 |
| DTO | └── NotificationResponse.java | 通知响应体 |
| 拦截器层 | **interceptor/** | HTTP 请求拦截器 |
| 拦截器 | └── UserActivityInterceptor.java | 用户活跃追踪拦截器（可选，配合Redis使用） |
| 异常层 | **exception/** | 自定义异常类与全局异常处理 |
| 异常处理器 | └── GlobalExceptionHandler.java | 统一捕获和处理异常 |
| 异常类 | └── AccessDeniedException.java | 访问被拒绝异常 |
| 异常类 | └── ResourceNotFoundException.java | 资源未找到异常 |
| 映射层 | **mapper/** | 实体与 DTO 的转换工具 |
| 映射器 | └── UserMapper.java | User ↔ UserResponse 转换 |
| 映射器 | └── PostMapper.java | Post ↔ PostResponse 转换 |
| 映射器 | └── CommentMapper.java | Comment ↔ CommentResponse 转换 |
| 映射器 | └── TagMapper.java | Tag ↔ TagResponse 转换 |
| 映射器 | └── CategoryMapper.java | Category ↔ CategoryResponse 转换 |
| 映射器 | └── FollowMapper.java | Follow ↔ FollowUserResponse 转换 |
| 实体层 | **model/** | 数据库实体类 (JPA Entity) |
| 实体 | └── User.java | 用户实体 |
| 实体 | └── Post.java | 文章实体（含浏览量字段）|
| 实体 | └── PostViewLog.java | 文章浏览日志实体（用于浏览量统计和防刷）|
| 实体 | └── Tag.java | 标签实体 |
| 实体 | └── Category.java | 分类实体 |
| 实体 | └── Like.java | 点赞实体（支持文章和评论点赞）|
| 实体 | └── Comment.java | 评论实体（支持顶层评论和子评论）|
| 实体 | └── Follow.java | 关注关系实体（follower→followed）|
| 实体 | └── FollowVisibility.java | 关注信息可见性设置实体 |
| 实体 | └── VisibilitySetting.java | 可嵌入的可见性设置类 |
| 实体 | └── PrivateMessage.java | 私信消息实体 |
| 实体 | └── Notification.java | 通知实体 |
| 枚举 | └── ContentType.java | 内容类型枚举 |
| 枚举 | └── FollowInfoType.java | 关注信息类型枚举（FOLLOWING/FOLLOWERS/FRIENDS/STATS） |
| 枚举 | └── NotificationType.java | 通知类型枚举（POST_LIKED/POST_COMMENTED/FOLLOWED/COMMENT_LIKED/COMMENT_REPLIED/MESSAGE_RECEIVED） |
| 数据访问层 | **repository/** | 提供数据库操作接口 |
| Repository | └── UserRepository.java | 用户数据访问接口 |
| Repository | └── PostRepository.java | 文章数据访问接口 |
| Repository | └── PostViewLogRepository.java | 文章浏览日志数据访问接口 |
| Repository | └── TagRepository.java | 标签数据访问接口 |
| Repository | └── CategoryRepository.java | 分类数据访问接口 |
| Repository | └── LikeRepository.java | 点赞数据访问接口（文章和评论）|
| Repository | └── CommentRepository.java | 评论数据访问接口 |
| Repository | └── FollowRepository.java | 关注关系数据访问接口 |
| Repository | └── FollowVisibilityRepository.java | 关注可见性设置数据访问接口 |
| Repository | └── PrivateMessageRepository.java | 私信消息数据访问接口 |
| Repository | └── NotificationRepository.java | 通知数据访问接口 |
| 安全层 | **security/** | 与认证和授权相关的工具类 |
| 工具类 | └── JwtTokenProvider.java | JWT 生成与验证 |
| 过滤器 | └── JwtAuthenticationFilter.java | 拦截并校验 JWT 请求 |
| 服务类 | └── CustomUserDetailsService.java | 加载用户信息 (Spring Security) |
| 服务层 | **service/** | 业务逻辑实现 |
| 接口 | └── UserService.java | 用户服务接口 |
| 实现类 | └── UserServiceImpl.java | 用户服务实现 |
| 接口 | └── PostService.java | 文章服务接口 |
| 实现类 | └── PostServiceImpl.java | 文章服务实现 |
| 接口 | └── FileService.java | 文件服务接口 |
| 实现类 | └── FileServiceImpl.java | 文件服务实现（头像上传、更新、删除） |
| 接口 | └── LikeService.java | 点赞服务接口 |
| 实现类 | └── LikeServiceImpl.java | 点赞服务实现（文章和评论点赞，触发通知）|
| 接口 | └── CommentService.java | 评论服务接口 |
| 实现类 | └── CommentServiceImpl.java | 评论服务实现（触发通知）|
| 接口 | └── TagService.java | 标签服务接口 |
| 实现类 | └── TagServiceImpl.java | 标签服务实现 |
| 接口 | └── CategoryService.java | 分类服务接口 |
| 实现类 | └── CategoryServiceImpl.java | 分类服务实现 |
| 接口 | └── StatisticsService.java | 统计服务接口 |
| 实现类 | └── StatisticsServiceImpl.java | 统计服务实现（用户总数、文章总数、活跃用户、今日访问） |
| 接口 | └── ActiveUserService.java | 活跃用户追踪服务接口（可选，使用Redis） |
| 实现类 | └── ActiveUserServiceImpl.java | 活跃用户追踪服务实现（可选，使用Redis） |
| 接口 | └── FollowService.java | 关注服务接口 |
| 实现类 | └── FollowServiceImpl.java | 关注服务实现（关注/取消关注、朋友检测、列表查询，触发通知） |
| 接口 | └── FollowVisibilityService.java | 关注可见性服务接口 |
| 实现类 | └── FollowVisibilityServiceImpl.java | 关注可见性服务实现（按类型检查可见性） |
| 接口 | └── PrivateMessageService.java | 私信服务接口 |
| 实现类 | └── PrivateMessageServiceImpl.java | 私信服务实现（发送消息、防骚扰机制、已读状态，触发通知） |
| 接口 | └── NotificationService.java | 通知服务接口 |
| 实现类 | └── NotificationServiceImpl.java | 通知服务实现（创建通知、查询、标记已读、未读计数） |
| 配置文件 | **resources/** | 存放应用的资源文件 |
| 配置文件 | └── application.properties | 应用配置（数据库、JWT密钥等） |

## 核心设计模式

### 1. DTO 模式

使用 DTO (Data Transfer Object) 模式实现数据传输与实体的解耦：

- **请求 DTO**: 封装客户端请求数据，进行参数验证
- **响应 DTO**: 封装服务端响应数据，隐藏敏感信息
- **Mapper**: 负责 Entity 与 DTO 之间的转换

### 2. 服务接口模式

Service 层使用接口与实现分离：

- 接口定义业务契约
- 实现类包含具体逻辑
- 便于测试和替换实现

### 3. Repository 模式

使用 Spring Data JPA 的 Repository 模式：

- 自动生成基础 CRUD 操作
- 支持自定义查询方法
- 声明式事务管理

### 4. 全局异常处理

使用 `@ControllerAdvice` 实现统一异常处理：

- 捕获所有异常并转换为标准 JSON 响应
- 区分业务异常和系统异常
- 提供友好的错误信息

## 安全架构

### JWT 认证流程

```
1. 用户登录 ──► 验证密码 ──► 生成 JWT ──► 返回 Token
                                          │
2. 后续请求 ──► 携带 JWT ──► 验证 Token ──┘ ──► 允许访问
```

### 权限控制

- **公开接口**: 注册、登录、文章列表/详情
- **需认证接口**: 创建/修改/删除文章、上传头像、获取用户信息
- **资源所有权校验**: 用户只能操作自己的资源

## 数据库设计

### 主要实体

- **User**: 用户信息（用户名、密码、邮箱、头像URL）
- **Post**: 文章信息（标题、内容、作者、创建时间、是否草稿、标签、浏览量）
- **PostViewLog**: 文章浏览日志（文章、IP、设备信息、用户ID、来源URL、访问时间），用于浏览量统计、防刷和流量来源分析
- **Tag**: 标签信息（名称、描述、颜色、图标、排序、创建者、创建时间）
- **Like**: 点赞信息（用户、文章或评论、创建时间）
- **Comment**: 评论信息（内容、用户、文章、父评论、被回复用户、层级、创建时间、更新时间）
- **Follow**: 关注关系（关注者、被关注者、创建时间），用于存储单向关注关系
- **FollowVisibility**: 关注可见性设置（用户、四种类型的独立可见性设置），控制关注信息对谁可见
- **PrivateMessage**: 私信消息（发送者、接收者、内容、已读状态、创建时间）
- **Notification**: 通知（类型、触发者、接收者、关联文章、关联评论、内容、已读状态、创建时间），用于消息中心

### 关系设计

- User ←─[一对多]─→ Post（一个用户可以有多篇文章）
- User ←─[一对多]─→ Tag（一个用户可以创建多个标签）
- Post ←─[多对多]─→ Tag（一篇文章可以有多个标签，一个标签可以被多篇文章使用）
- Post ←─[一对多]─→ PostViewLog（一篇文章可以有多条浏览日志）
- User ←─[一对多]─→ PostViewLog（一个用户可以有多条浏览记录，可选关联）
- User ←─[一对多]─→ Like（一个用户可以点赞多篇文章或评论）
- Post ←─[一对多]─→ Like（一篇文章可以有多个点赞）
- User ←─[一对多]─→ Comment（一个用户可以发表多条评论）
- Post ←─[一对多]─→ Comment（一篇文章可以有多条评论）
- Comment ←─[一对多]─→ Comment（一条评论可以有多条子评论，自引用关系）
- Comment ←─[一对多]─→ Like（一条评论可以有多个点赞）
- User ←─[多对一]─→ Comment（通过reply_to_user_id，一个用户可以被多条评论@）
- User ←─[一对多]─→ Follow（follower，一个用户可以关注多个人）
- User ←─[一对多]─→ Follow（followed，一个用户可以被多个人关注）
- User ←─[一对一]─→ FollowVisibility（一个用户有一个可见性设置）
- User ←─[一对多]─→ Notification（recipient，一个用户可以收到多条通知）
- User ←─[一对多]─→ Notification（actor，一个用户可以触发多条通知）

## 文件存储

### 文件组织结构

```
uploads/
  ├── {userId1}/
  │   ├── avatars/
  │   │   └── {uuid}.jpg
  │   └── covers/
  │       └── {uuid}.jpg
  ├── {userId2}/
  │   ├── avatars/
  │   │   └── {uuid}.png
  │   └── covers/
  │       └── {uuid}.png
  └── ...
```

### 优势

- 按用户隔离文件，便于管理
- 支持扩展其他文件类型（头像、封面等）
- 删除用户时可整体清理

### 封面图片管理

- 文章封面存储在 `uploads/{userId}/covers/` 目录
- 更新文章封面时，旧封面文件会被自动删除
- 删除文章时，关联的封面文件也会被自动删除
- 文件路径验证确保用户只能删除自己的文件

## 前端架构

### 技术栈

- **Vue 3**: 渐进式框架
- **Vue Router**: 路由管理
- **Axios**: HTTP 客户端
- **TailwindCSS**: 原子化 CSS
- **Highlight.js**: 代码高亮

### 主要页面

- **Home**: 首页，双模块布局(热门文章+最新文章)，侧边栏(搜索+热门标签+社区统计)
- **Login/Register**: 登录注册页面
- **Profile**: 用户个人中心
- **ProfileEdit**: 编辑个人资料
- **UserProfile**: 公开用户主页（查看其他用户资料及文章）
- **CreatePost**: 创建/编辑文章
- **PostDetail**: 文章详情（支持评论锚点跳转和高亮显示）
- **TagPosts**: 标签文章列表
- **Search**: 搜索结果页面（支持多维度搜索、排序、关键词高亮）
- **CommentEdit**: 编辑评论页面（支持Markdown工具栏和实时预览）
- **ReplyCreate**: 创建回复页面（支持Markdown工具栏和实时预览，与评论编辑页面体验一致）
- **Messages**: 私信页面（实时聊天、会话列表、未读标记）
- **Notifications**: 通知中心（通知列表、类型过滤、快捷操作、锚点导航）

### UI设计特点

**首页双模块布局**:
- **模块1 - 热门文章**: 展示热度最高的12篇文章，使用可滚动容器
- **模块2 - 最新文章**: 分页展示最新文章

**文章卡片**:
- 支持列表视图和网格视图两种布局
- 列表视图: 封面图(1/3宽度) + 内容(2/3宽度)，横向排列
- 自动摘要提取: 如无summary字段，自动从content中提取前150字符
- 渐变色背景: 无封面图时使用渐变色占位

**社区统计面板**:
- 实时显示用户总数、文章总数、在线用户数、今日访问数
- 支持定期自动刷新
- API未实现时使用占位数据，确保界面完整

**侧边栏组件**:
- 搜索框: 支持实时搜索，关键词高亮
- 热门标签: 显示使用频率最高的标签
- 热门作者: 占位(待后端支持)
- 社区统计: 实时更新社区数据

## 与原始方案的差异

### 功能差异

- **已实现**: 标签 (Tag) 系统，包含：
  - 完整的CRUD操作
  - 创建者追踪（便于后台管理）
  - 颜色和图标支持（便于前端展示）
  - 排序功能
  - 多对多关系（文章与标签）
  - 自动标签创建（创建文章时）
  - 热门标签查询
  - 增强的参数验证和异常处理
- **已实现**: 文章搜索功能，包含：
  - 多维度搜索（关键词、作者、标题、标签）
  - 按时间或热度排序（支持升序/降序）
  - 特殊字符处理（SQL转义、URL处理、标点清理）
  - 搜索结果关键词高亮
  - 输入参数验证
- **新增**: "记住我"功能、JWT自动刷新、增强的Markdown编辑器、头像上传系统、文章点赞功能、评论功能、评论点赞功能、子评论（回复）功能、子评论点赞功能、文章标签功能、创建者追踪、统一异常处理、**文章浏览量统计**（含防刷机制和访问日志）、**首页双模块布局**（热门文章+最新文章）、**文章摘要智能提取**（自动从内容提取）、**社区统计面板**（用户/文章/活跃用户/访问数）、**关注功能**（关注/取消关注、朋友检测、列表查询、独立可见性控制）、**通知功能**（收件箱、未读计数、类型过滤、快捷操作、锚点跳转）

## Redis 集成（可选）

### 概述

项目支持可选的 Redis 集成，用于实现高性能的活跃用户追踪。与基于数据库的统计相比，Redis 提供：

- **实时性**: 追踪用户任何操作（浏览、搜索、评论等），而不仅仅是个人资料更新
- **高性能**: 内存操作，响应速度远超数据库查询
- **自动清理**: 使用 TTL 自动清理过期数据，无需手动维护
- **分布式支持**: 多台服务器共享同一个 Redis 实例

### 架构设计

```
用户请求 → UserActivityInterceptor → 验证JWT → 提取userId → Redis记录
                                                            ↓
                                                    (15分钟TTL)
                                                            ↓
StatisticsController → StatisticsService → ActiveUserService → Redis查询
```

**核心组件**:

1. **RedisConfig**: Redis 连接和序列化配置
2. **ActiveUserService**: 活跃用户追踪服务接口
3. **ActiveUserServiceImpl**: 基于 Redis 的实现，使用 `active:user:{userId}` 作为键，15分钟 TTL
4. **UserActivityInterceptor**: 拦截所有已认证请求，自动记录用户活跃状态
5. **JwtTokenProvider增强**: 支持从 JWT 中提取用户ID（新增 `getUserIdFromJWT` 方法）

### 实施方式

**当前状态** (不使用Redis):
- 活跃用户基于 `User.updatedAt` 字段
- 只追踪个人资料更新，不追踪浏览等行为
- 适合小规模部署

**Redis方案** (可选升级):
- 追踪所有用户活动
- 实时统计，零延迟
- 支持大规模分布式部署

详细实施指南请参考: `docs/REDIS_INTEGRATION.md`

### 技术细节

**Redis 键设计**:
```
active:user:{userId}  # 值: "1", TTL: 900秒(15分钟)
```

**性能考虑**:
- 使用 `keys` 命令适用于用户量 < 10万
- 用户量 > 10万时建议使用 `scan` 命令
- Redis 操作失败时优雅降级（返回0，不影响主业务）

**向后兼容**:
- 拦截器通过 `getUserIdFromJWT()` 提取用户ID
- 如果旧 token 不包含 userId，自动通过 username 查询数据库
- 确保新旧 token 共存期间系统正常运行

### 架构差异

- **认证入口**: 使用 UserController 而非 AuthController
- **配置文件**: 使用 application.properties 而非 application.yml
- **文档工具**: 未集成 Swagger

### 权限简化

- 当前基于资源所有权（用户只能操作自己的资源）
- 未实现基于角色的权限体系（如管理员、普通用户）
