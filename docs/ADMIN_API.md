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

> 说明：无论是首次发布还是修改待审，管理员执行 **REJECT** 后都会将文章置为 `REJECTED`，该文章不再对外展示且不贡献热度统计。

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

## 评论管理

### 评论状态说明

| 状态 | 说明 |
|------|------|
| PENDING | 待审核 - 新发布的评论或修改后的评论 |
| APPROVED | 已通过 - 审核通过的评论（默认状态） |

### 获取评论列表

获取评论列表，支持分页和多条件搜索过滤。

```http
GET /api/admin/comments
Authorization: Bearer {admin-token}
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| content | string | 否 | 评论内容搜索（模糊匹配） |
| author | string | 否 | 作者用户名或昵称搜索（模糊匹配） |
| postTitle | string | 否 | 文章标题搜索（模糊匹配） |
| status | string | 否 | 状态过滤：PENDING/APPROVED |
| startDate | string | 否 | 创建开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 创建结束日期（yyyy-MM-dd） |
| includeReplies | boolean | 否 | 是否包含子评论（默认true） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "content": "这是一条评论内容",
      "contentPreview": "这是一条评论内容...",
      "authorId": 2,
      "authorUsername": "user1",
      "authorNickname": "用户1",
      "authorAvatarUrl": "/uploads/2/avatars/avatar.jpg",
      "authorEnabled": true,
      "postId": 1,
      "postTitle": "文章标题",
      "parentId": null,
      "parentContentPreview": null,
      "replyToUserId": null,
      "replyToUsername": null,
      "level": 0,
      "status": "APPROVED",
      "likeCount": 5,
      "replyCount": 3,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": null
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

---

### 获取评论详情

获取指定评论的详细信息。

```http
GET /api/admin/comments/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "content": "完整评论内容...",
  "contentPreview": "完整评论内容...",
  "authorId": 2,
  "authorUsername": "user1",
  "authorNickname": "用户1",
  "postId": 1,
  "postTitle": "文章标题",
  "parentId": null,
  "level": 0,
  "status": "PENDING",
  "likeCount": 5,
  "replyCount": 3,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

**错误响应:**
- `404 Not Found` - 评论不存在

---

### 批量操作评论

对一个或多个评论执行审核通过/删除操作。

```http
POST /api/admin/comments/action
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "action": "DELETE",
  "commentIds": [1, 2, 3],
  "formTitle": "评论删除通知",
  "reason": "评论内容违规，包含不当言论",
  "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"不当言论\"}]"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：APPROVE/DELETE |
| commentIds | array | 是 | 评论ID列表（支持批量操作） |
| formTitle | string | 删除时可选 | 表单标题（作为提示字段保存在后台） |
| reason | string | 删除时必填 | 删除原因 |
| extraFields | string | 否 | 扩展字段JSON数组，格式：`[{"fieldName":"字段名","fieldValue":"字段值"}]` |

**操作说明:**
- **APPROVE**: 审核通过，将评论状态改为 APPROVED
- **DELETE**: 删除评论（级联删除子评论），保存删除表单并向作者发送通知

**成功响应:** `200 OK`
```json
{
  "successCount": 3,
  "failureCount": 0,
  "successIds": [1, 2, 3],
  "failures": []
}
```

**错误响应:**
- `400 Bad Request` - 参数验证失败（如删除操作未填写理由）

---

### 评论审核机制

1. **新评论**: 默认状态为 APPROVED（已通过），免审核直接发布
2. **修改评论**: 用户修改评论后，状态自动重置为 PENDING（待审核）
3. **删除通知**: 管理员删除评论时会向作者发送通知，包含删除理由

---

## 标签管理

### 获取标签列表

获取标签列表，支持分页和多条件搜索过滤。按标签关联的文章数量（热度）降序排列。

```http
GET /api/admin/tags
Authorization: Bearer {admin-token}
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| name | string | 否 | 标签名称搜索（模糊匹配） |
| createdBy | string | 否 | 创建者用户名搜索（模糊匹配） |
| startDate | string | 否 | 创建开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 创建结束日期（yyyy-MM-dd） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Java",
      "description": "Java编程语言",
      "color": "#FF5733",
      "icon": "fa-brands fa-java",
      "sortOrder": 0,
      "postCount": 15,
      "createdByUsername": "admin",
      "createdByNickname": "管理员",
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

---

### 获取标签详情

获取指定标签的详细信息。

