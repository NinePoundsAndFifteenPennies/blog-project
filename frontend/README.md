# 博客系统前端项目

基于 Vue 3 + Tailwind CSS 构建的现代化多用户博客系统前端。

## ✨ 特性

- 🎨 **现代化设计** - 采用Tailwind CSS,毛玻璃效果,流畅动画
- 📱 **响应式布局** - 完美适配移动端、平板和桌面设备
- 🔐 **用户认证** - JWT Token认证,路由守卫保护,支持"记住我"功能
- 🔄 **自动刷新Token** - token即将过期时自动刷新,无感知体验
- ✍️ **Markdown编辑器** - 支持Markdown语法,实时预览
- 💬 **评论系统** - 文章评论、评论编辑、评论删除、动态分页加载
- 👍 **点赞功能** - 文章点赞、评论点赞、实时更新点赞状态
- 🖼️ **头像上传** - 支持用户头像上传和更新，图片尺寸和格式验证
- 🏷️ **标签分类** - 文章标签和分类管理
- 💾 **草稿保存** - 支持保存草稿,避免内容丢失
- 🎯 **SEO友好** - 页面标题动态设置

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **路由**: Vue Router 4
- **状态管理**: Vuex 4
- **HTTP客户端**: Axios
- **JWT解析**: jwt-decode
- **样式**: Tailwind CSS 3
- **Markdown**: marked + highlight.js
- **构建工具**: Vue CLI 5

