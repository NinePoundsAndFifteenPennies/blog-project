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

获取管理后台仪表盘的完整统计数据，包括核心指标、趋势图表、热门内容、标签/分类热力图等。

```http
GET /api/admin/dashboard
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "admin": "admin",
  "message": "欢迎进入管理后台",

  "totalUsers": 150,
  "todayNewUsers": 3,
  "totalPosts": 420,
  "todayNewPosts": 8,
  "totalComments": 1200,
  "todayNewComments": 25,
  "totalViews": 56000,
  "todayViews": 340,

  "userTrend": [
    { "date": "01-11", "count": 2 },
    { "date": "01-12", "count": 5 }
  ],
  "postTrend": [
    { "date": "01-11", "count": 3 },
    { "date": "01-12", "count": 1 }
  ],
  "commentTrend": [
    { "date": "01-11", "count": 10 },
    { "date": "01-12", "count": 8 }
  ],
  "viewTrend": [
    { "date": "01-11", "count": 200 },
    { "date": "01-12", "count": 180 }
  ],

  "hotPosts": [
    {
      "id": 1,
      "title": "Vue 3 入门指南",
      "author": "张三",
      "viewCount": 1200,
      "likeCount": 45,
      "commentCount": 18,
      "heatScore": 32.5
    }
  ],

  "publishedPosts": 350,
  "draftPosts": 40,
  "pendingPosts": 20,
  "rejectedPosts": 10,

  "tagStats": [
    { "id": 1, "name": "Vue", "postCount": 24, "color": "#1890ff" },
    { "id": 2, "name": "Spring", "postCount": 18, "color": "#52c41a" }
  ],
  "categoryStats": [
    { "id": 1, "name": "前端", "postCount": 45, "color": "#2f54eb" },
    { "id": 2, "name": "后端", "postCount": 32, "color": "#1890ff" }
  ],

  "totalTags": 25,
  "totalCategories": 8,
  "enabledUsers": 145,
  "disabledUsers": 5,

  "recentActivities": [
    { "type": "user", "description": "3 位新用户注册", "time": "02-10", "icon": "👤" },
    { "type": "post", "description": "8 篇新文章发布", "time": "02-10", "icon": "📝" },
    { "type": "comment", "description": "25 条新评论", "time": "02-10", "icon": "💬" },
    { "type": "pending", "description": "20 篇文章待审核", "time": "待处理", "icon": "⏳" },
    { "type": "view", "description": "昨日 300 次浏览", "time": "02-09", "icon": "👁" }
  ],

  "contentRadar": {
    "avgViewsPerPost": 65.0,
    "avgCommentsPerPost": 42.0,
    "avgLikesPerPost": 28.0,
    "publishRate": 83.3,
    "userEngagement": 55.0,
    "contentFreshness": 30.0
  }
}
```

**响应字段说明:**

| 字段 | 说明 |
|------|------|
| `totalUsers` / `todayNewUsers` | 用户总数 / 今日新增 |
| `totalPosts` / `todayNewPosts` | 文章总数 / 今日新增 |
| `totalComments` / `todayNewComments` | 评论总数 / 今日新增 |
| `totalViews` / `todayViews` | 总浏览量 / 今日浏览 |
| `userTrend` / `postTrend` / `commentTrend` / `viewTrend` | 最近30天每日趋势数据 |
| `hotPosts` | 热门文章 TOP10（按热度公式排序） |
| `hotPosts[].heatScore` | 热度值，公式：`(viewCount*0.1 + likeCount*5 + commentCount*10) / POW(hours+2, 1.2)` |
| `publishedPosts` / `draftPosts` / `pendingPosts` / `rejectedPosts` | 各状态文章数量 |
| `tagStats` / `categoryStats` | 标签/分类的文章数统计（热力图数据） |
| `totalTags` / `totalCategories` | 标签/分类总数 |
| `enabledUsers` / `disabledUsers` | 活跃/禁用用户数 |
| `recentActivities` | 最近动态列表（今日新增、待处理、昨日浏览） |
| `contentRadar` | 内容质量雷达图数据（0-100分，6个维度） |

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

