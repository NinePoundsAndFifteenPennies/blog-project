# Java 多用户博客系统

这是一个基于 Java Spring Boot 和前后端分离架构构建的多用户博客系统。项目从零开始，旨在实现一个功能完备、安全健壮、代码结构清晰的现代化 Web 应用。

## 项目简介

- **后端**: Spring Boot + Spring Security + JWT + JPA/Hibernate + MySQL
- **前端**: Vue.js + Axios + TailwindCSS
- **架构**: RESTful API + 前后端分离
- **特色**: JWT认证、"记住我"功能、Markdown编辑器、文章管理、头像上传、点赞功能、评论系统

## 核心功能

**用户认证与授权**
- 安全的用户注册（BCrypt密码加密，用户名/邮箱唯一性校验）
- 灵活的用户登录（支持用户名或邮箱）
- 基于 JWT 的无状态认证系统
- "记住我"功能（支持短期1小时和长期30天两种token有效期）
- JWT自动刷新机制（token在5分钟内过期时自动刷新）
- 细粒度的API权限控制（区分公开接口与受保护接口）

**文章管理**
- 完整的 CRUD 功能（创建、读取、更新、删除）
- 文章与作者用户的多对一关联
- 资源所有权校验（用户只能修改/删除自己的文章）
- 支持富文本内容（Markdown/HTML）
- 增强的 Markdown 编辑器（工具栏、快捷键、代码高亮）
- 公开的文章列表分页查询接口（支持排序）
- 草稿/发布状态管理

**社交功能**
- 文章点赞功能（点赞/取消点赞、点赞数显示）
- 评论功能（CRUD、分页、权限控制、级联删除）
- 评论点赞功能（点赞/取消点赞、点赞数显示、匿名查看）
- 子评论功能（嵌套回复、@用户、层级限制、级联删除）
- 子评论点赞功能（完整的点赞系统）

**用户个性化**
- 头像上传与管理（按用户分类存储、更新机制）

**架构与代码质量**
- 清晰的三层架构（Controller -> Service -> Repository）
- 通过 Mapper 组件实现实体与DTO的解耦
- 遵循 RESTful API 设计风格
- 全局统一的异常处理和标准化的JSON错误响应

## 快速开始

### 后端运行

1. 确保已安装 JDK 17+ 和 MySQL 8.0+
2. 创建数据库：`CREATE DATABASE blog_db;`
3. 修改配置文件 `backend/blog/src/main/resources/application.properties`
4. 运行后端：
```bash
   cd backend/blog
   mvn spring-boot:run
```
5. 后端服务运行在 `http://localhost:8080`

### 前端运行

1. 确保已安装 Node.js 16+
2. 安装依赖并运行：
```bash
   cd frontend
   npm install
   npm run dev
```
3. 前端服务运行在 `http://localhost:5173`

## 生产环境部署

系统已支持使用 Docker 一键部署到生产服务器。

**部署文档**：
- **[DOCKER部署指南.md](./DOCKER部署指南.md)** - 完整的 Docker 部署教程
- **[日常维护.md](./日常维护.md)** - 部署后的日常运维操作

**快速部署**：
```bash
# 1. 克隆项目
git clone https://github.com/NinePoundsAndFifteenPennies/blog-project.git 
cd blog-project

# 2. 配置环境变量
cp .env.docker.example .env
nano .env  # 修改数据库密码、JWT密钥等

# 3. 构建后端
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 4. 启动所有服务
docker-compose up -d

# 5. 查看日志
docker-compose logs -f
```

## 本地开发注意事项

为了方便在本机进行快速开发和测试，项目中存在一些简化配置，**这些配置不应用于生产环境**：

1. **数据库自动更新**：配置了 `spring.jpa.hibernate.ddl-auto=update`，生产环境应改为 `validate` 或使用专业的数据库迁移工具

2. **JWT 密钥设置**：JWT密钥硬编码在 `application.properties` 中，生产环境必须通过环境变量注入

3. **简化的安全权限**：文章相关的GET请求统一设置为公开访问，复杂场景需要更细粒度的权限控制

## 文档导航

