# Java 多用户博客系统

这是一个基于 Java Spring Boot 和前后端分离架构构建的多用户博客系统后端。项目从零开始，旨在实现一个功能完备、安全健壮、代码结构清晰的现代化 Web 应用。

## 当前进度与核心功能

**后端项目已完成的核心功能开发(CRUD)，具备上线能力。**

-   **[✔️] 用户认证与授权**
    -   [✔️] 安全的用户注册（BCrypt密码加密，用户名/邮箱唯一性校验）
    -   [✔️] 灵活的用户登录（支持用户名或邮箱）
    -   [✔️] 基于 JWT (JSON Web Token) 的无状态认证系统
    -   [✔️] "记住我"功能（支持短期1小时和长期30天两种token有效期）
    -   [✔️] JWT自动刷新机制（token在5分钟内过期时自动刷新）
    -   [✔️] 细粒度的API权限控制（区分公开接口与受保护接口）

-   **[✔️] 文章管理 (CRUD)**
    -   [✔️] 文章的创建、读取、更新、删除功能
    -   [✔️] 文章与作者用户的多对一关联
    -   [✔️] 资源所有权校验（用户只能修改/删除自己的文章）
    -   [✔️] 支持富文本内容（Markdown/HTML），并记录内容类型

-   **[✔️] API & 数据处理**
    -   [✔️] 公开的文章列表分页查询接口（支持排序）
    -   [✔️] 清晰的 DTO (Data Transfer Object) 设计，确保API接口数据安全
    -   [✔️] 全局统一的异常处理和标准化的JSON错误响应

-   **[✔️] 架构与代码质量**
    -   [✔️] 清晰的三层架构（Controller -> Service -> Repository）
    -   [✔️] 通过 Mapper 组件实现实体与DTO的解耦
    -   [✔️] 遵循 RESTful API 设计风格

---

## 架构说明

### 1. 📂文件组织结构 (Backend)

后端的代码结构经过精心设计，遵循了高内聚、低耦合的原则。

