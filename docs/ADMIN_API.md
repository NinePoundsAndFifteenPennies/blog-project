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

## 用户管理

### 获取用户列表

获取用户列表，支持分页和多条件搜索。

```http
GET /api/admin/users
Authorization: Bearer {admin-token}
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| username | string | 否 | 用户名/昵称搜索（模糊匹配） |
| email | string | 否 | 邮箱搜索（模糊匹配） |
| role | string | 否 | 角色过滤：USER 或 ADMIN |
| enabled | boolean | 否 | 状态过滤：true=启用, false=禁用 |
| startDate | string | 否 | 注册开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 注册结束日期（yyyy-MM-dd） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "email": "admin@example.com",
      "avatarUrl": "/uploads/1/avatars/avatar.jpg",
      "bio": "系统管理员",
      "role": "ADMIN",
      "enabled": true,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-15T12:00:00",
      "postCount": 10,
      "commentCount": 25
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

---

### 获取用户详情

获取用户详细信息，包括发文数和评论数统计。

```http
GET /api/admin/users/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "email": "admin@example.com",
  "avatarUrl": "/uploads/1/avatars/avatar.jpg",
  "bio": "系统管理员",
  "role": "ADMIN",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T12:00:00",
  "postCount": 10,
  "commentCount": 25
}
```

**错误响应:**
- `404 Not Found` - 用户不存在

---

### 更新用户状态（启用/禁用）

更新用户的启用状态。禁用后用户将无法登录，其个人主页和文章将对外隐藏。

```http
PUT /api/admin/users/{id}/status
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "enabled": false
}
```

**成功响应:** `200 OK`
```json
{
  "id": 2,
  "username": "user1",
  "enabled": false,
  ...
}
```

**错误响应:**
- `400 Bad Request` - 不能禁用自己的账户
- `404 Not Found` - 用户不存在

---

### 更新用户角色

修改用户的角色（普通用户/管理员）。

```http
PUT /api/admin/users/{id}/role
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "role": "ADMIN"
}
```

**参数说明:**
- `role`: 角色，必须为 `USER` 或 `ADMIN`

**成功响应:** `200 OK`
```json
{
  "id": 2,
  "username": "user1",
  "role": "ADMIN",
  ...
}
```

**错误响应:**
- `400 Bad Request` - 不能修改自己的角色
- `400 Bad Request` - 角色必须为USER或ADMIN
- `404 Not Found` - 用户不存在

---

## 封禁用户处理

当用户被禁用（`enabled = false`）时：

1. **强制下线**: 被封禁用户的JWT token将被拒绝，返回 `403 Forbidden`
2. **隐藏个人主页**: 访问被封禁用户的个人主页将返回 `404 Not Found`
3. **隐藏文章**: 
   - 被封禁用户的文章列表将返回空
   - 直接访问其文章详情将返回 `404 Not Found`
4. **评论**: 被封禁用户已发布的评论仍然可见（可根据需要扩展隐藏功能）

---

## 文章管理

### 文章状态说明

| 状态 | 说明 |
|------|------|
| DRAFT | 草稿 - 作者创建但未提交发布的文章 |
| PENDING_REVIEW | 待审核 - 作者提交发布申请，等待管理员审核（包括被拒绝后重新提交的文章） |
| PUBLISHED | 已发布 - 审核通过并对外展示的文章 |
| REJECTED | 已拒绝 - 审核不通过的文章 |
| PENDING_REVISION | 发布后修改待审 - 已发布文章被修改后等待重新审核，审核期间前台展示旧版本 |

### 获取文章列表

获取文章列表，支持分页和多条件搜索过滤。

```http
GET /api/admin/posts
Authorization: Bearer {admin-token}
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| title | string | 否 | 标题搜索（模糊匹配） |
| author | string | 否 | 作者用户名或昵称搜索（模糊匹配） |
| status | string | 否 | 状态过滤：DRAFT/PENDING_REVIEW/PUBLISHED/REJECTED（注：PENDING_REVISION 为内部状态，用于已发布文章修改后的审核，不作为常规筛选项） |
| tag | string | 否 | 标签名称搜索 |
| startDate | string | 否 | 创建开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 创建结束日期（yyyy-MM-dd） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "文章标题",
      "contentPreview": "文章内容预览...",
      "authorId": 1,
      "authorUsername": "user1",
      "authorNickname": "用户1",
      "authorAvatarUrl": "/uploads/1/avatars/avatar.jpg",
      "status": "PUBLISHED",
      "isDraft": false,
      "viewCount": 100,
      "likeCount": 10,
      "commentCount": 5,
      "tags": ["技术", "Java"],
      "categoryId": 1,
      "categoryName": "技术",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-15T12:00:00",
      "publishedAt": "2024-01-01T10:00:00",
      "rejectionFormId": null,
      "rejectionFormTitle": null
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

