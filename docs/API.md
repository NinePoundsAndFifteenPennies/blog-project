# API 接口文档

本文档详细说明所有 REST API 接口的使用方法。

## 基础信息

- **基础 URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **内容类型**: `application/json` (除文件上传外)

## 认证说明

需要认证的接口必须在请求头中携带 JWT Token：

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
- `rememberMe`: 是否记住我（true: 30天，false: 1小时）

**成功响应:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
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
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "avatarUrl": "/uploads/1/avatars/abc123.jpg",
  "createdAt": "2025-10-14T10:30:00"
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

**请求体:**
```json
{
  "rememberMe": false
}
```

**成功响应:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
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
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "avatarUrl": "/uploads/1/avatars/abc123.jpg"
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
  "id": 1,
  "title": "文章标题",
  "content": "文章内容",
  "contentType": "MARKDOWN",
  "authorUsername": "testuser",
  "createdAt": "2025-10-14T10:30:00",
  "updatedAt": null,
  "draft": false
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
      "id": 1,
      "title": "文章标题",
      "content": "文章内容",
      "contentType": "MARKDOWN",
      "authorUsername": "testuser",
      "createdAt": "2025-10-14T10:30:00",
      "draft": false
    }
  ],
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0
}
```

---

### 获取我的文章

获取当前用户的所有文章（包括草稿）。

```http
GET /api/posts/my?page=0&size=10
Authorization: Bearer {token}
```

**成功响应:** `200 OK`（格式同获取文章列表）

---

### 获取单篇文章

根据 ID 获取文章详情。

```http
GET /api/posts/{id}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "title": "文章标题",
  "content": "文章内容",
  "contentType": "MARKDOWN",
  "authorUsername": "testuser",
  "createdAt": "2025-10-14T10:30:00",
  "updatedAt": null,
  "draft": false
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

**成功响应:** `200 OK`（返回更新后的文章）

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

## 使用示例

### 完整的头像上传流程

1. **上传头像文件**
   ```bash
   curl -X POST http://localhost:8080/api/files/upload/avatar \
     -H "Authorization: Bearer {token}" \
     -F "file=@avatar.jpg"
   ```

2. **保存头像 URL**
   ```bash
   curl -X POST http://localhost:8080/api/users/me/avatar \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"avatarUrl": "/uploads/1/avatars/abc123.jpg"}'
   ```

3. **验证头像已保存**
   ```bash
   curl -X GET http://localhost:8080/api/users/me \
     -H "Authorization: Bearer {token}"
   ```

### 完整的文章发布流程

1. **创建文章**
   ```bash
   curl -X POST http://localhost:8080/api/posts \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "我的第一篇文章",
       "content": "# 标题\n\n这是内容",
       "contentType": "MARKDOWN",
       "draft": false
     }'
   ```

2. **查看文章列表**
   ```bash
   curl -X GET http://localhost:8080/api/posts?page=0&size=10
   ```