## 📦 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/                # API接口封装
│   │   ├── auth.js         # 认证接口 (register, login, getCurrentUser, refreshToken)
│   │   ├── posts.js        # 文章接口 (CRUD + 分页)
│   │   ├── comments.js     # 评论接口 (CRUD + 分页 + 点赞)
│   │   ├── likes.js        # 文章点赞接口
│   │   └── files.js        # 文件上传接口 (头像上传)
│   ├── assets/             # 资源文件
│   │   └── main.css        # 全局样式 (含Tailwind导入)
│   ├── components/         # 公共组件
│   │   ├── Header.vue      # 顶部导航栏 (支持响应式、登录状态、头像显示)
│   │   ├── PostCard.vue    # 文章卡片 (展示文章信息 + 点赞功能)
│   │   ├── CommentList.vue # 评论列表 (分页加载、动态加载更多)
│   │   ├── CommentItem.vue # 评论项 (编辑、删除、点赞功能)
│   │   └── Pagination.vue  # 分页组件 (支持首页/末页/上下页)
│   ├── router/             # 路由配置
│   │   └── index.js        # 路由定义 + 导航守卫
│   ├── store/              # Vuex状态管理
│   │   └── index.js        # 认证状态管理 (token, user, rememberMe)
│   ├── utils/              # 工具函数
│   │   ├── request.js      # Axios封装 (含JWT自动刷新、错误拦截)
│   │   └── avatar.js       # 头像辅助函数
│   ├── views/              # 页面组件
│   │   ├── Home.vue        # 首页 (文章列表 + 分页 + 点赞)
│   │   ├── Login.vue       # 登录页 (支持"记住我")
│   │   ├── Register.vue    # 注册页
│   │   ├── PostDetail.vue  # 文章详情 (Markdown渲染 + 评论 + 点赞)
│   │   ├── PostEdit.vue    # 文章编辑 (创建/更新 + 草稿功能)
│   │   ├── Profile.vue     # 个人中心 (我的文章 + 我的评论 + 头像上传)
│   │   ├── MyComments.vue  # 我的评论列表
│   │   └── NotFound.vue    # 404页面
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── .gitignore
├── package.json            # 依赖: Vue3, Axios, jwt-decode, Tailwind等
├── tailwind.config.js      # Tailwind配置
├── vue.config.js           # Vue CLI配置 (含API代理)
└── README.md
```

## 🚀 快速开始

### 环境要求

- Node.js >= 14.x
- npm >= 6.x 或 yarn >= 1.22.x

### 安装依赖

```bash
npm install
# 或
yarn install
```

### 开发环境运行

```bash
npm run serve
# 或
yarn serve
```

访问 http://localhost:3000

### 生产环境构建

```bash
npm run build
# 或
yarn build
```

## ⚙️ 配置说明

### 后端API代理配置

在 `vue.config.js` 中配置后端API地址:

```javascript
devServer: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 修改为你的后端地址
      changeOrigin: true
    }
  }
}
```

### Tailwind颜色主题自定义

在 `tailwind.config.js` 中修改主题色:

```javascript
colors: {
  primary: {
    600: '#667eea',  // 修改主色调
    // ...
  }
}
```

## 🔌 API对接说明

### 重要: 点赞字段命名差异

**注意**: 后端对文章(Post)和评论(Comment)使用了不同的字段命名方式：

**文章点赞字段**:
- JSON响应字段名: `isLiked` (Boolean)
- 使用示例: `post.isLiked`

**评论点赞字段**:
- JSON响应字段名: `liked` (Boolean)
- 使用示例: `comment.liked`

这是因为后端Java代码中，`CommentResponse`使用getter `isLiked()`会被Jackson序列化为`liked`，而`PostResponse`使用getter `getIsLiked()`会保留为`isLiked`。前端代码已按此规范实现。

### 需要实现的后端接口

所有API接口都已在 `src/api/` 目录下封装好。后端实际已实现的API与下面的示例可能略有不同，
请参考根目录下的 `README.md` 和 `REMEMBER_ME_IMPLEMENTATION.md` 以了解实际接口规格。

#### 用户认证接口

- `POST /api/users/register` - 用户注册
  - 请求体: `{ username, password, email }`
  - 响应: `{ message: "用户注册成功" }`
  
- `POST /api/users/login` - 用户登录（支持"记住我"）
  - 请求体: `{ username, password, rememberMe }`
  - 响应: `{ accessToken: "jwt-token", tokenType: "Bearer" }`
  - 说明: `rememberMe=true` 返回30天有效期token，`false` 返回1小时有效期token
  
- `GET /api/users/me` - 获取当前用户信息
  - 响应: `"当前登录用户是: {username}"`
  - 需要JWT认证
  
- `POST /api/users/refresh-token` - 刷新Token
  - 请求体: `{ rememberMe }`
  - 响应: `{ accessToken: "new-jwt-token", tokenType: "Bearer" }`
  - 需要JWT认证，用于自动刷新即将过期的token

#### 文章管理接口

- `GET /api/posts` - 获取文章列表(支持分页)
  - 查询参数: `page`, `size`, `sort` (例如: `sort=createdAt,desc`)
  - 响应: Spring Data分页对象 `{ content: [...], totalPages, totalElements, ... }`
  
- `GET /api/posts/my` - 获取当前用户的所有文章（包括草稿）
  - 查询参数: `page`, `size`, `sort`
  - 需要JWT认证
  
- `GET /api/posts/:id` - 获取文章详情
  - 响应: `{ id, title, content, contentType, author: { id, username }, createdAt, updatedAt }`
  
- `POST /api/posts` - 创建文章
  - 请求体: `{ title, content, contentType: "MARKDOWN" }`
  - 需要JWT认证
  
- `PUT /api/posts/:id` - 更新文章
  - 请求体: `{ title, content, contentType: "MARKDOWN" }`
  - 需要JWT认证，仅作者可操作
  
- `DELETE /api/posts/:id` - 删除文章
  - 响应: `"文章删除成功"`
  - 需要JWT认证，仅作者可操作

### 数据格式示例

**用户注册请求:**
```json
{
  "username": "user123",
  "password": "password123",
  "email": "user@example.com"
}
```

**登录请求:**
```json
{
  "username": "user123",
  "password": "password123",
  "rememberMe": true
}
```

**登录响应:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**文章创建/更新请求:**
```json
{
  "title": "文章标题",
  "content": "# Markdown内容\n\n这是文章正文...",
  "contentType": "MARKDOWN"
}
```

**文章响应 (PostResponse):**
```json
{
  "id": 1,
  "title": "文章标题",
  "content": "# Markdown内容\n\n这是文章正文...",
  "contentType": "MARKDOWN",
  "author": {
    "id": 1,
    "username": "user123"
  },
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-02T10:30:00"
}
```

**文章列表分页响应 (Spring Data Page):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "文章标题",
      "content": "...",
      "contentType": "MARKDOWN",
      "author": { "id": 1, "username": "user123" },
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-01-02T00:00:00"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 10 },
  "totalPages": 5,
  "totalElements": 42,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0
}
```