---

### 获取文章详情

获取指定文章的详细信息，包括拒绝表单信息（如有）。

```http
GET /api/admin/posts/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "title": "文章标题",
  "content": "完整文章内容...",
  "contentPreview": "文章内容预览...",
  "authorId": 1,
  "authorUsername": "user1",
  "authorNickname": "用户1",
  "status": "REJECTED",
  "rejectionFormId": 5,
  "rejectionFormTitle": "审核拒绝通知",
  "previousTitle": "修改前的标题",
  "previousContent": "修改前的内容..."
}
```

**错误响应:**
- `404 Not Found` - 文章不存在

---

### 批量操作文章

对一个或多个文章执行审核/拒绝/删除操作。

```http
POST /api/admin/posts/action
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "action": "REJECT",
  "postIds": [1, 2, 3],
  "formTitle": "审核拒绝通知",
  "reason": "文章内容违规，包含广告信息",
  "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"广告营销\"},{\"fieldName\":\"涉及规则\",\"fieldValue\":\"社区规范3.2\"}]"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：APPROVE/REJECT/DELETE |
| postIds | array | 是 | 文章ID列表（支持批量操作） |
| formTitle | string | 拒绝/删除时必填 | 表单标题（作为提示字段保存在后台） |
| reason | string | 拒绝/删除时必填 | 拒绝/删除原因 |
| extraFields | string | 否 | 扩展字段JSON数组，格式：`[{"fieldName":"字段名","fieldValue":"字段值"}]` |

**操作说明:**
- **APPROVE**: 审核通过，将文章状态改为 PUBLISHED，并向作者发送通知
- **REJECT**: 审核拒绝，将文章状态改为 REJECTED，保存拒绝表单并向作者发送通知
- **DELETE**: 删除文章，保存删除表单并向作者发送通知

**成功响应:** `200 OK`
```json
{
  "success": true,
  "message": "成功拒绝 3 篇文章",
  "processedCount": 3,
  "failedIds": []
}
```

**错误响应:**
- `400 Bad Request` - 参数验证失败

---

### 获取表单详情

获取拒绝/删除表单的详细信息。

```http
GET /api/admin/forms/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 5,
  "formType": "REJECTION",
  "title": "审核拒绝通知",
  "reason": "文章内容违规",
  "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"广告营销\"}]",
  "postId": 1,
  "postTitle": "文章标题",
  "targetUserId": 2,
  "adminId": 1,
  "adminUsername": "admin",
  "createdAt": "2024-01-15T12:00:00",
  "isSent": true,
  "sentAt": "2024-01-15T12:00:00"
}
```

---

### 获取文章的表单历史

获取指定文章的所有拒绝/删除表单记录。

```http
GET /api/admin/posts/{postId}/forms
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
[
  {
    "id": 5,
    "formType": "REJECTION",
    "title": "审核拒绝通知",
    "reason": "文章内容违规",
    "createdAt": "2024-01-15T12:00:00"
  }
]
```

---

## 后续规划

管理后台 API 将陆续增加以下功能：

### 评论管理
- `GET /api/admin/comments` - 获取评论列表
- `PUT /api/admin/comments/{id}/status` - 审核评论
- `DELETE /api/admin/comments/{id}` - 删除评论

### 系统设置
- `GET /api/admin/settings` - 获取系统设置
- `PUT /api/admin/settings` - 更新系统设置
