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
  "avatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效

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
  "draft": false
}
```

**参数说明:**
- `title`: 文章标题（必填）
- `content`: 文章内容（必填）
- `contentType`: 内容类型，`MARKDOWN` 或 `HTML`
- `draft`: 是否为草稿（true: 草稿，false: 发布）

**成功响应:** `201 Created`
```json
{
  "id": 24,
  "title": "测试api2",
  "content": "# 这是标题\n\n这是文章内容...",
  "authorUsername": "seconduser",
  "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
  "createdAt": "2025-10-17T17:58:06.0262871",
  "updatedAt": null,
  "publishedAt": "2025-10-17T17:58:06.0262871",
  "contentType": "MARKDOWN",
  "draft": false,
  "likeCount": 0,
  "isLiked": false,
  "commentCount": 0
}
```

---

### 获取文章列表

分页获取所有已发布的文章。

```http
GET /api/posts?page=0&size=10&sort=createdAt,desc
```

**查询参数:**
- `page`: 页码（从 0 开始，默认 0）
- `size`: 每页数量（默认 10）
- `sort`: 排序字段和方向（默认 `createdAt,desc`）

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 24,
      "title": "测试api2",
      "content": "# 这是标题\n\n这是文章内容...",
      "authorUsername": "seconduser",
      "authorAvatarUrl": "/uploads/2/avatars/9d70a757-54e1-41eb-99df-b509ba97b7dc.jpg",
      "createdAt": "2025-10-17T17:58:06.026287",
      "updatedAt": null,
      "publishedAt": "2025-10-17T17:58:06.026287",
      "contentType": "MARKDOWN",
      "draft": false,
      "likeCount": 0,
      "isLiked": false,
      "commentCount": 0
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
      "commentCount": 1
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
  "commentCount": 1
}
```

**错误响应:**
- `404 Not Found` - 文章不存在

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
    "commentCount": 1
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
- `file`: 图片文件（JPG/PNG，最大 5MB，尺寸 50x50 至 2000x2000 像素）

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
- `file`: 图片文件（JPG/PNG，最大 5MB，尺寸 50x50 至 2000x2000 像素）

**成功响应:** `200 OK`
```json
{
  "avatarUrl": "/uploads/1/avatars/def456.jpg"
}
```

**说明:** 旧头像文件会被自动删除。

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
  "isLiked": true
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
  "isLiked": false
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
  "isLiked": true
}
```

**说明:**
- 匿名用户可以访问此接口查看点赞数量
- 对于匿名用户，`isLiked` 始终为 `false`
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

### 获取文章评论

获取指定文章的所有评论，支持分页。

```http
GET /api/posts/{postId}/comments?page=0&size=20
```

**路径参数:**
- `postId`: 文章ID

**查询参数:**
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认20

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "content": "第一条评论",
      "postId": 10,
      "postTitle": "文章标题",
      "authorUsername": "user1",
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
- 评论按创建时间升序排列（最早的在前）
- 每条评论包含点赞数量和当前用户的点赞状态

**错误响应:**
- `404 Not Found` - 文章不存在

---

### 获取我的评论

获取当前登录用户发表的所有评论。

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
      "content": "我的评论",
      "postId": 10,
      "postTitle": "文章标题A",
      "authorUsername": "currentUser",
      "authorAvatarUrl": "https://example.com/my-avatar.jpg",
      "createdAt": "2025-10-16T15:30:00",
      "updatedAt": null,
      "likeCount": 5,
      "liked": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

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

**错误响应:**
- `401 Unauthorized` - 未登录或 token 无效
- `403 Forbidden` - 无权限删除此评论
- `404 Not Found` - 评论不存在

## 评论点赞相关接口

### 点赞评论

给评论点赞。如果已经点赞，则不会重复添加。

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