| 类型 | 路径/文件 | 说明 |
|------|-----------|------|
| 启动类 | **BlogApplication.java** | Spring Boot 应用启动入口 |
| 配置层 | **config/** | 存放全局配置类，如安全配置 |
| 配置类 | └── SecurityConfig.java | Spring Security 核心配置 |
| 控制层 (Controller) | **controller/** | 处理 API 请求，负责调用服务层并返回响应 |
| 控制器 | └── UserController.java | 提供用户注册、登录、获取信息的 API |
| 控制器 | └── PostController.java | 提供文章 CRUD API |
| 数据传输对象 | **dto/** | 定义请求和响应的数据模型 |
| DTO | └── UserRegistrationRequest.java | 用户注册请求体 |
| DTO | └── LoginRequest.java | 用户登录请求体 |
| DTO | └── PostRequest.java | 文章创建/更新请求体 |
| DTO | └── PostResponse.java | 文章响应体 |
| DTO | └── JwtAuthenticationResponse.java | JWT 认证响应 |
| DTO | └── ErrorResponse.java | 标准化错误响应 |
| 异常层 | **exception/** | 自定义异常类与全局异常处理 |
| 异常处理器 | └── GlobalExceptionHandler.java | 统一捕获和处理异常 |
| 异常类 | └── AccessDeniedException.java | 访问被拒绝异常 |
| 异常类 | └── ResourceNotFoundException.java | 资源未找到异常 |
| 映射层 | **mapper/** | 实体与 DTO 的转换工具 |
| 映射器 | └── PostMapper.java | Post ↔ PostResponse 转换 |
| 实体层 | **model/** | 数据库实体类 (JPA Entity) |
| 实体 | └── User.java | 用户实体 |
| 实体 | └── Post.java | 文章实体 |
| 枚举 | └── ContentType.java | 内容类型枚举 |
| 数据访问层 | **repository/** | 提供数据库操作接口 |
| Repository | └── UserRepository.java | 用户数据访问接口 |
| Repository | └── PostRepository.java | 文章数据访问接口 |
| 安全层 | **security/** | 与认证和授权相关的工具类 |
| 工具类 | └── JwtTokenProvider.java | JWT 生成与验证 |
| 过滤器 | └── JwtAuthenticationFilter.java | 拦截并校验 JWT 请求 |
| 服务类 | └── CustomUserDetailsService.java | 加载用户信息 (Spring Security) |
| 服务层 | **service/** | 业务逻辑实现 |
| 接口 | └── UserService.java | 用户服务接口 |
| 实现类 | └── UserServiceImpl.java | 用户服务实现 |
| 接口 | └── PostService.java | 文章服务接口 |
| 实现类 | └── PostServiceImpl.java | 文章服务实现 |
| 配置文件 | **resources/** | 存放应用的资源文件 |
| 配置文件 | └── application.properties | 应用配置（数据库、JWT密钥等） |

---

### 2. API 接口注释

所有API都遵循RESTful设计原则。

| 功能 | Method | Endpoint | 认证 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| **用户** |
| 用户注册 | `POST` | `/api/users/register` | **公开** | 创建一个新用户。请求体: `{ username, password, email }` |
| 用户登录 | `POST` | `/api/users/login` | **公开** | 使用用户名或邮箱登录，返回JWT。请求体: `{ username, password, rememberMe }` |
| 获取当前用户 | `GET` | `/api/users/me` | **需要** | 获取当前登录用户的信息。返回: `"当前登录用户是: {username}"` |
| 刷新Token | `POST` | `/api/users/refresh-token` | **需要** | 刷新即将过期的JWT token。请求体: `{ rememberMe }` |
| **文章** |
| 创建文章 | `POST` | `/api/posts` | **需要** | 发布一篇新文章。请求体: `{ title, content, contentType }` |
| 获取文章列表 | `GET` | `/api/posts` | **公开** | 分页获取所有文章列表。支持分页参数: `page`, `size`, `sort` |
| 获取我的文章 | `GET` | `/api/posts/my` | **需要** | 获取当前用户的所有文章（包括草稿）。支持分页参数 |
| 获取单篇文章 | `GET` | `/api/posts/{id}` | **公开** | 根据ID获取文章详情。 |
| 更新文章 | `PUT` | `/api/posts/{id}` | **需要** | 更新一篇文章，仅作者可操作。请求体: `{ title, content, contentType }` |
| 删除文章 | `DELETE` | `/api/posts/{id}`| **需要** | 删除一篇文章，仅作者可操作。返回: `"文章删除成功"` |

---

## 本机开发与测试说明

为了方便在本机进行快速开发和测试，项目中存在一些简化配置，**这些配置不应用于生产环境**。

### 1. 数据库自动更新 (`ddl-auto`)

在 `application.properties` 文件中，配置了 `spring.jpa.hibernate.ddl-auto=update`。

* **作用**: 在应用启动时，Hibernate会自动比较Java实体类和数据库表的结构，并尝试进行更新（如添加新列）。
* **优点**: 在开发阶段，这极大地简化了数据库表的管理，无需手动编写和执行`ALTER TABLE`语句。
* **生产环境警告**: 在生产环境中，应将此配置改为 `validate` 或 `none`，并使用专业的数据库迁移工具（如 Flyway 或 Liquibase）来管理数据库结构变更，以避免潜在的数据丢失或结构损坏风险。

### 2. JWT 密钥设置

在 `application.properties` 文件中，JWT密钥和过期时间被硬编码：
```properties
app.jwt.secret=...
app.jwt.expiration-ms=3600000           # 普通登录1小时
app.jwt.remember-me-expiration-ms=2592000000  # 记住我30天
```

* **作用**: 为本地开发提供了一个即时可用的密钥和配置。
* **生产环境警告**: **绝对不能**将密钥硬编码在代码或配置文件中并提交到版本控制系统。在生产环境中，必须通过**环境变量**或安全的配置管理服务（如 Spring Cloud Config, HashiCorp Vault）来注入密钥。

### 3. 简化的安全权限

在 `SecurityConfig.java` 中，我们对文章相关的`GET`请求统一设置为公开访问。

* **作用**: 满足了博客系统浏览文章的基本需求。
* **可扩展说明**: 在更复杂的系统中，可能会有“私密文章”或“付费文章”等需求。届时，可以在`Post`实体中增加一个状态字段，并在`PostService`的`getPostById`等方法中加入更复杂的权限校验逻辑，而不是仅仅在`SecurityConfig`中进行URL级别的“一刀切”。



### 4. JWT Token 刷新测试

项目实现了JWT自动刷新机制，测试方法详见 `REMEMBER_ME_IMPLEMENTATION.md`。

**核心机制**:
- Token在5分钟内过期时，前端自动调用 `/api/users/refresh-token` 刷新
- 使用队列机制防止并发刷新
- Token完全过期后跳转登录页并显示提示

**快速测试步骤**:
1. 修改 `application.properties` 中的 `app.jwt.expiration-ms=360000`（6分钟）
2. 重启后端服务器
3. 登录后等待2分钟（剩余4分钟，满足<5分钟条件）
4. 访问任意需要认证的页面
5. 在浏览器Network标签中可看到自动调用 `/api/users/refresh-token`

---
## 与原始方案的差异及未完成部分

### 1. 功能模块差异
* **已实现核心功能**: 用户认证（注册、登录、JWT、"记住我"、Token自动刷新）、文章管理（CRUD、分页、权限控制）
* **前后端已对接**: 前端Vue项目已完成并与后端API完全对接，可正常运行
* **未实现功能**: 原始方案中规划的 **评论 (Comment)**、**分类 (Category)** 和 **标签 (Tag)** 功能及其相关API在本期开发中并未实现，数据库中也仅创建了`users`和`posts`两张核心表

### 2. API 路径与文档
* **路径差异**: 用户认证相关的API路径由原计划的 `/auth/` 调整为 `/api/users/`，以保持资源路径风格的统一
* **新增接口**: 添加了 `/api/users/refresh-token` 接口支持JWT自动刷新
* **新增接口**: 添加了 `/api/posts/my` 接口获取当前用户的所有文章
* **文档工具**: 原计划推荐使用 **Swagger** 自动生成API文档，此功能尚未集成 

### 3. 架构与目录结构差异
- **原计划**：后端目录结构中使用 `AuthController` 与 `AuthService` 作为用户认证的核心入口，配置文件采用 `application.yml`，并预留了 `data.sql` 初始化脚本以及 `static/` 静态资源目录
- **实际实现**：项目采用了 `UserController` 与 `UserService` 替代认证入口，配置文件为 `application.properties`，未引入数据库初始化脚本和 `static/` 目录，整体结构更简化

### 4. 权限模型
* **权限简化**: 当前的权限系统基于资源所有权（即用户只能操作自己的文章），尚未实现原计划中更复杂的基于角色（如管理员、普通用户）的权限体系

### 5. 未完成部分
- 评论、分类、标签等数据库表与实体类的实现
- 评论管理、文章分类管理、标签管理相关的 API 接口
- Swagger 或 OpenAPI 文档集成
- 数据库初始化脚本 (`data.sql`) 及更完善的迁移工具支持（如 Flyway/Liquibase）
- 更细粒度的角色与权限体系设计（如管理员、普通用户区分）

### 6. 生产环境部署支持
- ✅ 已完成：添加了生产环境配置文件和部署支持
- ✅ 已完成：Docker 容器化方案（Dockerfile 和 docker-compose.yml）
- ✅ 已完成：完整的部署文档和环境变量配置
- 未完成：CI/CD 流水线自动化部署


---

## 生产环境部署

项目已支持生产环境部署，提供了多种部署方式。详细的部署指南请参阅 [DEPLOYMENT.md](DEPLOYMENT.md)。

### 快速开始

#### 方式一：使用 Docker Compose（推荐）

1. 复制环境变量示例文件：
```bash
cp .env.docker.example .env
```

2. 编辑 `.env` 文件，设置数据库密码和 JWT 密钥：
```bash
# 生成 JWT 密钥
openssl rand -base64 64

# 编辑 .env 文件，填入生成的密钥和其他配置
```

3. 构建并启动所有服务：
```bash
# 首先构建后端
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 启动所有服务
docker-compose up -d
```

4. 访问应用：
- 前端：http://localhost
- 后端 API：http://localhost:8080/api

#### 方式二：传统部署

1. 后端部署：
```bash
cd backend/blog

# 复制环境变量示例
cp .env.example .env

# 编辑 .env 文件
nano .env

# 构建应用
./mvnw clean package -DskipTests

# 使用生产配置启动
./start-prod.sh
```

2. 前端部署：
```bash
cd frontend

# 构建生产版本
npm run build

# 使用 Nginx 或其他 Web 服务器托管 dist/ 目录
```

详细步骤请参考 [DEPLOYMENT.md](DEPLOYMENT.md)。

### 生产环境配置要点

1. **数据库配置**：通过环境变量 `DATABASE_URL`、`DATABASE_USERNAME`、`DATABASE_PASSWORD` 配置
2. **JWT 密钥**：必须通过环境变量 `JWT_SECRET` 提供安全的密钥
3. **Hibernate DDL**：生产环境使用 `validate` 模式，避免自动修改数据库结构
4. **日志级别**：生产环境日志级别设置为 INFO
5. **前端 API**：支持通过环境变量 `VUE_APP_API_BASE_URL` 配置后端地址

---

## 下一步任务

**Milestone 1 已完成**：
- ✅ 后端CRUD API全部实现（用户认证、文章管理、分页查询）
- ✅ 前端页面框架构建完成（所有页面、组件、样式、交互）
- ✅ 前后端API对接完成
- ✅ "记住我"功能和JWT自动刷新机制实现
- ✅ 实现了相对完整的网页程序（注册登录、增删改查文章、分页显示文章）

**后续开发计划**：
- 评论功能（Comment实体、API、前端UI）
- 文章分类和标签功能
- 文章搜索功能
- 用户个人资料编辑
- 头像上传
- 更丰富的Markdown编辑器（预览、工具栏）
- 代码高亮优化
- 响应式设计优化