更新用户的启用状态。禁用后用户将无法登录，其个人主页和文章将对外隐藏。仅可操作非管理员用户。操作会在 `admin_forms` 表中记录日志（不会向用户发送通知）。

```http
PUT /api/admin/users/{id}/status
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "enabled": false,
  "formTitle": "用户禁用记录",
  "reason": "违反社区规范",
  "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"恶意刷评论\"}]"
}
```

**参数说明:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| enabled | boolean | 是 | 是否启用 |
| formTitle | string | 否 | 表单标题（不超过100字符），默认为"用户启用记录"或"用户禁用记录" |
| reason | string | 是 | 操作理由 |
| extraFields | string | 否 | 扩展字段（JSON格式），格式: `[{"fieldName":"...", "fieldValue":"..."}]` |

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
- `400 Bad Request` - 不能修改管理员的状态
- `404 Not Found` - 用户不存在

> 注意：管理员角色的分配只能通过数据库直接操作（SQL），不提供 API 接口。

---

## 封禁用户处理

当用户被禁用（`enabled = false`）时：

1. **强制下线**: 被封禁用户的JWT token将被拒绝，返回 `403 Forbidden`
2. **隐藏个人主页**: 访问被封禁用户的个人主页将返回 `404 Not Found`
3. **隐藏文章**: 
   - 被封禁用户的文章列表将返回空
   - 直接访问其文章详情将返回 `404 Not Found`
4. **评论**: 被封禁用户已发布的评论仍然可见（可根据需要扩展隐藏功能）
5. **日志记录**: 操作会在 `admin_forms` 表中存储记录（含表单标题、理由、扩展字段），但不会向被操作用户发送通知

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

对一个或多个分类执行删除操作。删除分类时会将所有使用该分类的文章的 `category_id` 设为 NULL，并在 `admin_forms` 表中记录操作日志（不向分类创建者发送通知）。

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
- **DELETE**: 删除分类，清除文章关联（`category_id` 设为 NULL），保存删除表单记录（不发送通知）

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

## 操作日志

操作日志是只读的，由管理员执行管理操作时自动生成，不能通过接口创建、修改或删除。

### 操作类型说明

| 操作类型 | 说明 |
|----------|------|
| POST_APPROVE | 文章审核通过 |
| POST_REJECT | 文章审核拒绝 |
| POST_DELETE | 文章删除 |
| COMMENT_APPROVE | 评论审核通过 |
| COMMENT_DELETE | 评论删除 |
| TAG_CREATE | 标签创建 |
| TAG_UPDATE | 标签更新 |
| TAG_SOFT_DELETE | 标签软删除（移除关联） |
| TAG_HARD_DELETE | 标签硬删除 |
| CATEGORY_CREATE | 分类创建 |
| CATEGORY_UPDATE | 分类更新 |
| CATEGORY_DELETE | 分类删除 |
| CATEGORY_ASSIGN_POST | 分类添加文章 |
| CATEGORY_REMOVE_POST | 分类移除文章 |
| USER_STATUS_CHANGE | 用户状态变更 |
| REPORT_APPROVE | 举报通过 |
| REPORT_REJECT | 举报驳回 |

### 获取操作日志列表

获取操作日志列表，支持分页和多条件搜索。

