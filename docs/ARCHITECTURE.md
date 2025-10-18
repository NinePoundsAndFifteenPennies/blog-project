# 项目架构说明

本文档详细说明项目的架构设计、代码组织和技术选型。

## 整体架构

本项目采用**前后端分离架构**，遵循 RESTful API 设计规范：

- **前端**: Vue.js SPA，负责用户界面和交互
- **后端**: Spring Boot REST API，负责业务逻辑和数据处理
- **数据库**: MySQL，存储用户、文章等数据
- **认证**: JWT 无状态认证

```
┌─────────────┐      HTTP/JSON       ┌─────────────┐
│   前端 Vue   │ ◄──────────────────► │  后端 API   │
│   (Port     │                       │  (Port      │
│    3000)    │                       │   8080)     │
└─────────────┘                       └──────┬──────┘
                                             │
                                             ▼
                                      ┌─────────────┐
                                      │   MySQL     │
                                      │  Database   │
                                      └─────────────┘
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
| 控制层 (Controller) | **controller/** | 处理 API 请求，负责调用服务层并返回响应 |
| 控制器 | └── UserController.java | 提供用户注册、登录、获取信息的 API |
| 控制器 | └── PostController.java | 提供文章 CRUD API |
| 控制器 | └── FileController.java | 提供头像上传和更新 API |
| 控制器 | └── LikeController.java | 提供文章点赞 API |
| 控制器 | └── CommentController.java | 提供评论 CRUD API |
| 数据传输对象 | **dto/** | 定义请求和响应的数据模型 |
| DTO | └── UserRegistrationRequest.java | 用户注册请求体 |
| DTO | └── LoginRequest.java | 用户登录请求体 |
| DTO | └── PostRequest.java | 文章创建/更新请求体 |
| DTO | └── PostResponse.java | 文章响应体 |
| DTO | └── UserResponse.java | 用户信息响应体 |
| DTO | └── AvatarUrlRequest.java | 头像URL请求体 |
| DTO | └── JwtAuthenticationResponse.java | JWT 认证响应 |
| DTO | └── ErrorResponse.java | 标准化错误响应 |
| DTO | └── LikeResponse.java | 点赞响应体 |
| DTO | └── CommentRequest.java | 评论创建/更新请求体 |
| DTO | └── CommentResponse.java | 评论响应体 |
| 异常层 | **exception/** | 自定义异常类与全局异常处理 |
| 异常处理器 | └── GlobalExceptionHandler.java | 统一捕获和处理异常 |
| 异常类 | └── AccessDeniedException.java | 访问被拒绝异常 |
| 异常类 | └── ResourceNotFoundException.java | 资源未找到异常 |
| 映射层 | **mapper/** | 实体与 DTO 的转换工具 |
| 映射器 | └── UserMapper.java | User ↔ UserResponse 转换 |
| 映射器 | └── PostMapper.java | Post ↔ PostResponse 转换 |
| 映射器 | └── CommentMapper.java | Comment ↔ CommentResponse 转换 |
| 实体层 | **model/** | 数据库实体类 (JPA Entity) |
| 实体 | └── User.java | 用户实体 |
| 实体 | └── Post.java | 文章实体 |
| 实体 | └── Like.java | 点赞实体 |
| 实体 | └── Comment.java | 评论实体 |
| 枚举 | └── ContentType.java | 内容类型枚举 |
| 数据访问层 | **repository/** | 提供数据库操作接口 |
| Repository | └── UserRepository.java | 用户数据访问接口 |
| Repository | └── PostRepository.java | 文章数据访问接口 |
| Repository | └── LikeRepository.java | 点赞数据访问接口 |
| Repository | └── CommentRepository.java | 评论数据访问接口 |
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
| 实现类 | └── LikeServiceImpl.java | 点赞服务实现 |
| 接口 | └── CommentService.java | 评论服务接口 |
| 实现类 | └── CommentServiceImpl.java | 评论服务实现 |
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
- **Post**: 文章信息（标题、内容、作者、创建时间、是否草稿）
- **Like**: 点赞信息（用户、文章、创建时间）
- **Comment**: 评论信息（内容、用户、文章、创建时间、更新时间）

### 关系设计

- User ←─[一对多]─→ Post（一个用户可以有多篇文章）
- User ←─[一对多]─→ Like（一个用户可以点赞多篇文章）
- Post ←─[一对多]─→ Like（一篇文章可以有多个点赞）
- User ←─[一对多]─→ Comment（一个用户可以发表多条评论）
- Post ←─[一对多]─→ Comment（一篇文章可以有多条评论）

## 文件存储

### 头像文件组织

```
uploads/
  ├── {userId1}/
  │   └── avatars/
  │       └── {uuid}.jpg
  ├── {userId2}/
  │   └── avatars/
  │       └── {uuid}.png
  └── ...
```

### 优势

- 按用户隔离文件，便于管理
- 支持扩展其他文件类型
- 删除用户时可整体清理

## 前端架构

### 技术栈

- **Vue 3**: 渐进式框架
- **Vue Router**: 路由管理
- **Axios**: HTTP 客户端
- **TailwindCSS**: 原子化 CSS
- **Highlight.js**: 代码高亮

### 主要页面

- **Home**: 首页，展示文章列表
- **Login/Register**: 登录注册页面
- **Profile**: 用户个人中心
- **CreatePost**: 创建/编辑文章
- **PostDetail**: 文章详情

## 与原始方案的差异

### 功能差异

- **未实现**: 分类 (Category)、标签 (Tag)
- **新增**: "记住我"功能、JWT自动刷新、增强的Markdown编辑器、头像上传系统、文章点赞功能、评论功能

### 架构差异

- **认证入口**: 使用 UserController 而非 AuthController
- **配置文件**: 使用 application.properties 而非 application.yml
- **文档工具**: 未集成 Swagger

### 权限简化

- 当前基于资源所有权（用户只能操作自己的资源）
- 未实现基于角色的权限体系（如管理员、普通用户）
