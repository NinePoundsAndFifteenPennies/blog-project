# Java 多用户博客系统

这是一个基于 Java Spring Boot 和前后端分离架构构建的多用户博客系统。项目从零开始，旨在实现一个功能完备、安全健壮、代码结构清晰的现代化 Web 应用。

## 项目简介

- **后端**: Spring Boot + Spring Security + JWT + JPA/Hibernate + MySQL
- **前端**: Vue.js + Axios + TailwindCSS
- **架构**: RESTful API + 前后端分离
- **特色**: JWT认证、Markdown编辑器、文章管理、用户关注、私信功能、通知中心、**热门作者榜单**、**管理后台**

## 核心功能

### 用户端功能
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
- ✅ 用户关注功能（关注/取消关注、朋友检测、可见性控制）
- ✅ 私信功能（实时聊天、防骚扰机制、已读状态）
- ✅ 通知中心（收件箱功能、未读计数、类型过滤、快捷操作）
- ✅ 热门作者榜单（加权对数混合模型、"品"字形布局、热度排名）

### 管理后台功能
- ✅ RBAC 权限管理（USER/ADMIN 角色）
- ✅ 统一登录（根据角色自动跳转）
- ✅ 管理员仪表盘（统计卡片、趋势指标）
- ✅ 文章管理视图（筛选、表格、状态徽章）
- ✅ 分类管理视图（卡片网格）
- ✅ 标签管理视图（标签列表）
- ✅ 评论管理视图（审核、删除）
- ✅ 媒体库视图（文件网格）
- ✅ 用户管理视图（角色、状态）
- ✅ 系统设置视图（表单、开关）

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

### 设置管理员

```sql
-- 将用户提升为管理员
UPDATE users SET role = 'ADMIN' WHERE username = 'your_admin_username';
```

## 文档导航

- **[架构说明](docs/ARCHITECTURE.md)** - 项目架构、代码结构、设计模式
- **[API 文档](docs/API.md)** - 用户端 API 接口文档
- **[管理后台 API](docs/ADMIN_API.md)** - 管理后台 API 接口文档

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