```http
GET /api/admin/tags/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "Java",
  "description": "Java编程语言",
  "color": "#FF5733",
  "icon": "fa-brands fa-java",
  "sortOrder": 0,
  "postCount": 15,
  "posts": [
    { "postId": 1, "postTitle": "Spring Boot入门" },
    { "postId": 3, "postTitle": "JPA实践指南" }
  ],
  "createdByUsername": "admin",
  "createdByNickname": "管理员",
  "createdAt": "2024-01-01T00:00:00"
}
```

> 注：`posts` 字段包含该标签关联的所有文章列表（ID和标题），列表接口也会返回此字段（当标签关联了文章时）。

**错误响应:**
- `404 Not Found` - 标签不存在

---

### 创建标签

创建新标签。

```http
POST /api/admin/tags
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "name": "Spring Boot",
  "description": "Spring Boot框架",
  "color": "#6DB33F",
  "icon": "fa-solid fa-leaf",
  "sortOrder": 0
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 标签名称（唯一） |
| description | string | 否 | 标签描述 |
| color | string | 否 | 颜色值（如 #FF5733） |
| icon | string | 否 | 图标类名（Font Awesome 6，如 `fa-brands fa-java`、`fa-solid fa-code`） |
| sortOrder | integer | 否 | 排序值（越小越靠前） |

**成功响应:** `201 Created`
```json
{
  "id": 10,
  "name": "Spring Boot",
  "description": "Spring Boot框架",
  "color": "#6DB33F",
  "icon": "fa-solid fa-leaf",
  "sortOrder": 0,
  "postCount": 0,
  "createdByUsername": "admin",
  "createdByNickname": "管理员",
  "createdAt": "2024-01-15T12:00:00"
}
```

**错误响应:**
- `400 Bad Request` - 标签名称已存在或参数验证失败

---

### 更新标签

更新已有标签信息。

```http
PUT /api/admin/tags/{id}
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:** 同创建标签

**成功响应:** `200 OK`（响应格式同标签详情）

**错误响应:**
- `404 Not Found` - 标签不存在
- `400 Bad Request` - 标签名称已存在

---

### 批量操作标签

对一个或多个标签执行软删除/硬删除操作。

```http
POST /api/admin/tags/action
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "action": "SOFT_DELETE",
  "tagIds": [1, 2, 3],
  "postIds": [1, 5],
  "formTitle": "标签关联移除通知",
  "reason": "标签与文章内容不相关",
  "extraFields": "[{\"fieldName\":\"建议\",\"fieldValue\":\"请使用更准确的标签\"}]"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：SOFT_DELETE/HARD_DELETE |
| tagIds | array | 是 | 标签ID列表（支持批量操作） |
| postIds | array | 否 | 指定要解除关联的文章ID列表（仅SOFT_DELETE有效，不提供则解除所有关联） |
| formTitle | string | 否 | 表单标题（默认根据操作类型自动生成） |
| reason | string | 是 | 操作原因 |
| extraFields | string | 否 | 扩展字段JSON数组，格式：`[{"fieldName":"字段名","fieldValue":"字段值"}]` |

**操作说明:**
- **SOFT_DELETE**: 软删除，移除标签与文章的关联关系（post_tags表），标签实体保留。可通过 `postIds` 指定要解除关联的特定文章，不提供则解除所有文章关联。保存删除表单（含受影响的文章列表）并向标签创建者发送 `TAG_REMOVED` 通知
- **HARD_DELETE**: 硬删除，移除关联关系并删除标签实体。保存删除表单并向标签创建者发送 `TAG_DELETED` 通知

**成功响应:** `200 OK`
```json
{
  "successCount": 3,
  "failureCount": 0,
  "successIds": [1, 2, 3],
  "failures": []
}
```

**错误响应:**
- `400 Bad Request` - 参数验证失败（如未填写理由）

---

## 分类管理

### 获取分类列表

获取分类列表，支持分页和多条件搜索过滤。按分类关联的文章数量降序排列。

```http
GET /api/admin/categories
Authorization: Bearer {admin-token}
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| name | string | 否 | 分类名称搜索（模糊匹配） |
| createdBy | string | 否 | 创建者用户名搜索（模糊匹配） |
| startDate | string | 否 | 创建开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 创建结束日期（yyyy-MM-dd） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "技术",
      "description": "技术类文章",
      "color": "#1890ff",
      "icon": "fa-solid fa-code",
      "sortOrder": 0,
      "postCount": 15,
      "createdById": 1,
      "createdByUsername": "admin",
      "createdByNickname": "管理员",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": null
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### 获取分类详情

