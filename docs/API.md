# API 接口文档

本文档详细说明所有 REST API 接口的使用方法。

## 基础信息

- **基础 URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **内容类型**: `application/json` (除文件上传外)

## 认证说明

需要认证的接口必须在请求头中携带 JWT Token：

## 注意

有些返回结果是我直接从postman里面复制粘贴过来的， 返回结果某些量的value不一定和测试给的一致。

```http
Authorization: Bearer {your-jwt-token}
```

## 用户相关接口

### 用户注册

创建一个新用户账号。

```http
POST /api/users/register
Content-Type: application/json
```

**请求体:**
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

**成功响应:** `201 Created`
```json
"用户注册成功！"
```

**错误响应:**
- `400 Bad Request` - 用户名已存在或邮箱已被注册

---

### 用户登录

使用用户名或邮箱登录，获取 JWT Token。

```http
POST /api/users/login
Content-Type: application/json
```

**请求体:**
```json
{
  "username": "testuser",
  "password": "password123",
  "rememberMe": true
}
```

**参数说明:**
- `username`: 用户名或邮箱
- `password`: 密码
- `rememberMe`: 是否记住我（true: 30天，false: 1小时，可以加可以不加，默认false）

**成功响应:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzZWNvbmR1c2VyIiwiaWF0IjoxNzYwNjg2MTc5LCJleHAiOjE3NjA2ODk3Nzl9.2Rev0bSNSRrD_gBhMii2DX5J2z-zdJPk4ZuRwjeIifQ6kXvGBnLS7hPOj0m9_-lwd06umRDSjKGQNIUeigohog",
  "tokenType": "Bearer"
}
```

---

### 获取当前用户信息

获取当前登录用户的详细信息。

```http
GET /api/users/me
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
  "nickname": "Second",
  "bio": "个人简介，最多200字",
  "socialLink": "https://example.com/me",
  "gender": "female",
  "birthday": "1995-02-03",
  "location": "上海"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效

---

### 获取公开用户资料

获取指定用户的公开资料信息（无需登录）。

```http
GET /api/users/{username}
```

**路径参数:**
- `username`: 用户名

**成功响应:** `200 OK`
```json
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
  "nickname": "Second",
  "bio": "个人简介，最多200字",
  "socialLink": "https://example.com/me",
  "gender": "female",
  "birthday": "1995-02-03",
  "location": "上海"
}
```

**错误响应:**
- `404 Not Found` - 用户不存在

---

### 更新当前用户资料

仅允许登录用户修改自己的资料。

```http
PUT /api/users/me/profile
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "nickname": "新的昵称，唯一",
  "bio": "个人简介，最多200字",
  "socialLink": "https://github.com/myself",
  "gender": "female",
  "birthday": "1995-02-03",
  "location": "上海",
  "email": "newmail@example.com",
  "currentPassword": "oldpass123",
  "newPassword": "newpass456"
}
```

**字段说明:**
- `nickname` (String, 可选): 昵称/展示名，1-50字符，需唯一，可与登录用户名不同
- `bio` (String, 可选): 个人简介，最长200字符（传空字符串表示清空）
- `socialLink` (String, 可选): 社交链接，须以 http/https 开头，传空字符串可清空
- `gender` (String, 可选): 性别文本，最长20字符（传空字符串清空）
- `birthday` (String, 可选): 生日，格式 `yyyy-MM-dd`，不能是未来日期；传空字符串清空
- `location` (String, 可选): 所在地，最长100字符（传空字符串清空）
- `email` (String, 可选): 新邮箱，需唯一且格式正确
- `currentPassword` / `newPassword` (String, 可选): 同时提供以修改密码，长度≥6，需校验旧密码

**成功响应:** `200 OK`
```json
{
  "id": 2,
  "username": "seconduser",
  "email": "newmail@example.com",
  "avatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
  "nickname": "Second",
  "bio": "个人简介，最多200字",
  "socialLink": "https://example.com/me",
  "gender": "female",
  "birthday": "1995-02-03",
  "location": "上海"
}
```

**错误响应:**
- `400 Bad Request` - 参数不符合要求（昵称重复、密码不正确、日期格式错误等）
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 已登录但无权限

---

### 刷新 Token

刷新即将过期的 JWT Token。

```http
POST /api/users/refresh-token
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**（不加默认false）
```json
{
  "rememberMe": false
}
```

**成功响应:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzZWNvbmR1c2VyIiwiaWF0IjoxNzYwNjg2MjI0LCJleHAiOjE3NjA2ODk4MjR9.KAtGtaVn61rBzXBFkoKPnBHbc9KHqiWRQ2UnYcbI28OlOHr7RflWNxMjBAIKOcBKkcQtyIEOsUeHdnOyqgbzaQ",
  "tokenType": "Bearer"
}
```

---

### 保存用户头像 URL

将头像 URL 保存到用户信息中。

```http
POST /api/users/me/avatar
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "avatarUrl": "/uploads/1/avatars/abc123.jpg"
}
```

**成功响应:** `200 OK`
```json
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg"
}
```

## 文章相关接口

### 创建文章

发布一篇新文章。

```http
POST /api/posts
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "title": "文章标题",
  "content": "文章内容，支持 Markdown",
  "contentType": "MARKDOWN",
  "draft": false,
  "category": "技术文章",
  "tags": ["Java", "Spring Boot"],
  "coverImageUrl": "/uploads/1/covers/abc123.jpg"
}
```

**参数说明:**
- `title`: 文章标题（必填）
- `content`: 文章内容（必填）
- `contentType`: 内容类型，`MARKDOWN` 或 `HTML`
- `draft`: 是否为草稿（true: 草稿，false: 发布）
- `category`: 分类名称（可选），如果分类不存在会自动创建
- `tags`: 标签名称列表（可选），如果标签不存在会自动创建
- `coverImageUrl`: 封面图片URL（可选），通过 `/api/files/upload/cover` 上传后获得

**成功响应:** `201 Created`
```json
{
  "id": 24,
  "title": "测试api2",
  "content": "# 这是标题\n\n这是文章内容...",
  "authorUsername": "seconduser",
  "authorNickname": "Second",
  "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
  "createdAt": "2025-10-17T17:58:06.0262871",
  "updatedAt": null,
  "publishedAt": "2025-10-17T17:58:06.0262871",
  "contentType": "MARKDOWN",
  "draft": false,
  "likeCount": 0,
  "isLiked": false,
  "commentCount": 0,
  "viewCount": 0
}
```

---

### 获取文章列表

分页获取所有已发布的文章，支持按时间或热度排序。

```http
GET /api/posts?page=0&size=10&sortBy=time&order=desc
```

**查询参数:**
- `page`: 页码（从 0 开始，默认 0）
- `size`: 每页数量（默认 10）
- `sortBy`: 排序方式（默认 `time`）
  - `time`: 按创建时间排序
  - `hotness`: 按热度排序（热度公式：`(viewCount * 0.1 + likeCount * 5 + commentCount * 10) / pow(hours + 2, 1.2)`）