## 🔧 核心实现说明

### 1. JWT认证与自动刷新 (utils/request.js)

**请求拦截器** - 自动添加JWT token并实现智能刷新:
- 检查token是否在5分钟内过期
- 自动调用 `/api/users/refresh-token` 刷新token
- 使用队列机制防止并发刷新
- 刷新后的新token自动用于后续请求

**响应拦截器** - 统一错误处理:
- `401` 错误: 清除认证信息，跳转登录页并显示提示
- `403` 错误: 权限不足提示
- `404` 错误: 资源不存在提示
- 其他错误: 统一错误消息格式

### 2. 状态管理 (store/index.js)

**认证状态管理**:
- 统一使用 `localStorage` 存储token、用户信息和rememberMe标志
- 支持在多个标签页之间共享认证状态
- 退出登录时清除所有认证信息

**Mutations**:
- `SET_TOKEN`: 保存token和rememberMe标志
- `SET_USER`: 保存用户信息
- `CLEAR_AUTH`: 清除所有认证信息

**Actions**:
- `login`: 调用登录API，保存token和用户信息
- `logout`: 清除本地认证信息（纯前端操作）
- `getCurrentUser`: 获取当前登录用户信息

### 3. 路由守卫 (router/index.js)

**导航守卫逻辑**:
- 检查路由meta中的 `requiresAuth` 标志
- 从localStorage读取token验证登录状态
- 未登录用户访问受保护路由时重定向到登录页
- 已登录用户访问guest页面（登录/注册）时重定向到首页
- 动态设置页面标题

### 4. API封装 (api/)

**auth.js** - 用户认证接口:
- `register(userData)`: 用户注册
- `login(credentials)`: 用户登录，支持rememberMe参数
- `getCurrentUser()`: 获取当前用户，解析后端返回的字符串
- `refreshToken(rememberMe)`: 刷新token

**posts.js** - 文章管理接口:
- `getPosts(params)`: 获取文章列表，支持分页参数
- `getMyPosts(params)`: 获取当前用户的所有文章
- `getPostById(id)`: 获取单篇文章详情
- `createPost(postData)`: 创建文章，自动添加contentType
- `updatePost(id, postData)`: 更新文章
- `deletePost(id)`: 删除文章

### 5. 组件说明

**TheNavbar.vue** - 导航栏组件:
- 响应式设计，支持移动端菜单
- 根据登录状态显示不同按钮
- 集成退出登录功能

**PostCard.vue** - 文章卡片:
- 展示文章标题、作者、创建时间
- 支持点击跳转到文章详情

**Pagination.vue** - 分页组件:
- 支持首页、末页、上一页、下一页跳转
- 显示当前页码和总页数

## 📝 开发指南

所有前端UI组件和核心功能已完成，后端CRUD接口也已完全实现并对接完毕。
当前项目已实现用户注册、登录、文章增删改查、分页显示等完整功能。

### 已完成功能

✅ **用户认证**:
- 用户注册、登录、获取当前用户信息
- "记住我"功能 (1小时 vs 30天token)
- JWT自动刷新机制
- Token过期自动跳转登录页
- 头像上传和更新功能

✅ **文章管理**:
- 文章列表展示（支持分页）
- 文章详情查看
- 创建、编辑、删除文章
- 获取"我的文章"列表
- Markdown内容支持
- 文章点赞和取消点赞
- 草稿功能

