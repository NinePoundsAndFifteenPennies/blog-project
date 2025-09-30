# 博客系统前端项目

基于 Vue 3 + Tailwind CSS 构建的现代化多用户博客系统前端。

## ✨ 特性

- 🎨 **现代化设计** - 采用Tailwind CSS,毛玻璃效果,流畅动画
- 📱 **响应式布局** - 完美适配移动端、平板和桌面设备
- 🔐 **用户认证** - JWT Token认证,路由守卫保护
- ✍️ **Markdown编辑器** - 支持Markdown语法,实时预览
- 🏷️ **标签分类** - 文章标签和分类管理
- 💾 **草稿保存** - 支持保存草稿,避免内容丢失
- 🎯 **SEO友好** - 页面标题动态设置

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **路由**: Vue Router 4
- **状态管理**: Vuex 4
- **HTTP客户端**: Axios
- **样式**: Tailwind CSS 3
- **Markdown**: marked + highlight.js
- **构建工具**: Vue CLI 5

## 📦 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/                # API接口封装
│   │   ├── auth.js         # 认证接口
│   │   └── posts.js        # 文章接口
│   ├── assets/             # 资源文件
│   │   └── main.css        # 全局样式
│   ├── components/         # 公共组件
│   │   ├── Header.vue      # 导航栏
│   │   ├── PostCard.vue    # 文章卡片
│   │   └── Pagination.vue  # 分页组件
│   ├── router/             # 路由配置
│   │   └── index.js
│   ├── store/              # Vuex状态管理
│   │   └── index.js
│   ├── utils/              # 工具函数
│   │   └── request.js      # Axios封装
│   ├── views/              # 页面组件
│   │   ├── Home.vue        # 首页
│   │   ├── Login.vue       # 登录页
│   │   ├── Register.vue    # 注册页
│   │   ├── PostDetail.vue  # 文章详情
│   │   ├── PostEdit.vue    # 文章编辑
│   │   ├── Profile.vue     # 个人中心
│   │   └── NotFound.vue    # 404页面
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── .gitignore
├── package.json
├── tailwind.config.js      # Tailwind配置
├── vue.config.js           # Vue CLI配置
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

### 需要实现的后端接口

CRUD所有API接口都已在 `src/api/` 目录下封装好,但是下面展示的只是接口示例，
后端的api不一定真长这样，需要同时参考根目录下的`README.md`和`Java博客系统架构方案 (1).docx`，
以实际后端编写为准。

#### 用户认证接口

- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/me` - 获取当前用户信息

#### 文章管理接口

- `GET /api/posts` - 获取文章列表(支持分页)
- `GET /api/posts/:id` - 获取文章详情
- `POST /api/posts` - 创建文章
- `PUT /api/posts/:id` - 更新文章
- `DELETE /api/posts/:id` - 删除文章
- `GET /api/users/:userId/posts` - 获取用户文章列表

### 数据格式示例

**登录请求:**
```json
{
  "username": "user123",
  "password": "password123"
}
```

**登录响应:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com"
  }
}
```

**文章列表响应:**
```json
{
  "data": [
    {
      "id": 1,
      "title": "文章标题",
      "content": "文章内容...",
      "summary": "文章摘要",
      "author": {
        "id": 1,
        "username": "author"
      },
      "tags": ["Vue", "JavaScript"],
      "category": "前端开发",
      "views": 123,
      "likes": 45,
      "comments": 12,
      "createdAt": "2025-01-01T00:00:00Z",
      "updatedAt": "2025-01-02T00:00:00Z"
    }
  ],
  "totalPages": 10,
  "currentPage": 1
}
```

## 📝 待实现功能(你需要添加后端逻辑的地方)

项目中所有标记为 `TODO` 的地方都需要连接后端API:

### 1. 用户认证 (src/store/index.js)

```javascript
// 登录
async login({ commit }, credentials) {
  const response = await login(credentials)  // 已封装好
  commit('SET_TOKEN', response.token)
  commit('SET_USER', response.user)
  return response
}
```

### 2. 文章列表 (src/views/Home.vue)

```javascript
const loadPosts = async () => {
  const response = await getPosts({
    page: currentPage.value,
    category: selectedCategory.value
  })
  posts.value = response.data
  totalPages.value = response.totalPages
}
```

### 3. 文章详情 (src/views/PostDetail.vue)

```javascript
const loadPost = async () => {
  const postId = route.params.id
  post.value = await getPostById(postId)
}
```

### 4. 创建/编辑文章 (src/views/PostEdit.vue)

```javascript
const handleSubmit = async () => {
  const postData = { title, content, tags, category }
  
  if (isEditMode.value) {
    await updatePost(route.params.id, postData)
  } else {
    await createPost(postData)
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

确保在 `vue.config.js` 中配置了代理,并且后端允许跨域请求。

### 2. Token过期处理

Axios拦截器会自动处理401错误并跳转到登录页。

### 3. Tailwind样式不生效

确保在 `src/main.js` 中引入了 `./assets/main.css`。

### 4. Markdown不渲染

检查是否安装了 `marked` 包,并在组件中正确使用 `v-html`。

## 📄 License

MIT

## 👥 贡献

欢迎提交 Issue 和 Pull Request!

---

**提示**: 所有UI组件已完成,你只需要在标记 `TODO` 的地方填充后端API调用逻辑即可!