- **[架构说明](docs/ARCHITECTURE.md)** - 项目架构、代码结构、设计模式
- **[API 文档](docs/API.md)** - 完整的 API 接口文档
- **[开发说明](docs/DEVELOPMENT.md)** - 本地开发、测试、注意事项
- **[功能特性](docs/features/)** - 各功能模块的详细说明
  - [编辑器改进](docs/features/EDITOR.md)
  - [记住我功能](docs/features/REMEMBER_ME.md)
  - [头像上传](docs/features/AVATAR_UPLOAD.md)
  - [点赞功能](docs/features/LIKES.md)
  - [评论功能](docs/features/COMMENTS.md)
  - [子评论功能](docs/features/SUB_COMMENTS.md)

## API 接口概览

**用户相关**
- `POST /api/users/register` - 用户注册（公开）
- `POST /api/users/login` - 用户登录（公开）
- `GET /api/users/me` - 获取当前用户信息（需要认证）
- `POST /api/users/refresh-token` - 刷新Token（需要认证）

**文章相关**
- `POST /api/posts` - 创建文章（需要认证）
- `GET /api/posts` - 获取文章列表（公开，支持分页）
- `GET /api/posts/my` - 获取我的文章（需要认证）
- `GET /api/posts/{id}` - 获取文章详情（公开）
- `PUT /api/posts/{id}` - 更新文章（需要认证，仅作者）
- `DELETE /api/posts/{id}` - 删除文章（需要认证，仅作者）

**点赞相关**
- `POST /api/posts/{postId}/like` - 点赞文章（需要认证）
- `DELETE /api/posts/{postId}/like` - 取消点赞（需要认证）

**评论相关**
- `POST /api/posts/{postId}/comments` - 创建评论（需要认证）
- `GET /api/posts/{postId}/comments` - 获取评论列表（公开，支持分页）
- `PUT /api/comments/{id}` - 更新评论（需要认证，仅作者）
- `DELETE /api/comments/{id}` - 删除评论（需要认证，仅作者）
- `POST /api/comments/{commentId}/like` - 点赞评论（需要认证）
- `DELETE /api/comments/{commentId}/like` - 取消点赞（需要认证）

**子评论相关**
- `POST /api/comments/{parentId}/replies` - 创建回复（需要认证）
- `GET /api/comments/{parentId}/replies` - 获取回复列表（公开，支持分页）
- `POST /api/sub-comments/{subCommentId}/like` - 点赞回复（需要认证）
- `DELETE /api/sub-comments/{subCommentId}/like` - 取消点赞（需要认证）

详细的API文档请参考 [API.md](docs/API.md)

## 项目进度

**已完成**：
- ✅ 用户认证系统（注册、登录、JWT、自动刷新）
- ✅ 文章 CRUD 与权限控制
- ✅ 增强的 Markdown 编辑器
- ✅ 头像上传与管理系统
- ✅ 文章点赞功能
- ✅ 评论功能（CRUD、权限控制、级联删除）
- ✅ 评论点赞功能
- ✅ 后端子评论（回复）及其点赞功能

**未来计划**：
- 子评论功能前端对接（后端已完成）
- 文章分类和标签
- 文章搜索
- 用户个人资料编辑
- 代码高亮优化
- 响应式设计优化

## 与原始方案的差异

**功能模块差异**
- 已实现核心功能：用户认证、文章管理、点赞、评论、子评论
- 未实现功能：分类 (Category) 和标签 (Tag) 功能

**API 路径调整**
- 用户认证相关 API 从 `/auth/` 调整为 `/api/users/`
- 新增 `/api/users/refresh-token` 接口支持JWT自动刷新
- 新增 `/api/posts/my` 接口获取当前用户的所有文章

**未完成部分**
- Swagger 或 OpenAPI 文档集成
- 数据库初始化脚本及迁移工具支持（如 Flyway/Liquibase）
- 更细粒度的角色与权限体系设计（如管理员、普通用户区分）
- CI/CD 流水线自动化部署

## 技术栈

**后端**
- Spring Boot 3.5.7
- Spring Security 6.5.6
- Spring Data JPA
- MySQL 8.0+
- JWT (io.jsonwebtoken)
- BCrypt 密码加密

**前端**
- Vue.js 3
- Vue Router
- Axios
- TailwindCSS
- Highlight.js

## 许可证

本项目仅用于学习和研究目的。

## 贡献

欢迎提交 Issue 和 Pull Request！