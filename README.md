# Java 多用户博客系统

这是一个基于 Java Spring Boot 和前后端分离架构构建的多用户博客系统后端。项目从零开始，旨在实现一个功能完备、安全健壮、代码结构清晰的现代化 Web 应用。

## 当前进度与核心功能

**后端项目已完成的核心功能开发(CRUD)，具备上线能力。**

-   **[✔️] 用户认证与授权**
    -   [✔️] 安全的用户注册（BCrypt密码加密，用户名/邮箱唯一性校验）
    -   [✔️] 灵活的用户登录（支持用户名或邮箱）
    -   [✔️] 基于 JWT (JSON Web Token) 的无状态认证系统
    -   [✔️] 细粒度的API权限控制（区分公开接口与受保护接口）

-   **[✔️] 文章管理 (CRUD)**
    -   [✔️] 文章的创建、读取、更新、删除功能
    -   [✔️] 文章与作者用户的多对一关联
    -   [✔️] 资源所有权校验（用户只能修改/删除自己的文章）
    -   [✔️] 支持富文本内容（Markdown/HTML），并记录内容类型
    -   [✔️] 草稿功能（文章可保存为草稿或发布状态，记录发布时间）

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
| DTO | └── PostRequest.java | 文章创建/更新请求体（含标题、内容、内容类型、草稿状态） |
| DTO | └── PostResponse.java | 文章响应体（含ID、标题、内容、作者、时间戳、草稿状态等） |
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
| 实体 | └── Post.java | 文章实体（含标题、内容、作者、时间戳、草稿状态、发布时间） |
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
| 用户注册 | `POST` | `/api/users/register` | **公开** | 创建一个新用户。 |
| 用户登录 | `POST` | `/api/users/login` | **公开** | 使用用户名或邮箱登录，返回JWT。 |
| 获取当前用户 | `GET` | `/api/users/me` | **需要** | 获取当前登录用户的信息。 |
| **文章** |
| 创建文章 | `POST` | `/api/posts` | **需要** | 发布一篇新文章。 |
| 获取文章列表 | `GET` | `/api/posts` | **公开** | 分页获取所有文章列表。 |
| 获取单篇文章 | `GET` | `/api/posts/{id}` | **公开** | 根据ID获取文章详情。 |
| 更新文章 | `PUT` | `/api/posts/{id}` | **需要** | 更新一篇文章，仅作者可操作。 |
| 删除文章 | `DELETE` | `/api/posts/{id}`| **需要** | 删除一篇文章，仅作者可操作。 |

**重要说明：实际API响应格式**

- **登录响应** (`POST /api/users/login`)：返回格式为 `{ "accessToken": "jwt_token", "tokenType": "Bearer" }`
- **获取当前用户** (`GET /api/users/me`)：返回纯文本字符串 `"当前登录用户是: username"`，前端需解析提取用户名
- **文章列表** (`GET /api/posts`)：返回 Spring Data 标准分页对象 `Page<PostResponse>`
- **文章对象** (`PostResponse`)：包含 `draft`（草稿状态）、`publishedAt`（发布时间）、`contentType`（内容类型：MARKDOWN/HTML）等字段

---

## 本机开发与测试说明

为了方便在本机进行快速开发和测试，项目中存在一些简化配置，**这些配置不应用于生产环境**。

### 1. 数据库自动更新 (`ddl-auto`)

在 `application.properties` 文件中，配置了 `spring.jpa.hibernate.ddl-auto=update`。

* **作用**: 在应用启动时，Hibernate会自动比较Java实体类和数据库表的结构，并尝试进行更新（如添加新列）。
* **优点**: 在开发阶段，这极大地简化了数据库表的管理，无需手动编写和执行`ALTER TABLE`语句。
* **生产环境警告**: 在生产环境中，应将此配置改为 `validate` 或 `none`，并使用专业的数据库迁移工具（如 Flyway 或 Liquibase）来管理数据库结构变更，以避免潜在的数据丢失或结构损坏风险。

### 2. JWT 密钥设置

在 `application.properties` 文件中，JWT密钥被硬编码：`app.jwt.secret=...`

* **作用**: 为本地开发提供了一个即时可用的密钥。
* **生产环境警告**: **绝对不能**将密钥硬编码在代码或配置文件中并提交到版本控制系统。在生产环境中，必须通过**环境变量**或安全的配置管理服务（如 Spring Cloud Config, HashiCorp Vault）来注入密钥。

### 3. 简化的安全权限

在 `SecurityConfig.java` 中，我们对文章相关的`GET`请求统一设置为公开访问。

* **作用**: 满足了博客系统浏览文章的基本需求。
* **可扩展说明**: 在更复杂的系统中，可能会有“私密文章”或“付费文章”等需求。届时，可以在`Post`实体中增加一个状态字段，并在`PostService`的`getPostById`等方法中加入更复杂的权限校验逻辑，而不是仅仅在`SecurityConfig`中进行URL级别的“一刀切”。


