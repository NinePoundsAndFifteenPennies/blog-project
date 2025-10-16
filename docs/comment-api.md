# 评论功能 API 文档

## 概述
本文档描述了博客系统的评论功能接口。评论功能允许登录用户对已发布的文章进行评论，并支持评论的增删改查操作。

## 基本说明
- 所有涉及创建、更新、删除的接口都需要用户登录（JWT认证）
- 查看评论列表的接口不需要登录
- 评论内容长度限制：1-3000字符
- 评论支持纯文本和Markdown格式（前端处理）

## API 端点

### 1. 创建评论
在指定文章下创建一条评论。

**端点：** `POST /api/posts/{postId}/comments`

**权限：** 需要登录

**路径参数：**
- `postId` (Long): 文章ID

**请求体：**
```json
{
  "content": "这是评论内容"
}
```

**请求体字段说明：**
- `content` (String, 必填): 评论内容，长度1-3000字符

**响应示例（201 Created）：**
```json
{
  "id": 1,
  "content": "这是评论内容",
  "postId": 10,
  "postTitle": "文章标题",
  "authorUsername": "用户名",
  "authorAvatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-10-16T15:30:00",
  "updatedAt": null
}
```

**错误响应：**
- `404 Not Found`: 文章不存在
- `403 Forbidden`: 尝试评论草稿文章
- `401 Unauthorized`: 未登录
- `400 Bad Request`: 评论内容不符合要求

---

### 2. 更新评论
更新自己创建的评论内容。

**端点：** `PUT /api/comments/{commentId}`

**权限：** 需要登录，且只能更新自己的评论

**路径参数：**
- `commentId` (Long): 评论ID

**请求体：**
```json
{
  "content": "更新后的评论内容"
}
```

**响应示例（200 OK）：**
```json
{
  "id": 1,
  "content": "更新后的评论内容",
  "postId": 10,
  "postTitle": "文章标题",
  "authorUsername": "用户名",
  "authorAvatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-10-16T15:30:00",
  "updatedAt": "2025-10-16T16:00:00"
}
```

**错误响应：**
- `404 Not Found`: 评论不存在
- `403 Forbidden`: 无权限编辑此评论
- `401 Unauthorized`: 未登录

---

### 3. 删除评论
删除一条评论。评论作者和文章作者都可以删除评论。

**端点：** `DELETE /api/comments/{commentId}`

**权限：** 需要登录，且满足以下条件之一：
- 是评论的作者
- 是评论所在文章的作者

**路径参数：**
- `commentId` (Long): 评论ID

**响应示例（200 OK）：**
```json
"评论删除成功"
```

**错误响应：**
- `404 Not Found`: 评论不存在
- `403 Forbidden`: 无权限删除此评论
- `401 Unauthorized`: 未登录

---

### 4. 获取文章的所有评论
获取指定文章的所有评论，支持分页。

**端点：** `GET /api/posts/{postId}/comments`

**权限：** 无需登录

**路径参数：**
- `postId` (Long): 文章ID

**查询参数（分页）：**
- `page` (int, 可选): 页码，从0开始，默认0
- `size` (int, 可选): 每页数量，默认20
- `sort` (String, 可选): 排序方式，默认按创建时间升序（最早的在前）

**示例请求：**
```
GET /api/posts/10/comments?page=0&size=20
```