获取指定分类的详细信息，包含关联的文章列表。

```http
GET /api/admin/categories/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "name": "技术",
  "description": "技术类文章",
  "color": "#1890ff",
  "icon": "fa-solid fa-code",
  "sortOrder": 0,
  "postCount": 2,
  "posts": [
    { "postId": 1, "postTitle": "Spring Boot入门" },
    { "postId": 5, "postTitle": "Vue3实践指南" }
  ],
  "createdById": 1,
  "createdByUsername": "admin",
  "createdByNickname": "管理员",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": null
}
```

**错误响应:**
- `404 Not Found` - 分类不存在

---

### 创建分类

创建新分类。

```http
POST /api/admin/categories
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "name": "技术",
  "description": "技术类文章",
  "color": "#1890ff",
  "icon": "fa-solid fa-code",
  "sortOrder": 0
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 分类名称（唯一，1-50字符，支持中英文、数字、下划线、连字符） |
| description | string | 否 | 分类描述（最多200字符） |
| color | string | 否 | 颜色值（格式 #RRGGBB，如 #FF5733） |
| icon | string | 否 | 图标类名（最多50字符） |
| sortOrder | integer | 否 | 排序值（≥0，越小越靠前） |

**成功响应:** `201 Created`

**错误响应:**
- `400 Bad Request` - 分类名称已存在或参数验证失败

---

### 更新分类

更新已有分类信息。

```http
PUT /api/admin/categories/{id}
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:** 同创建分类

**成功响应:** `200 OK`（响应格式同分类详情）

**错误响应:**
- `404 Not Found` - 分类不存在
- `400 Bad Request` - 分类名称已存在

---

### 按标题搜索文章

按标题关键词搜索文章，用于分类管理时将文章归入分类。返回最多20条匹配结果。

```http
GET /api/admin/categories/search-posts?title={keyword}
Authorization: Bearer {admin-token}
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 文章标题关键词（模糊匹配） |

**成功响应:** `200 OK`
```json
[
  { "postId": 1, "postTitle": "Spring Boot入门" },
  { "postId": 5, "postTitle": "Spring Cloud实战" }
]
```

---

### 将文章归入分类

将指定文章归入指定分类。文章只能属于一个分类（1:N关系），如果文章已有分类会被覆盖。

```http
POST /api/admin/categories/{id}/posts/{postId}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`（返回更新后的分类详情，含最新文章列表）

**错误响应:**
- `404 Not Found` - 分类或文章不存在

---

### 将文章从分类移除

将指定文章从分类中移除（将文章的 `category_id` 设为 NULL）。

```http
DELETE /api/admin/categories/{id}/posts/{postId}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`（返回更新后的分类详情）

**错误响应:**
- `404 Not Found` - 分类或文章不存在
- `400 Bad Request` - 文章不属于该分类

---

### 批量删除分类

对一个或多个分类执行删除操作。删除分类时会将所有使用该分类的文章的 `category_id` 设为 NULL，并向分类创建者发送通知。

```http
POST /api/admin/categories/action
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "action": "DELETE",
  "categoryIds": [1, 2, 3],
  "reason": "分类合并整理",
  "extraFields": "[{\"fieldName\":\"备注\",\"fieldValue\":\"已迁移至新分类\"}]"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：DELETE |
| categoryIds | array | 是 | 分类ID列表（支持批量操作） |
| reason | string | 是 | 删除原因 |
| extraFields | string | 否 | 扩展字段JSON数组，格式：`[{"fieldName":"字段名","fieldValue":"字段值"}]` |

**操作说明:**
- **DELETE**: 删除分类，清除文章关联（`category_id` 设为 NULL），保存删除表单并向分类创建者发送 `CATEGORY_DELETED` 通知

**成功响应:** `200 OK`
```json
{
  "successCount": 3,
  "failureCount": 0,
  "successIds": [1, 2, 3],
  "failures": []
}
```

**错误响应:**
- `400 Bad Request` - 参数验证失败（如未填写理由）

---

## 后续规划

管理后台 API 将陆续增加以下功能：

### 系统设置
- `GET /api/admin/settings` - 获取系统设置
- `PUT /api/admin/settings` - 更新系统设置
