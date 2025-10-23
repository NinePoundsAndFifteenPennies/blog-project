# 博客系统前端项目

基于 Vue 3 + Tailwind CSS 构建的现代化多用户博客系统前端。

## ✨ 核心功能

- 🎨 现代化设计 - Tailwind CSS、毛玻璃效果、流畅动画
- 📱 响应式布局 - 适配移动端、平板和桌面设备
- 🔐 JWT 认证 - 支持"记住我"和自动刷新 Token
- ✍️ Markdown 编辑器 - 实时预览、代码高亮
- 💬 评论系统 - 支持嵌套评论和点赞
- 👍 点赞功能 - 文章和评论点赞
- 🖼️ 头像上传 - 图片格式和尺寸验证

## 🛠️ 技术栈

- Vue 3 + Vue Router + Vuex
- Axios + JWT
- Tailwind CSS
- marked + highlight.js

## 🚀 快速开始

### 环境要求

- Node.js >= 14.x
- npm >= 6.x

### 开发环境

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

## ⚙️ 配置说明

### 后端 API 代理

在 `vue.config.js` 中配置：

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

### Tailwind 主题定制

在 `tailwind.config.js` 中修改：

```javascript
colors: {
  primary: {
    600: '#667eea',  // 主色调
  }
}
```

## �� 项目结构

```
frontend/
├── src/
│   ├── api/           # API 接口封装
│   ├── components/    # 公共组件
│   ├── router/        # 路由配置
│   ├── store/         # Vuex 状态管理
│   ├── utils/         # 工具函数（Axios、头像等）
│   ├── views/         # 页面组件
│   └── main.js        # 入口文件
├── public/            # 静态资源
├── Dockerfile         # Docker 构建文件
├── nginx.conf         # Nginx 配置
└── package.json       # 依赖配置
```

## 🔌 重要说明

### 点赞字段差异

后端对文章和评论使用了不同的字段命名：

- **文章点赞**：`post.isLiked`
- **评论点赞**：`comment.liked`

### JWT 自动刷新

Token 在 5 分钟内过期时自动刷新（`utils/request.js`）。完全过期时（401），会清除认证信息并跳转登录页。

### 路由守卫

在 `router/index.js` 中配置：

```javascript
{
  path: '/my-page',
  component: MyPage,
  meta: { 
    requiresAuth: true,  // 需要登录
    title: '我的页面'
  }
}
```

## 🔧 开发指南

### API 调用示例

```javascript
import { getPosts } from '@/api/posts'

const loadPosts = async () => {
  const response = await getPosts({
    page: 0,        // 从 0 开始
    size: 10,
    sort: 'createdAt,desc'
  })
  posts.value = response.content
}
```

### 状态管理示例

```javascript
// 在组件中使用
import { mapState, mapActions } from 'vuex'

export default {
  computed: {
    ...mapState(['user', 'token'])
  },
  methods: {
    ...mapActions(['login', 'logout'])
  }
}
```

## 📄 更多文档

- [主 README](../README.md) - 项目整体说明
- [API 文档](../docs/API.md) - 后端 API 接口
- [架构说明](../docs/ARCHITECTURE.md) - 项目架构

## 📝 License

MIT
