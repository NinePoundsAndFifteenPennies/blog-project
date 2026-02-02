# 博客系统前端项目

基于 Vue 3 + Tailwind CSS 构建的现代化多用户博客系统前端。

## ✨ 核心特性

- 🎨 **现代化UI设计** - 响应式布局,毛玻璃效果,流畅动画
- 🔐 **完整认证系统** - JWT认证,"记住我",Token自动刷新
- ✍️ **Markdown编辑器** - 实时预览,代码高亮
- 💬 **社交功能** - 评论系统,点赞功能,子评论/回复,**用户关注**,**私信功能**
- 🏷️ **内容组织** - 标签系统,分类功能,文章搜索
- 👤 **用户系统** - 个人主页,资料编辑,头像上传,悬浮卡片,**关注/粉丝列表**
- 📊 **社区统计** - 用户总数,文章总数,在线用户,今日访问
- 🔒 **隐私控制** - 关注信息可见性设置
- 💌 **私信系统** - 实时聊天,防骚扰机制,已读状态,未读提醒

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
├── public/                 # 静态资源
├── src/
│   ├── api/                # API接口封装
│   │   ├── auth.js         # 认证相关API
│   │   ├── posts.js        # 文章相关API
│   │   ├── comments.js     # 评论相关API
│   │   ├── follow.js       # 关注功能API
│   │   └── messages.js     # 私信功能API
│   ├── assets/             # 全局样式
│   ├── components/         # 可复用组件
│   │   ├── Header.vue      # 导航栏(含未读私信提醒)
│   │   ├── PostCard.vue    # 文章卡片(支持列表/网格视图)
│   │   ├── CommentList.vue # 评论列表
│   │   ├── TagBadge.vue    # 标签徽章
│   │   ├── PopularTags.vue # 热门标签
│   │   ├── CommunityStats.vue # 社区统计
│   │   ├── UserProfileHoverCard.vue # 用户悬浮卡片
│   │   ├── FollowButton.vue # 关注/取消关注按钮
│   │   └── FollowStats.vue  # 关注统计(关注数/粉丝数/朋友数)
│   ├── router/             # 路由配置+导航守卫
│   ├── store/              # Vuex状态(认证、用户)
│   ├── utils/              # 工具函数(Axios封装等)
│   ├── views/              # 页面组件
│   │   ├── Home.vue        # 首页(热门文章+最新文章)
│   │   ├── PostDetail.vue  # 文章详情
│   │   ├── PostEdit.vue    # 文章编辑
│   │   ├── Profile.vue     # 个人中心
│   │   ├── UserProfile.vue # 用户主页
│   │   ├── Settings.vue    # 设置页面(入口)
│   │   ├── FollowList.vue  # 关注/粉丝/朋友列表(含私信入口)
│   │   ├── Messages.vue    # 私信页面(会话列表+聊天界面)
│   │   └── VisibilitySettings.vue # 隐私设置
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── tailwind.config.js      # Tailwind配置
└── vue.config.js           # Vue CLI配置(API代理)
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