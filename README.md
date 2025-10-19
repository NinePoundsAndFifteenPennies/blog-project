# Java 多用户博客系统

这是一个基于 Java Spring Boot 和前后端分离架构构建的多用户博客系统。项目从零开始，旨在实现一个功能完备、安全健壮、代码结构清晰的现代化 Web 应用。

## 项目简介

- **后端**: Spring Boot + Spring Security + JWT + JPA/Hibernate + MySQL
- **前端**: Vue.js + Axios + TailwindCSS
- **架构**: RESTful API + 前后端分离
- **特色**: JWT认证、"记住我"功能、Markdown编辑器、文章管理、头像上传、点赞功能、评论功能、评论点赞

## 核心功能

- ✅ 用户认证与授权（注册、登录、JWT、"记住我"、Token自动刷新）
- ✅ 文章管理（CRUD、分页、权限控制、草稿/发布）
- ✅ 增强的 Markdown 编辑器（工具栏、快捷键、代码高亮）
- ✅ 头像上传与管理（按用户分类存储、更新机制）
- ✅ 文章点赞功能（点赞/取消点赞、点赞数显示）
- ✅ 评论功能（CRUD、分页、权限控制、级联删除）
- ✅ 评论点赞功能（点赞/取消点赞、点赞数显示、匿名查看）
- ✅ 全局异常处理与标准化错误响应

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
- **[问题修复](docs/)** - 重要问题的修复说明
  - [头像显示问题修复](docs/AVATAR_DISPLAY_FIX.md) - 技术文档
  - [头像显示问题修复总结](docs/AVATAR_FIX_SUMMARY_CN.md) - 中文总结
  - [头像问题图解](docs/AVATAR_FIX_DIAGRAM.md) - 问题与解决方案对比图

## 项目进度

**当前状态**: 后端核心功能已完成，具备上线能力

**已完成**:
- 用户认证系统（注册、登录、JWT、自动刷新）
- 文章 CRUD 与权限控制
- 前后端完全对接
- 增强的 Markdown 编辑器
- 头像上传与管理系统
- 头像正确加载显示（修复开发环境CORS问题）✨修复
- 文章点赞功能
- 评论功能（CRUD、权限控制、级联删除）
- 评论点赞功能

**未来计划**:
- 文章分类和标签
- 文章搜索
- 用户个人资料编辑
- 代码高亮优化
- 响应式设计优化
- 评论回复功能

## 技术栈

### 后端
- Spring Boot 3.5.7
- Spring Security 6.5.6
- Spring Data JPA
- MySQL 8.0+
- JWT (io.jsonwebtoken)
- BCrypt 密码加密

### 前端
- Vue.js 3
- Vue Router
- Axios
- TailwindCSS
- Highlight.js

## 许可证

本项目仅用于学习和研究目的。

## 贡献

欢迎提交 Issue 和 Pull Request！