```http
GET /api/admin/logs
Authorization: Bearer {admin-token}
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| operationType | string | 否 | 操作类型过滤（见操作类型说明） |
| adminUsername | string | 否 | 管理员用户名搜索（模糊匹配） |
| title | string | 否 | 操作标题搜索（模糊匹配） |
| startDate | string | 否 | 开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 结束日期（yyyy-MM-dd） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "operationType": "POST_REJECT",
      "title": "审核拒绝通知",
      "description": "文章内容违规",
      "adminId": 1,
      "adminUsername": "admin",
      "adminNickname": "管理员",
      "postId": 5,
      "postTitle": "文章标题",
      "commentId": null,
      "commentContentPreview": null,
      "tagId": null,
      "tagName": null,
      "categoryId": null,
      "categoryName": null,
      "targetUserId": null,
      "targetUsername": null,
      "formId": 3,
      "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"广告营销\"}]",
      "createdAt": "2024-01-15T12:00:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

**日志标题说明:**
- 有关联 `admin_forms` 表单且表单标题非空时，日志标题使用表单标题
- 否则使用默认格式，如"审核通过文章 #5"、"创建标签: Java"

---

### 获取操作日志详情

获取单条操作日志的详细信息。

```http
GET /api/admin/logs/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "operationType": "POST_REJECT",
  "title": "审核拒绝通知",
  "description": "文章内容违规",
  "adminId": 1,
  "adminUsername": "admin",
  "adminNickname": "管理员",
  "postId": 5,
  "postTitle": "文章标题",
  "commentId": null,
  "commentContentPreview": null,
  "tagId": null,
  "tagName": null,
  "categoryId": null,
  "categoryName": null,
  "targetUserId": null,
  "targetUsername": null,
  "formId": 3,
  "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"广告营销\"}]",
  "createdAt": "2024-01-15T12:00:00"
}
```

**响应字段说明:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 日志ID |
| operationType | string | 操作类型（见操作类型说明） |
| title | string | 操作标题 |
| description | string | 操作描述/理由（可为空） |
| adminId | long | 执行操作的管理员ID |
| adminUsername | string | 管理员用户名 |
| adminNickname | string | 管理员昵称 |
| postId | long | 关联文章ID（可为空） |
| postTitle | string | 关联文章标题（可为空） |
| commentId | long | 关联评论ID（可为空） |
| commentContentPreview | string | 关联评论内容预览（可为空） |
| tagId | long | 关联标签ID（可为空） |
| tagName | string | 关联标签名称（可为空） |
| categoryId | long | 关联分类ID（可为空） |
| categoryName | string | 关联分类名称（可为空） |
| targetUserId | long | 被操作用户ID（可为空） |
| targetUsername | string | 被操作用户名（可为空） |
| formId | long | 关联管理表单ID（可为空） |
| extraFields | string | 扩展字段JSON数组（可为空），格式：`[{"fieldName":"字段名","fieldValue":"字段值"}]` |
| createdAt | string | 创建时间 |

**错误响应:**
- `404 Not Found` - 日志不存在

---

## 举报管理

管理员可以查看、搜索和处理用户提交的举报。处理举报时需要填写表单（标题+理由），系统会自动记录操作日志并发送通知。

### 举报状态说明

| 状态 | 说明 |
|------|------|
| PENDING | 待处理 |
| APPROVED | 举报通过（已确认违规） |
| REJECTED | 举报驳回（未确认违规） |

### 举报目标类型

| 类型 | 说明 |
|------|------|
| POST | 文章 |
| COMMENT | 评论 |

### 获取举报列表

获取举报列表，支持分页和多条件搜索。

```http
GET /api/admin/reports
Authorization: Bearer {admin-token}
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（从0开始），默认0 |
| size | int | 否 | 每页数量，默认10 |
| status | string | 否 | 举报状态过滤（PENDING/APPROVED/REJECTED） |
| targetType | string | 否 | 举报目标类型过滤（POST/COMMENT） |
| reporterUsername | string | 否 | 举报者用户名搜索（模糊匹配） |
| reportedUsername | string | 否 | 被举报者用户名搜索（模糊匹配） |
| startDate | string | 否 | 开始日期（yyyy-MM-dd） |
| endDate | string | 否 | 结束日期（yyyy-MM-dd） |

**成功响应:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "targetType": "POST",
      "targetId": 5,
      "targetContentPreview": "文章标题或内容前200字...",
      "reason": "该文章包含不实信息",
      "status": "PENDING",
      "reporterId": 3,
      "reporterUsername": "user1",
      "reporterNickname": "用户一",
      "reporterAvatarUrl": "/api/files/avatars/3/xxx.jpg",
      "reportedUserId": 7,
      "reportedUsername": "user2",
      "reportedNickname": "用户二",
      "reportedAvatarUrl": "/api/files/avatars/7/xxx.jpg",
      "adminFormId": null,
      "createdAt": "2024-01-15T12:00:00",
      "processedAt": null
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### 获取举报详情

获取单条举报的详细信息。