- `order`: 排序顺序（默认 `desc`）
  - `desc`: 降序（时间从新到旧，热度从高到低）
  - `asc`: 升序（时间从旧到新，热度从低到高）

**示例请求:**
```http
# 按时间倒序（最新文章优先，默认）
GET /api/posts?page=0&size=10

# 按时间正序（最早文章优先）
GET /api/posts?page=0&size=10&sortBy=time&order=asc

# 按热度倒序（最热门文章优先）
GET /api/posts?page=0&size=10&sortBy=hotness&order=desc

# 按热度正序（最冷门文章优先）
GET /api/posts?page=0&size=10&sortBy=hotness&order=asc
```

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 24,
      "title": "测试api2",
      "content": "# 这是标题\n\n这是文章内容...",
      "authorUsername": "seconduser",
      "authorNickname": "Second",
      "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
      "createdAt": "2025-10-17T17:58:06.026287",
      "updatedAt": null,
      "publishedAt": "2025-10-17T17:58:06.026287",
      "contentType": "MARKDOWN",
      "draft": false,
      "likeCount": 0,
      "isLiked": false,
      "commentCount": 0,
      "viewCount": 0
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": false,
      "unsorted": false,
      "sorted": true
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "sort": {
    "empty": false,
    "unsorted": false,
    "sorted": true
  },
  "empty": false
}
```

---

### 获取我的文章

获取当前用户的所有文章（包括草稿）。

```http
GET /api/posts/my?page=0&size=10
Authorization: Bearer {token}
```
**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 8,
      "title": "更新",
      "content": "更新后的内容",
      "authorUsername": "seconduser",
      "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
      "createdAt": "2025-10-07T17:41:04.153692",
      "updatedAt": "2025-10-17T17:44:12.00442",
      "publishedAt": "2025-10-08T23:19:00.080556",
      "contentType": "MARKDOWN",
      "draft": false,
      "likeCount": 2,
      "isLiked": true,
      "commentCount": 1,
      "viewCount": 15
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": false
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "sort": {
    "empty": true,
    "unsorted": true,
    "sorted": false
  },
  "empty": false
}
```


---

### 获取单篇文章

根据 ID 获取文章详情。

```http
GET /api/posts/{id}
#这个token可加可不加，加了的话返回当前用户是否isLiked，不加默认false
Authorization: Bearer {token} 
```

**成功响应:** `200 OK`
```json
{
  "id": 8,
  "title": "更新",
  "content": "更新后的内容",
  "authorUsername": "seconduser",
  "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
  "createdAt": "2025-10-07T17:41:04.153692",
  "updatedAt": "2025-10-17T17:44:12.00442",
  "publishedAt": "2025-10-08T23:19:00.080556",
  "contentType": "MARKDOWN",
  "draft": false,
  "likeCount": 2,
  "isLiked": false,
  "commentCount": 1,
  "viewCount": 42
}
```

**说明:**
- 访问文章详情时，系统会自动记录浏览量
- 同一IP在1小时内重复访问不会重复计数
- `viewCount` 字段表示文章的总浏览次数

**错误响应:**
- `404 Not Found` - 文章不存在

---

### 获取指定用户的已发布文章

获取指定用户的所有已发布文章（不包括草稿），用于公开用户主页展示。

```http
GET /api/posts/user/{username}?page=0&size=10
```

**路径参数:**
- `username`: 用户名

**查询参数:**
- `page`: 页码（从 0 开始，默认 0）
- `size`: 每页数量（默认 10）

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 8,
      "title": "文章标题",
      "content": "文章内容...",
      "authorUsername": "username",
      "authorNickname": "用户昵称",
      "authorAvatarUrl": "/uploads/1/avatars/abc.jpg",
      "createdAt": "2025-10-07T17:41:04.153692",
      "updatedAt": null,
      "publishedAt": "2025-10-08T23:19:00.080556",
      "contentType": "MARKDOWN",
      "draft": false,
      "likeCount": 5,
      "isLiked": false,
      "commentCount": 3,
      "viewCount": 28
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

**说明:**
- 无需认证即可访问
- 仅返回该用户已发布的文章（draft = false）
- 如果携带JWT token，响应中会包含当前用户的点赞状态（isLiked）

**错误响应:**
- `404 Not Found` - 用户不存在

---

### 搜索文章

多维度搜索已发布的文章，支持按关键词、作者、标题、标签搜索，结果支持按时间或热度排序。

```http
GET /api/posts/search?keyword=spring&author=john&title=教程&tag=Java&sortBy=time&order=desc&page=0&size=10
```

**查询参数:**
- `keyword` (可选): 通用关键词，匹配文章标题、内容、作者昵称/用户名和标签名称，长度不超过200字符
- `author` (可选): 作者用户名或昵称（模糊匹配），长度不超过50字符
- `title` (可选): 标题关键词（模糊匹配），长度不超过100字符
- `tag` (可选): 标签名称（模糊匹配），长度不超过50字符
- `sortBy` (可选): 排序方式（默认 `time`）
  - `time`: 按创建时间排序
  - `hotness`: 按热度排序
- `order` (可选): 排序顺序（默认 `desc`）
  - `desc`: 降序
  - `asc`: 升序
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认10

