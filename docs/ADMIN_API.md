# 管理后台 API 文档

本文档详细说明管理后台 REST API 接口的使用方法。

## 基础信息

- **基础 URL**: `http://localhost:8080/api/admin`
- **认证方式**: JWT Bearer Token
- **权限要求**: 需要 `ADMIN` 角色
- **内容类型**: `application/json`

## 认证说明

所有管理后台接口都需要 ADMIN 角色的 JWT Token：

```http
Authorization: Bearer {admin-jwt-token}
```

### 获取管理员 Token

管理员可以使用以下任一方式获取 Token：

**方式1 - 统一登录接口** (推荐):
```http
POST /api/users/login
Content-Type: application/json

{
  "username": "admin_username",
  "password": "admin_password",
  "rememberMe": true
}
```

**方式2 - 管理员专用登录**:
```http
POST /api/admin/login
Content-Type: application/json

{
  "username": "admin_username",
  "password": "admin_password",
  "rememberMe": true
}
```

> 注意：方式2 会验证用户是否具有 ADMIN 角色，非管理员会返回 403 Forbidden。

---

## 设置管理员账户

现有用户默认为 `USER` 角色。要将用户提升为管理员，需要在数据库中执行：

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_admin_username';
```

---

## 管理员接口

### 管理员登录

验证用户凭证并检查是否具有管理员角色。

```http
POST /api/admin/login
Content-Type: application/json
```

**请求体:**
```json
{
  "username": "admin",
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
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer"
}
```

**错误响应:**
- `401 Unauthorized` - 用户名或密码错误
- `403 Forbidden` - 用户没有管理员权限

```json
"您没有管理员权限"
```

---

### 获取当前管理员信息

获取当前登录管理员的详细信息。

```http
GET /api/admin/me
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "nickname": "管理员",
  "bio": "系统管理员",
  "avatarUrl": "/uploads/1/avatars/avatar.jpg",
  "role": "ADMIN",
  "createdAt": "2024-01-01T00:00:00"
}
```

**错误响应:**
- `401 Unauthorized` - 未登录或 Token 无效
- `403 Forbidden` - 没有管理员权限

---

### 获取仪表盘数据

获取管理后台仪表盘的统计数据。

```http
GET /api/admin/dashboard
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "message": "欢迎进入管理后台",
  "admin": "admin"
}
```

> 注意：仪表盘 API 目前返回基础信息，后续将扩展支持：
> - 用户总数统计
> - 文章总数统计
> - 评论总数统计
> - 今日访问量
> - 最新文章列表
> - 待审核内容列表

---

## Token 刷新

管理员使用与普通用户相同的 Token 刷新接口：

```http
POST /api/users/refresh-token
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "rememberMe": true
}
```

**成功响应:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer"
}
```

---

## 权限控制

### Spring Security 配置

管理后台接口通过两层安全控制保护：

1. **URL级别**: Spring Security 配置 `/api/admin/**` 路径需要 `ADMIN` 角色
2. **方法级别**: 所有管理员接口添加 `@PreAuthorize("hasRole('ADMIN')")` 注解

### 错误码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未认证（Token 无效或过期） |
| 403 | 无权限（用户不是管理员） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 后续规划

管理后台 API 将陆续增加以下功能：

### 用户管理
- `GET /api/admin/users` - 获取用户列表
- `PUT /api/admin/users/{id}/role` - 修改用户角色
- `PUT /api/admin/users/{id}/status` - 禁用/启用用户

### 内容管理
- `GET /api/admin/posts` - 获取文章列表（含审核状态）
- `PUT /api/admin/posts/{id}/status` - 修改文章状态
- `DELETE /api/admin/posts/{id}` - 删除文章

### 评论管理
- `GET /api/admin/comments` - 获取评论列表
- `PUT /api/admin/comments/{id}/status` - 审核评论
- `DELETE /api/admin/comments/{id}` - 删除评论

### 系统设置
- `GET /api/admin/settings` - 获取系统设置
- `PUT /api/admin/settings` - 更新系统设置