```http
GET /api/admin/reports/{id}
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
{
  "id": 1,
  "targetType": "POST",
  "targetId": 5,
  "targetContentPreview": "文章标题或内容前200字...",
  "reason": "该文章包含不实信息",
  "status": "APPROVED",
  "reporterId": 3,
  "reporterUsername": "user1",
  "reporterNickname": "用户一",
  "reporterAvatarUrl": "/api/files/avatars/3/xxx.jpg",
  "reportedUserId": 7,
  "reportedUsername": "user2",
  "reportedNickname": "用户二",
  "reportedAvatarUrl": "/api/files/avatars/7/xxx.jpg",
  "adminFormId": 12,
  "createdAt": "2024-01-15T12:00:00",
  "processedAt": "2024-01-15T14:00:00"
}
```

**响应字段说明:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 举报ID |
| targetType | string | 举报目标类型（POST/COMMENT） |
| targetId | long | 举报目标ID |
| targetContentPreview | string | 目标内容预览（文章标题或评论内容前200字） |
| reason | string | 举报原因 |
| status | string | 举报状态（PENDING/APPROVED/REJECTED） |
| reporterId | long | 举报者ID |
| reporterUsername | string | 举报者用户名 |
| reporterNickname | string | 举报者昵称 |
| reporterAvatarUrl | string | 举报者头像URL |
| reportedUserId | long | 被举报者ID |
| reportedUsername | string | 被举报者用户名 |
| reportedNickname | string | 被举报者昵称 |
| reportedAvatarUrl | string | 被举报者头像URL |
| adminFormId | long | 关联管理表单ID（处理后才有值） |
| createdAt | string | 举报提交时间 |
| processedAt | string | 处理时间（处理后才有值） |

**错误响应:**
- `404 Not Found` - 举报不存在

---

### 处理举报（通过/驳回）

管理员处理举报，需要填写表单标题和理由。处理后系统会自动发送通知并记录操作日志。

```http
POST /api/admin/reports/action
Authorization: Bearer {admin-token}
Content-Type: application/json
```

**请求体:**
```json
{
  "action": "APPROVE",
  "reportId": 1,
  "formTitle": "举报处理通知",
  "reason": "经核实，该内容违反社区规范",
  "extraFields": "[{\"fieldName\":\"违规类型\",\"fieldValue\":\"虚假信息\"}]"
}
```

**参数说明:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：`APPROVE`（通过）或 `REJECT`（驳回） |
| reportId | long | 是 | 举报ID |
| formTitle | string | 是 | 表单标题（最多100个字符） |
| reason | string | 是 | 处理理由 |
| extraFields | string | 否 | 扩展字段，JSON数组格式：`[{"fieldName":"字段名","fieldValue":"字段值"}]` |

**成功响应:** `200 OK`
```json
"举报已通过处理"
```
或
```json
"举报已驳回处理"
```

**处理流程:**

| 操作 | 通知对象 | 通知类型 | 说明 |
|------|----------|----------|------|
| APPROVE（通过） | 被举报者 | REPORTED_CONTENT | 以"系统管理员"名义通知被举报者内容违规 |
| APPROVE（通过） | 举报者 | REPORT_RESULT | 通知举报者举报已通过 |
| REJECT（驳回） | 举报者 | REPORT_RESULT | 通知举报者举报未通过 |

**错误响应:**
- `400 Bad Request` - 参数验证失败（操作类型为空、举报ID为空、表单标题为空、理由为空）
- `404 Not Found` - 举报不存在

---

### 获取待处理举报数量

获取当前待处理（PENDING状态）的举报总数，用于仪表盘显示。

```http
GET /api/admin/reports/pending/count
Authorization: Bearer {admin-token}
```

**成功响应:** `200 OK`
```json
5
```

---

## 后续规划

管理后台 API 将陆续增加以下功能：

### 系统设置
- `GET /api/admin/settings` - 获取系统设置
- `PUT /api/admin/settings` - 更新系统设置

### 媒体管理
- `GET /api/admin/media` - 获取媒体文件列表
- `DELETE /api/admin/media/{id}` - 删除媒体文件
