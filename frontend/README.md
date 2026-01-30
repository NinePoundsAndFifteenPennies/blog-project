# 博客系统前端项目

基于 Vue 3 + Tailwind CSS 构建的现代化多用户博客系统前端。

## ✨ 核心特性

- 🎨 **现代化UI设计** - 响应式布局,毛玻璃效果,流畅动画
- 🔐 **完整认证系统** - JWT认证,"记住我",Token自动刷新
- ✍️ **Markdown编辑器** - 实时预览,代码高亮
- 💬 **社交功能** - 评论系统,点赞功能,子评论/回复
- 🏷️ **内容组织** - 标签系统,分类功能,文章搜索
- 👤 **用户系统** - 个人主页,资料编辑,头像上传,悬浮卡片
- 📊 **社区统计** - 用户总数,文章总数,在线用户,今日访问

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
│   ├── assets/             # 全局样式
│   ├── components/         # 可复用组件
│   │   ├── Header.vue      # 导航栏
│   │   ├── PostCard.vue    # 文章卡片(支持列表/网格视图)
│   │   ├── CommentList.vue # 评论列表
│   │   ├── TagBadge.vue    # 标签徽章
│   │   ├── PopularTags.vue # 热门标签
│   │   ├── CommunityStats.vue # 社区统计
│   │   └── UserProfileHoverCard.vue # 用户悬浮卡片
│   ├── router/             # 路由配置+导航守卫
│   ├── store/              # Vuex状态(认证、用户)
│   ├── utils/              # 工具函数(Axios封装等)
│   ├── views/              # 页面组件
│   │   ├── Home.vue        # 首页(热门文章+最新文章)
│   │   ├── PostDetail.vue  # 文章详情
│   │   ├── PostEdit.vue    # 文章编辑
│   │   ├── Profile.vue     # 个人中心
│   │   └── UserProfile.vue # 用户主页
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── tailwind.config.js      # Tailwind配置
└── vue.config.js           # Vue CLI配置(API代理)
```

## 🚀 快速开始

### 环境要求

- Node.js >= 14.x
- npm >= 6.x

### 开发运行

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run serve

# 访问 http://localhost:3000
```

### 生产构建

```bash
npm run build
```

## ⚙️ 配置

### API代理 (vue.config.js)

```javascript
devServer: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true
    }
  }
}
```

### 主题色 (tailwind.config.js)

```javascript
colors: {
  primary: {
    600: '#667eea',  // 主色调
  }
}
```

## 🎯 核心功能实现

### JWT认证与Token刷新

- 请求拦截器自动添加JWT token
- Token过期前5分钟自动刷新
- 使用队列防止并发刷新

### 首页布局

**双模块设计**:
- **模块1 - 热门文章**: 显示热度最高的12篇文章(可滚动)
- **模块2 - 最新文章**: 分页显示最新文章

**侧边栏**:
- 搜索框
- 热门标签
- 热门作者(占位)
- 社区统计(实时更新)

### 文章卡片

支持两种视图:
- **列表视图**: 横向布局,封面图(1/3) + 内容(2/3)
- **网格视图**: 垂直布局,封面图在上

自动提取摘要: 如果文章没有摘要,自动从内容中提取前150字符

## 📖 API对接

### 认证接口

- `POST /api/users/register` - 注册
- `POST /api/users/login` - 登录(支持"记住我")
- `GET /api/users/me` - 获取当前用户
- `POST /api/users/refresh-token` - 刷新Token

### 文章接口

- `GET /api/posts` - 文章列表(分页+排序)
- `GET /api/posts/:id` - 文章详情
- `POST /api/posts` - 创建文章
- `PUT /api/posts/:id` - 更新文章
- `DELETE /api/posts/:id` - 删除文章