**示例请求:**
```http
# 按关键词搜索（匹配标题、内容、作者和标签）
GET /api/posts/search?keyword=Spring Boot

# 通过关键词搜索特定作者的文章
GET /api/posts/search?keyword=john

# 通过关键词搜索带特定标签的文章
GET /api/posts/search?keyword=Java

# 按作者搜索（仅匹配作者字段）
GET /api/posts/search?author=john

# 按标题搜索
GET /api/posts/search?title=入门教程

# 按标签搜索（仅匹配标签字段）
GET /api/posts/search?tag=Java

# 组合搜索：关键词 + 作者 + 按热度排序
GET /api/posts/search?keyword=spring&author=john&sortBy=hotness&order=desc

# 带分页的搜索
GET /api/posts/search?keyword=spring&page=0&size=20
```

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot入门教程",
      "content": "这是一篇关于Spring Boot的教程...",
      "authorUsername": "john",
      "authorNickname": "John Doe",
      "authorAvatarUrl": "/uploads/1/avatars/abc.jpg",
      "createdAt": "2025-10-26T10:00:00",
      "updatedAt": null,
      "publishedAt": "2025-10-26T10:00:00",
      "contentType": "MARKDOWN",
      "draft": false,
      "likeCount": 5,
      "isLiked": false,
      "commentCount": 3,
      "viewCount": 28,
      "tags": [
        {
          "id": 1,
          "name": "Java",
          "color": "#FF5733"
        },
        {
          "id": 2,
          "name": "Spring",
          "color": "#6DB33F"
        }
      ],
      "category": {
        "id": 1,
        "name": "技术文章"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

**搜索算法说明:**
- **文本匹配**: 使用SQL LIKE进行模糊匹配，不区分大小写
- **特殊字符处理**: 
  - SQL特殊字符（`%`、`_`、`\`）会被自动转义
  - URL协议前缀（`http://`、`https://`、`www.`）会被移除
  - 首尾标点符号会被清理

**说明:**
- 无需认证即可访问
- 仅返回已发布的文章（draft = false）
- 如果携带JWT token，响应中会包含当前用户的点赞状态（isLiked）
- 至少需要提供一个搜索条件（keyword、author、title、tag中的一个）

**错误响应:**
- `400 Bad Request` - 参数不符合要求（长度超限、排序参数非法等）

---

### 更新文章

更新指定文章，仅作者可操作。

```http
PUT /api/posts/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "title": "更新后的标题",
  "content": "更新后的内容",
  "contentType": "MARKDOWN",
  "draft": false
}
```

**成功响应:** `200 OK`
```json
{
    "id": 8,
    "title": "更新后的标题",
    "content": "更新后的内容",
    "authorUsername": "seconduser",
    "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
    "createdAt": "2025-10-07T17:41:04.153692",
    "updatedAt": "2025-10-10T13:41:13.683885",
    "publishedAt": "2025-10-08T23:19:00.080556",
    "contentType": "MARKDOWN",
    "draft": false,
    "likeCount": 2,
    "isLiked": true,
    "commentCount": 1,
    "viewCount": 56
}
```

**错误响应:**
- `403 Forbidden` - 无权限修改此文章
- `404 Not Found` - 文章不存在

---

### 删除文章

删除指定文章，仅作者可操作。

```http
DELETE /api/posts/{id}
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
"文章删除成功"
```

**错误响应:**
- `403 Forbidden` - 无权限删除此文章
- `404 Not Found` - 文章不存在

## 文件相关接口

### 上传头像（首次）

首次上传用户头像。如果用户已有头像，将返回错误。

```http
POST /api/files/upload/avatar
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数:**
- `file`: 图片文件（JPG/PNG，最大 10MB，尺寸 50x50 至 4096x4096 像素）

**成功响应:** `200 OK`
```json
{
  "avatarUrl": "/uploads/1/avatars/abc123.jpg"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `400 Bad Request` - 文件格式、大小或尺寸不符合要求
- `400 Bad Request` - "用户已有头像，请使用更新接口替换头像"

---

### 更新头像

更新用户头像。自动删除旧头像文件。

```http
PUT /api/files/update/avatar
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数:**
- `file`: 图片文件（JPG/PNG，最大 10MB，尺寸 50x50 至 4096x4096 像素）

**成功响应:** `200 OK`
```json
{
  "avatarUrl": "/uploads/1/avatars/def456.jpg"
}
```

**说明:** 旧头像文件会被自动删除。

---

### 上传文章封面图片

上传文章封面图片。封面图片会在文章保存/发布时上传。

```http
POST /api/files/upload/cover
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数:**
- `file`: 图片文件（JPG/PNG，最大 10MB，尺寸 50x50 至 4096x4096 像素）

**成功响应:** `200 OK`
```json
{
  "imageUrl": "/uploads/1/covers/xyz789.jpg"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `400 Bad Request` - 文件格式、大小或尺寸不符合要求

**说明:** 
- 封面图片存储在 `uploads/{userId}/covers/` 目录下
- 当文章封面更新或文章删除时，旧封面文件会被自动删除

---

### 删除图片

删除用户上传的图片文件（包括封面图片）。

```http
DELETE /api/files/delete
Authorization: Bearer {token}
```

**请求参数:**
- `imageUrl`: 图片URL（如 `/uploads/1/covers/xyz789.jpg`）

**成功响应:** `200 OK`
```json
{
  "message": "图片删除成功"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 无权删除该文件（只能删除自己的文件）
- `400 Bad Request` - 无效的文件路径

**安全特性:**
- 验证用户身份和文件所有权
- 防止路径遍历攻击
- 只允许删除 `uploads/{userId}/` 目录下的文件

## 点赞相关接口

### 点赞文章

给文章点赞。如果已经点赞，则不会重复添加。

```http
POST /api/posts/{postId}/likes
Authorization: Bearer {token}
```

**路径参数:**
- `postId`: 文章ID

**成功响应:** `200 OK`
```json
{
  "likeCount": 5,
  "liked": true
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 文章不存在

---

### 取消点赞

取消对文章的点赞。如果未点赞，则不会有任何影响。

```http
DELETE /api/posts/{postId}/likes
Authorization: Bearer {token}
```

**路径参数:**
- `postId`: 文章ID

**成功响应:** `200 OK`
```json
{
  "likeCount": 4,
  "Liked": false
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 文章不存在

---

### 获取点赞信息

获取文章的点赞数量和当前用户的点赞状态。

```http
GET /api/posts/{postId}/likes
```

**路径参数:**
- `postId`: 文章ID

**成功响应:** `200 OK`
```json
{
  "likeCount": 5,
  "Liked": true
}
```

**说明:**
- 匿名用户可以访问此接口查看点赞数量
- 对于匿名用户，`Liked` 始终为 `false`
- 登录用户可以看到自己是否已点赞

**错误响应:**
- `404 Not Found` - 文章不存在

## 评论相关接口

### 创建评论

在指定文章下创建一条评论。

```http
POST /api/posts/{postId}/comments
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数:**
- `postId`: 文章ID

**请求体:**
```json
{
  "content": "这是评论内容"
}
```

**字段说明:**
- `content` (String, 必填): 评论内容，长度1-3000字符

**成功响应:** `201 Created`
```json
{
  "id": 1,
  "content": "这是评论内容",
  "postId": 10,
  "postTitle": "文章标题",
  "parentId": null,
  "replyToUserId": null,
  "replyToUsername": null,
  "level": 0,
  "replyCount": 0,
  "authorUsername": "用户名",
  "authorAvatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-10-16T15:30:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 尝试评论草稿文章
- `404 Not Found` - 文章不存在
- `400 Bad Request` - 评论内容不符合要求

---

### 创建子评论（回复评论）

在指定评论下创建回复（子评论）。

```http
POST /api/comments/{commentId}/replies
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数:**
- `commentId`: 被回复的评论ID（可以是顶层评论或子评论）

**请求体:**
```json
{
  "content": "回复内容",
  "replyToUserId": 123
}
```

**字段说明:**
- `content` (String, 必填): 回复内容，长度1-3000字符
- `replyToUserId` (Long, 可选): 被@的用户ID，不填则回复评论本身

**成功响应:** `201 Created`
```json
{
  "id": 100,
  "content": "回复内容",
  "postId": 10,
  "postTitle": "文章标题",
  "parentId": 5,
  "replyToUserId": 123,
  "replyToUsername": "张三",
  "level": 1,
  "replyCount": 0,
  "authorUsername": "李四",
  "authorAvatarUrl": "/uploads/4/avatars/abc.jpg",
  "createdAt": "2025-10-19T10:00:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 尝试回复草稿文章的评论
- `404 Not Found` - 父评论不存在或replyToUserId不存在
- `400 Bad Request` - 内容长度不符合要求、层级超限

---

### 获取文章评论

获取指定文章的所有顶层评论，支持分页和排序（按时间或热度）。

```http
GET /api/posts/{postId}/comments?page=0&size=20&sortBy=time&order=asc
```

**路径参数:**
- `postId`: 文章ID

**查询参数:**
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认20
- `sortBy` (可选): 排序方式（默认 `time`）
  - `time`: 按创建时间排序
  - `hotness`: 按热度排序（热度公式：`(likeCount * 1 + replyCount * 3 + bonus) / pow(hours + 2, 1.5)`，bonus = if(replyCount > 5, 10, 0)）
- `order` (可选): 排序顺序（默认 `asc`）
  - `asc`: 升序（时间从旧到新，热度从低到高）
  - `desc`: 降序（时间从新到旧，热度从高到低）

**示例请求:**
```http
# 按时间正序（默认，最早的评论优先）
GET /api/posts/1/comments?page=0&size=20

# 按时间倒序（最新的评论优先）
GET /api/posts/1/comments?page=0&size=20&sortBy=time&order=desc

# 按热度倒序（最热门的评论优先）
GET /api/posts/1/comments?page=0&size=20&sortBy=hotness&order=desc

# 按热度正序（最冷门的评论优先）
GET /api/posts/1/comments?page=0&size=20&sortBy=hotness&order=asc
```

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "content": "第一条评论",
      "postId": 10,
      "postTitle": "文章标题",
      "parentId": null,
      "replyToUserId": null,
      "replyToUsername": null,
      "level": 0,
      "replyCount": 5,
      "authorUsername": "user1",
      "authorNickname": "用户昵称",
      "authorAvatarUrl": "https://example.com/avatar1.jpg",
      "createdAt": "2025-10-16T15:30:00",
      "updatedAt": null,
      "likeCount": 3,
      "liked": false
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

**说明:**
- 匿名用户可以访问此接口查看评论
- **仅返回顶层评论**（parentId为null），子评论排序不受影响
- 每条评论包含点赞数量、当前用户的点赞状态和子评论数量（replyCount）
- 热度算法特点：
  - 重讨论：回复权重高（×3），回复多的评论排在前面
  - 快代谢：重力因子1.5，旧评论会比文章更快让位给新评论
  - 防马太效应：通过时间衰减，防止早期评论永远霸榜

**错误响应:**
- `404 Not Found` - 文章不存在

---

### 获取评论的所有回复（子评论列表）

获取指定评论的所有回复，支持分页。

```http
GET /api/comments/{commentId}/replies?page=0&size=20
```

**路径参数:**
- `commentId`: 评论ID

**查询参数:**
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认20

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 100,
      "content": "第一条回复",
      "postId": 10,
      "postTitle": "文章标题",
      "parentId": 5,
      "replyToUserId": null,
      "replyToUsername": null,
      "level": 1,
      "replyCount": 0,
      "authorUsername": "用户A",
      "authorAvatarUrl": "/uploads/a/avatars/a.jpg",
      "createdAt": "2025-10-19T10:00:00",
      "updatedAt": null,
      "likeCount": 3,
      "liked": true
    },
    {
      "id": 101,
      "content": "@用户A 回复内容",
      "postId": 10,
      "postTitle": "文章标题",
      "parentId": 5,
      "replyToUserId": 200,
      "replyToUsername": "用户A",
      "level": 2,
      "replyCount": 0,
      "authorUsername": "用户B",
      "authorAvatarUrl": "/uploads/b/avatars/b.jpg",
      "createdAt": "2025-10-19T10:05:00",
      "updatedAt": null,
      "likeCount": 1,
      "liked": false
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

**说明:**
- 匿名用户可以访问此接口查看回复
- 返回所有子评论（包括嵌套回复）
- 按创建时间升序排列（最早的在前）
- 包含点赞数和当前用户点赞状态

**错误响应:**
- `404 Not Found` - 评论不存在

---

### 获取我的评论

获取当前登录用户发表的所有评论（包括顶层评论和子评论）。

```http
GET /api/comments/my?page=0&size=10
Authorization: Bearer {token}
```

**查询参数:**
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认10

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "content": "我的顶层评论",
      "postId": 10,
      "postTitle": "文章A",
      "parentId": null,
      "replyToUserId": null,
      "replyToUsername": null,
      "level": 0,
      "replyCount": 3,
      "authorUsername": "currentUser",
      "authorAvatarUrl": "https://example.com/my-avatar.jpg",
      "createdAt": "2025-10-16T15:30:00",
      "updatedAt": null,
      "likeCount": 5,
      "liked": true
    },
    {
      "id": 100,
      "content": "@张三 我的回复",
      "postId": 20,
      "postTitle": "文章B",
      "parentId": 50,
      "replyToUserId": 123,
      "replyToUsername": "张三",
      "level": 1,
      "replyCount": 0,
      "authorUsername": "currentUser",
      "authorAvatarUrl": "https://example.com/my-avatar.jpg",
      "createdAt": "2025-10-19T10:00:00",
      "updatedAt": null,
      "likeCount": 2,
      "liked": false
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

**说明:**
- 包含顶层评论和子评论
- 可通过parentId和level字段区分评论类型

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效

---

### 更新评论

更新自己创建的评论内容。

```http
PUT /api/comments/{commentId}
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数:**
- `commentId`: 评论ID

**请求体:**
```json
{
  "content": "更新后的评论内容"
}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "content": "更新后的评论内容",
  "postId": 10,
  "postTitle": "文章标题",
  "parentId": null,
  "replyToUserId": null,
  "replyToUsername": null,
  "level": 0,
  "replyCount": 2,
  "authorUsername": "用户名",
  "authorAvatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-10-16T15:30:00",
  "updatedAt": "2025-10-16T16:00:00",
  "likeCount": 2,
  "liked": true
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 无权限编辑此评论（非评论作者）
- `404 Not Found` - 评论不存在

---

### 删除评论

删除一条评论。评论作者和文章作者都可以删除评论。

**注意**：删除顶层评论会级联删除所有子评论及其点赞记录。

```http
DELETE /api/comments/{commentId}
Authorization: Bearer {token}
```

**路径参数:**
- `commentId`: 评论ID

**成功响应:** `200 OK`
```json
"评论删除成功"
```

**权限说明:**
- 评论作者可以删除自己的评论
- 文章作者可以删除自己文章下的任何评论

**级联删除:**
- 删除顶层评论时，会自动删除所有子评论及其点赞
- 删除子评论时，会自动删除该子评论的所有子孙评论及其点赞

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 无权限删除此评论
- `404 Not Found` - 评论不存在

## 评论点赞相关接口

### 点赞评论

给评论点赞（支持顶层评论和子评论）。如果已经点赞，则不会重复添加。

```http
POST /api/comments/{commentId}/likes
Authorization: Bearer {token}
```

**路径参数:**
- `commentId`: 评论ID

**成功响应:** `200 OK`
```json
{
  "likeCount": 5,
  "liked": true
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 评论不存在

---

### 取消点赞评论

取消对评论的点赞。如果未点赞，则不会有任何影响。

```http
DELETE /api/comments/{commentId}/likes
Authorization: Bearer {token}
```

**路径参数:**
- `commentId`: 评论ID

**成功响应:** `200 OK`
```json
{
  "likeCount": 4,
  "liked": false
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 评论不存在

---

### 获取评论点赞信息

获取评论的点赞数量和当前用户的点赞状态。

```http
GET /api/comments/{commentId}/likes
```

**路径参数:**
- `commentId`: 评论ID

**成功响应:** `200 OK`
```json
{
  "likeCount": 5,
  "liked": true
}
```

**说明:**
- 匿名用户可以访问此接口查看点赞数量
- 对于匿名用户，`liked` 始终为 `false`
- 登录用户可以看到自己是否已点赞

**错误响应:**
- `404 Not Found` - 评论不存在

---

## 标签相关接口

标签系统支持为文章添加标签以便分类和管理。标签具有以下特性：
- 支持颜色和图标自定义，便于前端展示
- 自动追踪创建者信息，便于后台管理
- 支持排序功能，控制标签显示顺序
- 严格的参数验证，确保数据质量
- 统一的错误响应格式

**字段说明：**
- `name`: 标签名称，唯一，只能包含中文、英文、数字、空格、下划线和连字符
- `description`: 标签描述
- `color`: 标签颜色，必须符合 #RRGGBB 格式
- `icon`: 标签图标名称（如 fa-java）
- `sortOrder`: 排序顺序（≥0）
- `createdById` / `createdByUsername`: 创建者信息（响应字段）

### 创建标签

创建一个新的标签。标签可以用于文章分类和管理。

**需要认证。系统会自动记录创建者信息。**

```http
POST /api/tags
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "name": "Java",
  "description": "Java编程语言相关内容",
  "color": "#FF5733",
  "icon": "fa-java",
  "sortOrder": 1
}
```

**字段说明:**
- `name` (String, 必填): 标签名称，长度1-50字符，必须唯一，只能包含中文、英文、数字、空格、下划线和连字符
- `description` (String, 可选): 标签描述，长度不超过200字符
- `color` (String, 可选): 标签颜色代码，必须符合 #RRGGBB 格式（如 #FF5733）
- `icon` (String, 可选): 标签图标名称或图标类，长度不超过50字符（如 fa-java）
- `sortOrder` (Integer, 可选): 排序顺序，必须≥0，数字越小越靠前

**成功响应:** `201 Created`
```json
{
  "id": 1,
  "name": "Java",
  "description": "Java编程语言相关内容",
  "color": "#FF5733",
  "icon": "fa-java",
  "sortOrder": 1,
  "createdById": 123,
  "createdByUsername": "admin",
  "createdById": 123,
  "createdByUsername": "admin",
  "postCount": 0,
  "createdAt": "2025-11-08T10:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `400 Bad Request` - 标签名称已存在或参数不符合要求
- `401 Unauthorized` - 未登录或 token 无效

---

### 获取标签详情

根据ID获取标签详细信息。

```http
GET /api/tags/{id}
```

**路径参数:**
- `id`: 标签ID

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "Java",
  "description": "Java编程语言相关内容",
  "color": "#FF5733",
  "icon": "fa-java",
  "sortOrder": 1,
  "createdById": 123,
  "createdByUsername": "admin",
  "postCount": 5,
  "createdAt": "2025-10-26T10:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `404 Not Found` - 标签不存在

---

### 根据名称获取标签

根据标签名称获取标签详细信息。

```http
GET /api/tags/name/{name}
```

**路径参数:**
- `name`: 标签名称

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "Java",
  "description": "Java编程语言相关内容",
  "color": "#FF5733",
  "icon": "fa-java",
  "sortOrder": 1,
  "createdById": 123,
  "createdByUsername": "admin",
  "postCount": 5,
  "createdAt": "2025-10-26T10:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `404 Not Found` - 标签不存在

---

### 获取所有标签

获取所有标签列表，支持分页。

```http
GET /api/tags?page=0&size=20
```

**查询参数:**
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认20

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Java",
      "description": "Java编程语言相关内容",
      "color": "#FF5733",
      "icon": "fa-java",
      "sortOrder": 1,
  "createdById": 123,
  "createdByUsername": "admin",
      "postCount": 5,
      "createdAt": "2025-10-26T10:00:00",
      "updatedAt": null
    },
    {
      "id": 2,
      "name": "Spring",
      "description": "Spring框架相关内容",
      "color": "#6DB33F",
      "icon": "fa-leaf",
      "sortOrder": 2,
  "createdById": 123,
  "createdByUsername": "admin",
      "postCount": 3,
      "createdAt": "2025-10-26T11:00:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

---

### 获取热门标签

获取按文章数量排序的热门标签列表。

```http
GET /api/tags/popular
```

**成功响应:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Java",
    "description": "Java编程语言相关内容",
    "color": "#FF5733",
    "icon": "fa-java",
    "sortOrder": 1,
  "createdById": 123,
  "createdByUsername": "admin",
    "postCount": 10,
    "createdAt": "2025-10-26T10:00:00",
    "updatedAt": null
  },
  {
    "id": 2,
    "name": "Spring",
    "description": "Spring框架相关内容",
    "color": "#6DB33F",
    "icon": "fa-leaf",
    "sortOrder": 2,
  "createdById": 123,
  "createdByUsername": "admin",
    "postCount": 8,
    "createdAt": "2025-10-26T11:00:00",
    "updatedAt": null
  }
]
```

---

### 更新标签

更新标签的信息。

**需要认证。**

```http
PUT /api/tags/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数:**
- `id`: 标签ID

**请求体:**
```json
{
  "name": "Java Programming",
  "description": "Java编程语言及相关技术",
  "color": "#007396",
  "icon": "fa-coffee",
  "sortOrder": 1
}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "Java Programming",
  "description": "Java编程语言及相关技术",
  "color": "#007396",
  "icon": "fa-coffee",
  "sortOrder": 1,
  "createdById": 123,
  "createdByUsername": "admin",
  "postCount": 5,
  "createdAt": "2025-10-26T10:00:00",
  "updatedAt": "2025-10-26T15:00:00"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 标签不存在
- `400 Bad Request` - 标签名称已存在或参数不符合要求

---

### 删除标签

删除指定的标签。删除标签不会删除使用该标签的文章。

**需要认证。**

```http
DELETE /api/tags/{id}
Authorization: Bearer {token}
```

**路径参数:**
- `id`: 标签ID

**成功响应:** `200 OK`
```json
"标签删除成功"
```

**说明:**
- 删除标签会自动从所有文章中移除该标签的关联关系
- 不会影响文章本身

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 标签不存在

---

### 文章中使用标签

创建或更新文章时，可以通过 `tags` 字段关联标签。

**创建带标签的文章示例:**
```http
POST /api/posts
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "title": "Spring Boot入门教程",
  "content": "这是一篇关于Spring Boot的教程...",
  "contentType": "MARKDOWN",
  "draft": false,
  "tags": ["Java", "Spring", "教程"]
}
```

**说明:**
- `tags` 字段为可选字符串数组
- 如果标签不存在，系统会自动创建新标签
- 如果标签已存在，会直接关联已有标签
- 更新文章时也可以修改标签列表

**文章响应示例（包含标签）:**
```json
{
  "id": 1,
  "title": "Spring Boot入门教程",
  "content": "这是一篇关于Spring Boot的教程...",
  "authorUsername": "testuser",
  "authorAvatarUrl": "/uploads/1/avatars/abc.jpg",
  "createdAt": "2025-10-26T10:00:00",
  "updatedAt": null,
  "publishedAt": "2025-10-26T10:00:00",
  "contentType": "MARKDOWN",
  "draft": false,
  "likeCount": 0,
  "isLiked": false,
  "commentCount": 0,
  "tags": [
    {
      "id": 1,
      "name": "Java",
      "description": null
    },
    {
      "id": 2,
      "name": "Spring",
      "description": null
    },
    {
      "id": 3,
      "name": "教程",
      "description": null
    }
  ]
}
```

---

## 分类相关接口

分类系统用于对文章进行宏观分组，每篇文章只能属于一个分类。分类具有以下特性：
- 一对多关系：一个分类可以包含多篇文章，一篇文章只能属于一个分类
- 支持颜色和图标自定义，便于前端展示
- 自动追踪创建者信息，便于后台管理
- 支持排序功能，控制分类显示顺序
- 删除保护：无法删除仍在使用中的分类
- 严格的参数验证，确保数据质量

**字段说明：**
- `name`: 分类名称，唯一，只能包含中文、英文、数字、空格、下划线和连字符
- `description`: 分类描述
- `color`: 分类颜色，必须符合 #RRGGBB 格式
- `icon`: 分类图标名称
- `sortOrder`: 排序顺序（≥0）
- `createdById` / `createdByUsername`: 创建者信息（响应字段）

### 创建分类

创建一个新的分类。

**需要认证。系统会自动记录创建者信息。**

**注意**：创建文章时如果指定的分类不存在，系统会自动创建该分类，因此通常不需要单独调用此接口。

```http
POST /api/categories
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "name": "技术文章",
  "description": "技术相关的文章分类",
  "color": "#3B82F6",
  "icon": "tech-icon",
  "sortOrder": 1
}
```

**字段说明:**
- `name` (String, 必填): 分类名称，长度1-50字符，必须唯一，只能包含中文、英文、数字、空格、下划线和连字符
- `description` (String, 可选): 分类描述，长度不超过200字符
- `color` (String, 可选): 分类颜色代码，必须符合 #RRGGBB 格式（如 #3B82F6）
- `icon` (String, 可选): 分类图标名称，长度不超过50字符
- `sortOrder` (Integer, 可选): 排序顺序，必须≥0，数字越小越靠前

**成功响应:** `201 Created`
```json
{
  "id": 1,
  "name": "技术文章",
  "description": "技术相关的文章分类",
  "color": "#3B82F6",
  "icon": "tech-icon",
  "sortOrder": 1,
  "createdById": 1,
  "createdByUsername": "admin",
  "postCount": 0,
  "createdAt": "2025-11-08T14:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `400 Bad Request` - 分类名称已存在或参数不符合要求
- `401 Unauthorized` - 未登录或 token 无效

---

### 获取分类详情

根据ID获取分类详细信息。

```http
GET /api/categories/{id}
```

**路径参数:**
- `id`: 分类ID

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "技术文章",
  "description": "技术相关的文章分类",
  "color": "#3B82F6",
  "icon": "tech-icon",
  "sortOrder": 1,
  "createdById": 1,
  "createdByUsername": "admin",
  "postCount": 5,
  "createdAt": "2025-11-08T14:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `404 Not Found` - 分类不存在

---

### 根据名称获取分类

根据分类名称获取分类详细信息。

```http
GET /api/categories/name/{name}
```

**路径参数:**
- `name`: 分类名称

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "技术文章",
  "description": "技术相关的文章分类",
  "color": "#3B82F6",
  "icon": "tech-icon",
  "sortOrder": 1,
  "createdById": 1,
  "createdByUsername": "admin",
  "postCount": 5,
  "createdAt": "2025-11-08T14:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `404 Not Found` - 分类不存在

---

### 获取所有分类（分页）

获取所有分类列表，支持分页和排序。

```http
GET /api/categories?page=0&size=20&sort=sortOrder,asc
```

**查询参数:**
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认20
- `sort` (可选): 排序字段和方向，如 `sortOrder,asc` 或 `createdAt,desc`

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "技术文章",
      "description": "技术相关的文章分类",
      "color": "#3B82F6",
      "icon": "tech-icon",
      "sortOrder": 1,
      "createdById": 1,
      "createdByUsername": "admin",
      "postCount": 5,
      "createdAt": "2025-11-08T14:00:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 获取热门分类

获取按文章数量降序排列的所有分类。

```http
GET /api/categories/popular
```

**成功响应:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "技术文章",
    "description": "技术相关的文章分类",
    "color": "#3B82F6",
    "icon": "tech-icon",
    "sortOrder": 1,
    "createdById": 1,
    "createdByUsername": "admin",
    "postCount": 10,
    "createdAt": "2025-11-08T14:00:00",
    "updatedAt": null
  },
  {
    "id": 2,
    "name": "生活随笔",
    "postCount": 3,
    ...
  }
]
```

---

### 更新分类

更新分类信息。

**需要认证。**

```http
PUT /api/categories/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数:**
- `id`: 分类ID

**请求体:**
```json
{
  "name": "技术文章",
  "description": "更新后的描述",
  "color": "#3B82F6",
  "icon": "new-icon",
  "sortOrder": 2
}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "技术文章",
  "description": "更新后的描述",
  "color": "#3B82F6",
  "icon": "new-icon",
  "sortOrder": 2,
  "createdById": 1,
  "createdByUsername": "admin",
  "postCount": 5,
  "createdAt": "2025-11-08T14:00:00",
  "updatedAt": "2025-11-08T15:00:00"
}
```

**错误响应:**
- `400 Bad Request` - 分类名称已存在或参数不符合要求
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 分类不存在

---

### 删除分类

硬删除指定分类（管理员操作）。

**需要认证。删除后，关联的文章不会被删除，只是category字段会被设置为null。**

```http
DELETE /api/categories/{id}
Authorization: Bearer {token}
```

**路径参数:**
- `id`: 分类ID

**成功响应:** `200 OK`
```json
"分类删除成功"
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 分类不存在

---

### 从文章中移除标签（软删除）

用户可以从自己的文章中移除指定标签，标签本身不会被删除。

```http
DELETE /api/posts/{postId}/tags/{tagName}
Authorization: Bearer {token}
```

**路径参数:**
- `postId`: 文章ID
- `tagName`: 标签名称（需要URL编码）

**成功响应:** `200 OK`
返回更新后的文章详情（格式同"获取文章详情"）

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 无权操作该文章（不是文章作者）
- `404 Not Found` - 文章或标签不存在

---

### 从文章中移除分类（软删除）

用户可以从自己的文章中移除分类，分类本身不会被删除。

```http
DELETE /api/posts/{postId}/category
Authorization: Bearer {token}
```

**路径参数:**
- `postId`: 文章ID

**成功响应:** `200 OK`
返回更新后的文章详情（格式同"获取文章详情"）

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 无权操作该文章（不是文章作者）
- `404 Not Found` - 文章不存在

---

## 错误码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或 token 无效） |
| 403 | 禁止访问（无权限） |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |

## 标准错误响应格式

```json
{
  "timestamp": "2025-10-14T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "具体错误信息",
  "path": "/api/posts"
}
```

---

## 社区统计接口 (可选)

### 获取社区统计

获取社区的各项统计数据。

```http
GET /api/statistics
```

**成功响应:** `200 OK`
```json
{
  "totalUsers": 1250,
  "totalPosts": 4823,
  "onlineUsers": 42,
  "todayVisits": 356
}
```

**字段说明:**
- `totalUsers`: 用户总数
- `totalPosts`: 文章总数
- `onlineUsers`: 当前在线用户数
- `todayVisits`: 今日访问数


**错误响应:**
- `500 Internal Server Error` - 服务器内部错误

**实现建议:**
- 可使用缓存减少数据库查询
- 在线用户数可基于活跃session或最近活动时间计算
- 今日访问数建议使用Redis或数据库日志统计


---

## 关注相关接口

### 关注用户

关注指定用户。如果双方互相关注，自动标记为朋友。

```http
POST /api/users/{userId}/follow
Authorization: Bearer {token}
```

**路径参数:**
- `userId`: 要关注的用户ID

**成功响应:** `200 OK`
```json
{
  "following": true,
  "friend": false,
  "message": "关注成功"
}
```

如果对方也关注了当前用户，会自动成为朋友：
```json
{
  "following": true,
  "friend": true,
  "message": "关注成功，你们已成为朋友"
}
```

**错误响应:**
- `400 Bad Request` - 不能关注自己
- `401 Unauthorized` - 未登录
- `404 Not Found` - 用户不存在

---

### 取消关注

取消关注指定用户。

```http
DELETE /api/users/{userId}/follow
Authorization: Bearer {token}
```

**路径参数:**
- `userId`: 要取消关注的用户ID

**成功响应:** `200 OK`
```json
{
  "following": false,
  "friend": false,
  "message": "取消关注成功"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录
- `404 Not Found` - 用户不存在

---

### 获取关注统计

获取指定用户的关注统计信息。

```http
GET /api/users/{userId}/follow/stats
Authorization: Bearer {token}  (可选)
```

**路径参数:**
- `userId`: 用户ID

**成功响应:** `200 OK`
```json
{
  "followingCount": 42,
  "followerCount": 128,
  "friendCount": 15,
  "isFollowing": true,
  "isFriend": false
}
```

**字段说明:**
- `followingCount`: 关注数（该用户关注了多少人），如果被隐藏则返回 -1
- `followerCount`: 粉丝数（有多少人关注该用户），如果被隐藏则返回 -1
- `friendCount`: 朋友数（互相关注的用户数量），如果被隐藏则返回 -1
- `isFollowing`: 当前登录用户是否已关注该用户（未登录时为null）
- `isFriend`: 是否为朋友关系（未登录时为null）

**注意:** 如果用户设置了可见性限制且当前查看者无权查看，计数字段会返回 -1。

---

### 获取关注列表

获取指定用户的关注列表（该用户关注了谁）。

```http
GET /api/users/{userId}/following?page=0&size=20
Authorization: Bearer {token}  (可选)
```

**路径参数:**
- `userId`: 用户ID

**查询参数:**
- `page`: 页码，从0开始，默认0
- `size`: 每页数量，1-100，默认20

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 2,
      "username": "alice",
      "nickname": "Alice",
      "avatarUrl": "/uploads/2/avatars/xxx.jpg",
      "bio": "Hello world",
      "followedAt": "2026-01-30T10:30:00",
      "friend": true
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

**注意:** 如果用户设置了可见性限制且当前查看者无权查看，返回空列表。

---

### 获取粉丝列表

获取指定用户的粉丝列表（谁关注了该用户）。

```http
GET /api/users/{userId}/followers?page=0&size=20
Authorization: Bearer {token}  (可选)
```

**路径参数:**
- `userId`: 用户ID

**查询参数:**
- `page`: 页码，从0开始，默认0
- `size`: 每页数量，1-100，默认20

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 3,
      "username": "bob",
      "nickname": "Bob",
      "avatarUrl": "/uploads/3/avatars/xxx.jpg",
      "bio": "Nice to meet you",
      "followedAt": "2026-01-29T15:45:00",
      "friend": false
    }
  ],
  "totalElements": 128,
  "totalPages": 7,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

---

### 获取朋友列表

获取指定用户的朋友列表（互相关注的用户）。

```http
GET /api/users/{userId}/friends?page=0&size=20
Authorization: Bearer {token}  (可选)
```

**路径参数:**
- `userId`: 用户ID

**查询参数:**
- `page`: 页码，从0开始，默认0
- `size`: 每页数量，1-100，默认20

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 2,
      "username": "alice",
      "nickname": "Alice",
      "avatarUrl": "/uploads/2/avatars/xxx.jpg",
      "bio": "Hello world",
      "followedAt": "2026-01-30T10:30:00",
      "friend": true
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

---

### 获取可见性设置

获取当前用户的关注信息可见性设置。

```http
GET /api/follow/visibility
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "following": {
    "isPublic": true,
    "visibleToFriends": false,
    "visibleToFollowing": false,
    "allowedNicknames": [],
    "blockedNicknames": []
  },
  "followers": {
    "isPublic": true,
    "visibleToFriends": false,
    "visibleToFollowing": false,
    "allowedNicknames": [],
    "blockedNicknames": []
  },
  "friends": {
    "isPublic": true,
    "visibleToFriends": false,
    "visibleToFollowing": false,
    "allowedNicknames": [],
    "blockedNicknames": []
  },
  "stats": {
    "isPublic": true,
    "visibleToFriends": false,
    "visibleToFollowing": false,
    "allowedNicknames": [],
    "blockedNicknames": []
  }
}
```

**字段说明:**

每种类型（following/followers/friends/stats）都有独立的设置：
- `isPublic`: 是否公开可见（所有人都可以看）
- `visibleToFriends`: 是否对朋友可见（互相关注的用户可以看）
- `visibleToFollowing`: 是否对我关注的人可见（我关注的用户可以看）
- `allowedNicknames`: 允许查看的用户昵称列表
- `blockedNicknames`: 禁止查看的用户昵称列表（最高优先级）

**错误响应:**
- `401 Unauthorized` - 未登录

---

### 更新可见性设置

更新当前用户的关注信息可见性设置。可以只更新需要修改的类型。

```http
PUT /api/follow/visibility
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "following": {
    "isPublic": false,
    "visibleToFriends": true,
    "visibleToFollowing": false,
    "allowedNicknames": ["alice", "bob"],
    "blockedNicknames": ["spammer"]
  },
  "stats": {
    "isPublic": true
  }
}
```

**注意:** 可以只传入需要修改的类型，未传入的类型保持原有设置。每个类型内也可以只传入需要修改的字段。

**成功响应:** `200 OK`

返回更新后的完整可见性设置（格式同获取可见性设置）。

**可见性规则优先级:**
1. 如果是本人查看 → 允许
2. 如果在 `blockedNicknames` 中 → 拒绝（最高优先级）
3. 如果 `isPublic` = true → 允许
4. 如果在 `allowedNicknames` 中 → 允许
5. 如果 `visibleToFriends` = true 且是朋友 → 允许
6. 如果 `visibleToFollowing` = true 且被关注 → 允许
7. 否则 → 拒绝

**错误响应:**
- `401 Unauthorized` - 未登录

---

## 私信相关接口

### 发送私信

发送一条私信给指定用户。

```http
POST /api/messages/send/{receiverId}
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体:**
```json
{
  "content": "你好，很高兴认识你！"
}
```

**成功响应:** `201 Created`
```json
{
  "id": 1,
  "senderId": 2,
  "senderUsername": "alice",
  "senderNickname": "Alice",
  "senderAvatarUrl": "/uploads/avatars/2/avatar.jpg",
  "receiverId": 3,
  "receiverUsername": "bob",
  "receiverNickname": "Bob",
  "receiverAvatarUrl": null,
  "content": "你好，很高兴认识你！",
  "createdAt": "2025-01-31T10:30:00Z",
  "readAt": null,
  "read": false,
  "sentByMe": true
}
```

**错误响应:**
- `400 Bad Request` - 不能给自己发送私信
- `400 Bad Request` - 在对方回复前，您无法发送第二条消息（防骚扰机制）
- `404 Not Found` - 用户不存在

---

### 获取对话消息

获取与指定用户的对话消息列表（分页）。

```http
GET /api/messages/conversation/{partnerId}?page=0&size=20
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 5,
      "senderId": 3,
      "senderUsername": "bob",
      "content": "我也是！",
      "createdAt": "2025-01-31T10:35:00Z",
      "read": true,
      "sentByMe": false
    }
  ],
  "totalPages": 1,
  "totalElements": 5,
  "size": 20,
  "number": 0
}
```

---

### 获取会话列表

获取当前用户的所有会话列表。

```http
GET /api/messages/conversations
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
[
  {
    "partnerId": 3,
    "partnerUsername": "bob",
    "partnerNickname": "Bob",
    "partnerAvatarUrl": null,
    "lastMessageContent": "我也是！",
    "lastMessageTime": "2025-01-31T10:35:00Z",
    "lastMessageSentByMe": false,
    "unreadCount": 2,
    "friend": true
  }
]
```

---

### 标记消息为已读

标记与指定用户的所有消息为已读。

```http
PUT /api/messages/read/{partnerId}
Authorization: Bearer {token}
```

**成功响应:** `200 OK`

---

### 获取未读消息总数

获取当前用户的未读消息总数。

```http
GET /api/messages/unread/count
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "count": 5
}
```

---

### 获取来自特定用户的未读消息数

获取来自指定用户的未读消息数量。

```http
GET /api/messages/unread/count/{userId}
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "count": 2
}
```

---

### 检查是否可以发送消息

检查当前用户是否可以给指定用户发送消息（防骚扰机制）。

```http
GET /api/messages/can-send/{receiverId}
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "canSend": true
}
```

**防骚扰机制说明:**
- 如果两人是朋友（互相关注），可以无限制发送消息
- 如果不是朋友，发送一条消息后，在对方回复前无法发送第二条消息

---

## 通知相关接口

### 获取通知列表

获取当前用户的通知列表，支持分页和类型过滤。

```http
GET /api/notifications?page=0&size=20&filter=all
Authorization: Bearer {token}
```

**查询参数:**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | integer | 0 | 页码（从0开始）|
| size | integer | 20 | 每页数量 |
| filter | string | all | 过滤类型：`all`（全部）、`comments`（评论）、`likes`（点赞）、`follows`（关注）、`messages`（私信）|

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "type": "POST_COMMENTED",
      "actorId": 2,
      "actorUsername": "bob",
      "actorNickname": "Bob",
      "actorAvatarUrl": "/uploads/2/avatars/abc.jpg",
      "recipientId": 1,
      "postId": 5,
      "postTitle": "Vue.js 入门教程",
      "commentId": 12,
      "content": "写得真好！",
      "read": false,
      "createdAt": "2026-02-01T10:30:00Z"
    },
    {
      "id": 2,
      "type": "FOLLOWED",
      "actorId": 3,
      "actorUsername": "charlie",
      "actorNickname": "Charlie",
      "actorAvatarUrl": null,
      "recipientId": 1,
      "postId": null,
      "postTitle": null,
      "commentId": null,
      "content": null,
      "read": true,
      "createdAt": "2026-02-01T09:15:00Z"
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0
}
```

**通知类型说明:**

| 类型 | 说明 |
|------|------|
| POST_LIKED | 文章被点赞 |
| POST_COMMENTED | 文章收到评论 |
| COMMENT_LIKED | 评论被点赞 |
| COMMENT_REPLIED | 评论收到回复 |
| FOLLOWED | 被关注 |
| MESSAGE_RECEIVED | 收到私信 |

---

### 获取最近通知

获取最近的通知（用于导航栏下拉列表）。

```http
GET /api/notifications/recent
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
[
  {
    "id": 1,
    "type": "POST_COMMENTED",
    "actorId": 2,
    "actorUsername": "bob",
    "actorNickname": "Bob",
    "actorAvatarUrl": "/uploads/2/avatars/abc.jpg",
    "recipientId": 1,
    "postId": 5,
    "postTitle": "Vue.js 入门教程",
    "commentId": 12,
    "content": "写得真好！",
    "read": false,
    "createdAt": "2026-02-01T10:30:00Z"
  }
]
```

---

### 获取未读通知数量

获取当前用户的未读通知总数。

```http
GET /api/notifications/unread/count
Authorization: Bearer {token}
```

**成功响应:** `200 OK`
```json
{
  "count": 5
}
```

---

### 标记单条通知为已读

将指定通知标记为已读。

```http
PUT /api/notifications/{id}/read
Authorization: Bearer {token}
```

**成功响应:** `200 OK`

---

### 标记通知为已读（批量）

批量标记通知为已读，支持按类型过滤。

```http
PUT /api/notifications/read?filter=all
Authorization: Bearer {token}
```

**查询参数:**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| filter | string | all | 过滤类型：`all`（全部）、`comments`（评论）、`likes`（点赞）、`follows`（关注）、`messages`（私信）|

**成功响应:** `200 OK`

**说明:**
- 当 `filter=all` 时，标记所有未读通知为已读
- 当 `filter=comments` 时，仅标记评论相关通知（POST_COMMENTED, COMMENT_REPLIED）为已读
- 其他 filter 类型同理
