# 博客系统前端项目

基于 Vue 3 + Tailwind CSS 构建的现代化多用户博客系统前端。

## ✨ 核心特性

### 用户端
- 🎨 **现代化UI设计** - 响应式布局,毛玻璃效果,流畅动画
- 🔐 **完整认证系统** - JWT认证,"记住我",Token自动刷新
- ✍️ **Markdown编辑器** - 实时预览,代码高亮
- 💬 **社交功能** - 评论系统,点赞功能,子评论/回复,**用户关注**,**私信功能**
- 🏷️ **内容组织** - 标签系统,分类功能,文章搜索
- 👤 **用户系统** - 个人主页,资料编辑,头像上传,悬浮卡片,**关注/粉丝列表**
- 📊 **社区统计** - 用户总数,文章总数,在线用户,今日访问
- 🔥 **热门作者榜单** - "品"字形布局,热度算法排名,悬浮卡片关注
- 🔒 **隐私控制** - 关注信息可见性设置
- 💌 **私信系统** - 实时聊天,防骚扰机制,已读状态,未读提醒

### 管理后台
- 🛡️ **RBAC权限控制** - 基于角色的访问控制（USER/ADMIN）
- 📊 **仪表盘** - 统计卡片,趋势指标,动态欢迎动画
- 📝 **内容管理** - 文章、分类、标签、评论管理
- 🖼️ **媒体库** - 文件管理,网格视图
- 👥 **用户管理** - 用户列表,多条件搜索,角色管理,批量操作,启用/禁用
- ⚙️ **系统设置** - 表单配置,开关控制

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **路由**: Vue Router 4
- **状态管理**: Vuex 4
- **HTTP客户端**: Axios + JWT认证拦截
- **样式**: Tailwind CSS 3
- **Markdown**: marked + highlight.js
- **构建**: Vue CLI 5

## 📦 项目结构

```
frontend/
├── public/                     # 静态资源
├── src/
│   ├── api/                    # API接口封装
│   │   ├── auth.js             # 认证相关API
│   │   ├── posts.js            # 文章相关API
│   │   ├── comments.js         # 评论相关API
│   │   ├── follow.js           # 关注功能API
│   │   ├── messages.js         # 私信功能API
│   │   ├── authors.js          # 热门作者API
│   │   └── admin.js            # 管理后台API
│   ├── assets/                 # 全局样式
│   ├── components/             # 公共组件（前后台通用）
│   │   ├── Header.vue          # 导航栏(含未读私信提醒,管理后台入口)
│   │   ├── PostCard.vue        # 文章卡片(支持列表/网格视图)
│   │   ├── CommentList.vue     # 评论列表
│   │   ├── TagBadge.vue        # 标签徽章
│   │   ├── PopularTags.vue     # 热门标签
│   │   ├── CommunityStats.vue  # 社区统计
│   │   ├── UserProfileHoverCard.vue # 用户悬浮卡片
│   │   ├── FollowButton.vue    # 关注/取消关注按钮
│   │   ├── FollowStats.vue     # 关注统计(关注数/粉丝数/朋友数)
│   │   └── HotAuthors.vue      # 热门作者榜单("品"字形布局)
│   ├── layouts/                # 布局组件
│   │   └── AdminLayout.vue     # 管理后台通用布局
│   ├── modules/                # 模块化目录
│   │   ├── admin/
│   │   │   ├── components/     # 后台专用组件
│   │   │   ├── views/          # 后台页面
│   │   │   └── router.js       # 后台路由
│   │   └── portal/
│   │       ├── components/     # 前台专用组件
│   │       ├── views/          # 前台页面
│   │       └── router.js       # 前台路由
│   ├── router/                 # 主路由入口（合并 portal/admin 路由）
│   ├── store/                  # Vuex状态(认证、用户)
│   ├── utils/                  # 工具函数(Axios封装等)
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── tailwind.config.js          # Tailwind配置
└── vue.config.js               # Vue CLI配置(API代理)
```

## 🚀 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器 (http://localhost:3000)
npm run serve

# 生产构建
npm run build
```

## 🔐 管理后台

管理后台需要 ADMIN 角色才能访问。

### 访问入口

1. 使用管理员账户登录（统一登录页面）
2. 登录后在右上角用户菜单中点击"管理后台"
3. 或直接访问 `/admin` 路由

### 功能模块

| 模块 | 功能 |
|------|------|
| 仪表盘 | 统计卡片、趋势指标、最新文章、动态欢迎动画 |
| 文章管理 | 文章列表、多条件筛选（状态/作者/标签/日期）、批量审核/拒绝/删除、状态管理 |
| 分类管理 | 分类卡片网格、新建分类 |
| 标签管理 | 标签列表、使用统计 |
| 评论管理 | 评论审核、删除 |
| 媒体库 | 文件网格、上传 |
| 用户管理 | 用户列表、多条件搜索、角色管理、批量启用/禁用、批量设置管理员 |
| 系统设置 | 网站配置、评论设置 |

### 设置管理员

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_admin_username';
```
