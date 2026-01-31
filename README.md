# Java 多用户博客系统

这是一个基于 Java Spring Boot 和前后端分离架构构建的多用户博客系统。项目从零开始，旨在实现一个功能完备、安全健壮、代码结构清晰的现代化 Web 应用。

## 项目简介

- **后端**: Spring Boot + Spring Security + JWT + JPA/Hibernate + MySQL
- **前端**: Vue.js + Axios + TailwindCSS
- **架构**: RESTful API + 前后端分离
- **特色**: JWT认证、Markdown编辑器、文章管理、用户关注、**私信功能**

## 核心功能

- ✅ 用户认证与授权（注册、登录、JWT、Token自动刷新）
- ✅ 文章管理（CRUD、分页、权限控制、草稿/发布）
- ✅ 文章搜索功能（多维度搜索、关键词高亮）
- ✅ Markdown 编辑器（工具栏、快捷键、代码高亮）
- ✅ 头像上传与管理
- ✅ 文章封面图片
- ✅ 点赞功能（文章和评论）
- ✅ 评论功能（CRUD、分页、子评论/回复）
- ✅ 文章标签与分类功能
- ✅ 用户个人资料编辑
- ✅ 用户公开主页
- ✅ **用户关注功能**（关注/取消关注、朋友检测、可见性控制）
- ✅ **私信功能**（实时聊天、防骚扰机制、已读状态）

## 快速开始

### 后端运行

```bash
cd backend/blog
mvn spring-boot:run
# 访问 http://localhost:8080
```

### 前端运行

```bash
cd frontend
npm install
npm run serve
# 访问 http://localhost:3000
```

## 文档导航

- **[架构说明](docs/ARCHITECTURE.md)** - 项目架构、代码结构、设计模式
- **[API 文档](docs/API.md)** - 完整的 API 接口文档

## 技术栈

### 后端
- Spring Boot 3.5.7
- Spring Security 6.5.6
- Spring Data JPA
- MySQL 8.0+
- JWT

### 前端
- Vue.js 3
- Vue Router
- Axios
- TailwindCSS

## 许可证

本项目仅用于学习和研究目的。