## 与原始方案的差异及未完成部分

### 1. 功能模块差异
* **未实现功能**: 原始方案中规划的 **评论 (Comment)**、**分类 (Category)** 和 **标签 (Tag)** 功能及其相关API在本期开发中并未实现，数据库中也仅创建了`users`和`posts`两张核心表。
* **已实现的新增功能**: Post表新增了**草稿功能**（`draft`字段）和**发布时间追踪**（`publishedAt`字段），允许用户保存草稿并在发布时记录时间戳。

### 2. API 路径与文档
* **路径差异**: 用户认证相关的API路径由原计划的 `/auth/` 调整为 `/api/users/`，以保持资源路径风格的统一。
* **响应格式差异**: 部分API的实际响应格式与前端README中的示例不同，详见"API 接口注释"部分的说明。
* **文档工具**: 原计划推荐使用 **Swagger** 自动生成API文档，此功能尚未集成。 

### 3. 架构与目录结构差异
- **原计划**：后端目录结构中使用 `AuthController` 与 `AuthService` 作为用户认证的核心入口，配置文件采用 `application.yml`，并预留了 `data.sql` 初始化脚本以及 `static/` 静态资源目录。
- **实际实现**：项目采用了 `UserController` 与 `UserService` 替代认证入口，配置文件为 `application.properties`，未引入数据库初始化脚本和 `static/` 目录，整体结构更简化。

### 4. 权限模型
* **权限简化**: 当前的权限系统基于资源所有权（即用户只能操作自己的文章），尚未实现原计划中更复杂的基于角色（如管理员、普通用户）的权限体系。

### 5. 未完成部分
- 评论、分类、标签等数据库表与实体类的实现。
- 评论管理、文章分类管理、标签管理相关的 API 接口。
- Swagger 或 OpenAPI 文档集成。
- 数据库初始化脚本 (`data.sql`) 及更完善的迁移工具支持（如 Flyway/Liquibase）。
- 更细粒度的角色与权限体系设计（如管理员、普通用户区分）。
- 部署层面的 Docker 容器化方案与 CI/CD 流水线。


## 前端开发进度

### 已完成
-   **[✔️] 基础框架搭建** - 基于 Vue 3 + Tailwind CSS + Vue Router + Vuex
-   **[✔️] 页面组件** - 已创建主要页面组件（Home.vue, Login.vue, Register.vue, PostDetail.vue, PostEdit.vue, Profile.vue, NotFound.vue）
-   **[✔️] 公共组件** - Header导航栏、PostCard文章卡片、Pagination分页组件
-   **[✔️] API封装** - 已在 `src/api/` 目录下封装认证和文章相关接口，并适配后端实际格式
-   **[✔️] 路由配置** - 已配置路由守卫和权限控制
-   **[✔️] 状态管理** - 已配置 Vuex 状态管理（用户认证、token管理）
-   **[✔️] 样式系统** - Tailwind CSS配置完成，支持响应式布局和毛玻璃效果

### 已知问题
-   **[⚠️] API对接问题** - 部分页面中的API调用逻辑尚未完全实现（标记为 TODO），需要补充后端API对接代码
-   **[⚠️] 数据格式适配** - 前端部分组件可能仍在使用示例数据格式，需要与后端实际返回格式（如分页对象、认证响应等）进行适配
-   **[⚠️] 错误处理** - 部分边界情况和错误处理逻辑需要完善
-   **[⚠️] 功能缺失** - 用户文章列表功能（`GET /api/users/:userId/posts`）后端接口未实现，前端对应功能无法使用

详细的前端说明请参考 `frontend/README.md`。


## 下一步任务

### 短期目标（前端完善）
1. **完成前端API对接** - 补充所有标记为 TODO 的后端API调用逻辑
2. **修复已知Bug** - 解决数据格式适配问题和错误处理逻辑
3. **功能测试** - 对注册、登录、文章增删改查、分页显示等功能进行完整测试
4. **用户体验优化** - 完善加载状态、错误提示、表单验证等交互细节

### 中期目标（功能扩展）
1. **评论系统** - 实现文章评论功能（后端表结构、API、前端UI）
2. **分类与标签** - 实现文章分类和标签管理
3. **个人中心完善** - 实现用户文章列表功能（需要后端支持 `GET /api/users/:userId/posts`）
4. **富文本编辑器增强** - 优化 Markdown 编辑体验，支持图片上传等

### 长期目标（系统完善）
1. **API文档** - 集成 Swagger/OpenAPI 自动生成API文档
2. **角色权限** - 实现基于角色的权限体系（管理员、普通用户）
3. **数据库迁移** - 引入 Flyway/Liquibase 管理数据库版本
4. **部署优化** - Docker 容器化、CI/CD 流水线、生产环境配置