✅ **评论系统**:
- 发表评论、编辑评论、删除评论
- 评论列表展示（分页加载）
- 动态加载更多评论（首次10条，之后每次20条）
- 评论点赞和取消点赞
- 我的评论列表

✅ **状态管理**:
- Vuex管理认证状态
- localStorage持久化
- 路由守卫保护

✅ **UI组件**:
- 响应式导航栏
- 文章卡片组件
- 评论列表和评论项组件
- 分页组件
- 多个页面视图 (Home, Login, Register, Profile, PostDetail, PostEdit, MyComments)

### 代码示例

如需扩展功能，可参考以下模式：

**在store中添加新的action:**
```javascript
// src/store/index.js
async login({ commit }, credentials) {
  const token = await login(credentials)  // 调用API
  commit('SET_TOKEN', { token, rememberMe: credentials.rememberMe })
  // 获取用户信息
  const user = await getCurrentUser()
  commit('SET_USER', user)
  return token
}
```

**在页面中调用API获取数据:**
```javascript
// src/views/Home.vue
import { getPosts } from '@/api/posts'

const loadPosts = async () => {
  try {
    const response = await getPosts({
      page: currentPage.value - 1,  // Spring分页从0开始
      size: 10,
      sort: 'createdAt,desc'
    })
    posts.value = response.content
    totalPages.value = response.totalPages
  } catch (error) {
    console.error('加载文章失败:', error)
  }
}
```

## 🎨 组件使用示例

### PostCard组件

```vue
<PostCard :post="postData" />
```

### Pagination组件

```vue
<Pagination
  :current-page="currentPage"
  :total-pages="totalPages"
  @page-change="handlePageChange"
/>
```

## 🔐 权限控制

路由守卫已配置在 `src/router/index.js`:

- **requiresAuth**: 需要登录才能访问的页面
- **guest**: 只有未登录用户可访问的页面(如登录、注册)

添加新路由时设置对应的meta:

```javascript
{
  path: '/my-page',
  component: MyPage,
  meta: { requiresAuth: true, title: '我的页面' }
}
```

## 🐛 常见问题

### 1. 跨域问题

确保在 `vue.config.js` 中配置了代理:
```javascript
devServer: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```
后端确保允许跨域请求（已在SecurityConfig中配置）。

### 2. Token过期处理

**自动刷新**: token在5分钟内过期时，Axios请求拦截器会自动调用刷新接口。

**完全过期**: token已失效时（401错误），响应拦截器会:
- 清除所有认证信息
- 跳转到登录页
- 显示"登录已过期，请重新登录"提示
- 保存当前路径到redirect参数，登录后自动返回

### 3. JWT解析错误

确保已安装 `jwt-decode` 依赖:
```bash
npm install jwt-decode
```

在 `utils/request.js` 中使用:
```javascript
import { jwtDecode } from "jwt-decode"
```

### 4. localStorage存储策略

项目统一使用 `localStorage` 存储:
- `token`: JWT token
- `user`: 用户信息对象（字符串化的JSON）
- `rememberMe`: "true" 或 "false" 字符串

关闭浏览器后重新打开，只要JWT未过期，用户仍保持登录状态。

### 5. Tailwind样式不生效

确保在 `src/main.js` 中引入了 `./assets/main.css`:
```javascript
import './assets/main.css'
```

### 6. Markdown不渲染

检查是否安装了 `marked` 包:
```bash
npm install marked
```

在组件中正确使用 `v-html`:
```vue
<div v-html="marked(post.content)"></div>
```

### 7. 分页参数问题

Spring Data分页从0开始，前端显示从1开始:
```javascript
// 前端显示: 第1页 -> 后端请求: page=0
const response = await getPosts({
  page: currentPage.value - 1,
  size: 10
})
```

## 📄 License

MIT

## 👥 贡献

欢迎提交 Issue 和 Pull Request!

---

**项目状态**: 前后端基础功能已完成并对接，可正常运行。包括用户认证、文章CRUD、分页显示、JWT自动刷新等核心功能。