**响应示例（200 OK）：**
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
      "updatedAt": null
    },
    {
      "id": 2,
      "content": "第二条评论",
      "postId": 10,
      "postTitle": "文章标题",
      "authorUsername": "user2",
      "authorAvatarUrl": "https://example.com/avatar2.jpg",
      "createdAt": "2025-10-16T16:00:00",
      "updatedAt": "2025-10-16T17:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "number": 0,
  "size": 20,
  "numberOfElements": 2,
  "empty": false
}
```

**错误响应：**
- `404 Not Found`: 文章不存在

---

### 5. 获取当前用户的所有评论
获取当前登录用户发表的所有评论，支持分页。

**端点：** `GET /api/comments/my`

**权限：** 需要登录

**查询参数（分页）：**
- `page` (int, 可选): 页码，从0开始，默认0
- `size` (int, 可选): 每页数量，默认20

**示例请求：**
```
GET /api/comments/my?page=0&size=10
```

**响应示例（200 OK）：**
```json
{
  "content": [
    {
      "id": 1,
      "content": "我的第一条评论",
      "postId": 10,
      "postTitle": "文章标题A",
      "authorUsername": "currentUser",
      "authorAvatarUrl": "https://example.com/my-avatar.jpg",
      "createdAt": "2025-10-16T15:30:00",
      "updatedAt": null
    },
    {
      "id": 5,
      "content": "我的第二条评论",
      "postId": 12,
      "postTitle": "文章标题B",
      "authorUsername": "currentUser",
      "authorAvatarUrl": "https://example.com/my-avatar.jpg",
      "createdAt": "2025-10-16T16:30:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "empty": false
}
```

**错误响应：**
- `401 Unauthorized`: 未登录

---

## 文章接口更新

### 文章响应中新增评论数字段
所有返回文章信息的接口（`GET /api/posts`, `GET /api/posts/{id}`, `GET /api/posts/my` 等）现在都会在响应中包含 `commentCount` 字段。

**示例响应：**
```json
{
  "id": 10,
  "title": "文章标题",
  "content": "文章内容",
  "authorUsername": "作者名",
  "authorAvatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-10-16T10:00:00",
  "updatedAt": null,
  "publishedAt": "2025-10-16T10:00:00",
  "contentType": "MARKDOWN",
  "draft": false,
  "likeCount": 5,
  "isLiked": true,
  "commentCount": 12
}
```

---

## 权限说明

### 评论创建权限
- 必须登录
- 只能对已发布的文章评论（`draft = false`）
- 不能对草稿文章评论

### 评论编辑权限
- 必须登录
- 只能编辑自己的评论

### 评论删除权限
- 必须登录
- 评论作者可以删除自己的评论
- 文章作者可以删除自己文章下的任何评论

### 评论查看权限
- 获取文章评论列表：无需登录
- 获取自己的评论列表：需要登录

---

## 级联删除说明

当文章被删除时，该文章下的所有评论将自动被删除。这是在服务层实现的级联删除，确保数据一致性。

---

## 数据模型

### Comment 实体
```
comments 表：
- id (Long): 主键，自增
- content (TEXT): 评论内容，1-3000字符
- user_id (Long): 评论作者ID，外键关联 users 表
- post_id (Long): 所属文章ID，外键关联 posts 表
- created_at (TIMESTAMP): 创建时间
- updated_at (TIMESTAMP): 更新时间（可为null）
```

---

## 注意事项

1. **评论内容验证**：评论内容长度限制在1-3000字符之间，后端会进行验证。

2. **时间戳**：所有时间字段使用 ISO 8601 格式（`yyyy-MM-dd'T'HH:mm:ss`）。

3. **分页默认值**：如果不提供分页参数，默认 `page=0, size=20`。

4. **排序**：评论列表默认按创建时间升序排列（最早的评论在前），后续可根据需求调整。

5. **编辑标记**：当评论被编辑后，`updatedAt` 字段会自动更新为编辑时间，前端可以据此显示"已编辑"标记。

6. **草稿限制**：草稿文章不支持评论功能，尝试对草稿文章评论会返回 403 错误。

7. **级联删除**：删除文章时会自动删除该文章的所有评论。

---

## 扩展性设计

当前评论功能设计简洁，为后续扩展预留了空间：

1. **回复功能**：可以在 Comment 模型中添加 `parent_id` 字段支持评论回复。

2. **评论点赞**：可以参考现有的 Like 模型为评论添加点赞功能。

3. **评论排序**：可以支持按点赞数、按最新等多种排序方式。

4. **富文本支持**：可以扩展支持更多格式的评论内容。

5. **评论通知**：可以集成通知系统，当评论被回复或文章被评论时通知相关用户。
