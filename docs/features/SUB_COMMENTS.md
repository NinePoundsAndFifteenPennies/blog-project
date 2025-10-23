# 子评论功能文档

## 概述

本文档详细描述博客系统的**子评论（回复）**和**子评论点赞**功能的完整设计与实现。

本功能遵循以下原则：
1. **前端友好**：API返回字段考虑前端渲染便利性
2. **级联完整**：处理所有删除操作的连锁效应，确保数据一致性
3. **高扩展性**：设计具备良好的扩展性，支持未来功能演进
4. **文档复用**：最大化复用现有文档，减少维护成本

## 目录

- [1. 功能需求](#1-功能需求)
- [2. 数据库设计](#2-数据库设计)
- [3. API设计](#3-api设计)
- [4. 业务逻辑](#4-业务逻辑)
- [5. 级联删除](#5-级联删除)
- [6. 前端展示设计](#6-前端展示设计)
- [7. 架构图示](#7-架构图示)

---

## 1. 功能需求

### 1.1 子评论功能

基于现有评论功能，新增以下能力：

- ✅ 用户可以回复顶层评论（创建子评论）
- ✅ 用户可以回复其他子评论（创建嵌套回复）
- ✅ 子评论显示被回复者信息（@用户名）
- ✅ 子评论支持编辑（仅作者）
- ✅ 子评论支持删除（作者或文章作者）
- ✅ 支持纯文本和Markdown内容
- ✅ 子评论内容长度限制：1-3000字符

### 1.2 子评论点赞功能

- ✅ 用户可以对子评论进行点赞
- ✅ 用户可以取消对子评论的点赞
- ✅ 匿名用户可以查看子评论点赞数量
- ✅ 登录用户可以查看自己的点赞状态
- ✅ 防止重复点赞（数据库唯一约束）

### 1.3 现有基础

参考文档：[COMMENTS.md](./COMMENTS.md)

当前系统已实现：
- ✅ 顶层评论CRUD功能
- ✅ 评论点赞功能
- ✅ 文章点赞功能
- ✅ 完善的级联删除机制（文章→评论→点赞）

---

## 2. 数据库设计

### 2.1 Comment表结构调整

**现有字段**（参考[COMMENTS.md](./COMMENTS.md#comment-表)）：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| content | TEXT | 评论内容，1-3000字符 |
| user_id | BIGINT | 外键，关联 users 表 |
| post_id | BIGINT | 外键，关联 posts 表 |
| created_at | TIMESTAMP | 评论创建时间 |
| updated_at | TIMESTAMP | 评论更新时间（可为空）|

**新增字段**：

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| parent_id | BIGINT | 外键，关联 comments 表（可为空）| NULL |
| reply_to_user_id | BIGINT | 外键，关联 users 表（可为空）| NULL |
| level | INT | 评论层级：0=顶层评论，1=一级回复，2=二级回复... | 0 |

**字段说明**：

1. **parent_id**：
   - 标识该评论的父评论ID
   - NULL表示顶层评论
   - 非NULL表示子评论（回复）
   - 外键约束：`FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE`
   - 索引：`CREATE INDEX idx_comment_parent ON comments(parent_id)`

2. **reply_to_user_id**：
   - 记录被回复的用户ID
   - 用于显示"@用户名"信息
   - 可为NULL（回复评论本身，不@特定用户）
   - 外键约束：`FOREIGN KEY (reply_to_user_id) REFERENCES users(id) ON DELETE SET NULL`
   - 当被@的用户删除账号时，该字段自动设为NULL，保持数据完整性

3. **level**：
   - 评论层级深度
   - 便于前端展示和数据库查询优化
   - 限制最大层级（建议≤3，防止无限嵌套）
   - 非空，默认值0

**JPA/Hibernate 实体定义**:

```java
@Entity
public class Comment {
    // 基础字段...

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 关键: 声明对子评论的一对多关系，并设置级联删除
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // 关键: 声明对点赞的一对多关系，并设置级联删除
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "reply_to_user_id")
    private User replyToUser;

    @Column(name = "level", nullable = false)
    private Integer level = 0;
}
```

### 2.2 Like表结构

**无需修改**！当前设计已支持评论点赞（参考[COMMENTS.md](./COMMENTS.md#like-表支持文章和评论点赞)）。

子评论也是Comment实体，因此现有Like表天然支持子评论点赞，无需任何调整。

### 2.3 数据库迁移SQL

```sql
-- 为comments表添加子评论支持字段
ALTER TABLE comments 
  ADD COLUMN parent_id BIGINT NULL AFTER post_id,
  ADD COLUMN reply_to_user_id BIGINT NULL AFTER parent_id,
  ADD COLUMN level INT NOT NULL DEFAULT 0 AFTER reply_to_user_id;

-- 添加外键约束
ALTER TABLE comments 
  ADD CONSTRAINT fk_comment_parent 
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE;

ALTER TABLE comments 
  ADD CONSTRAINT fk_comment_reply_to_user 
    FOREIGN KEY (reply_to_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 添加索引
CREATE INDEX idx_comment_parent ON comments(parent_id);
CREATE INDEX idx_comment_reply_to_user ON comments(reply_to_user_id);
CREATE INDEX idx_comment_level ON comments(level);
CREATE INDEX idx_comment_post_parent ON comments(post_id, parent_id);
```

---

## 3. API设计

### 3.1 设计原则

1. **向后兼容**：现有API行为保持不变
2. **RESTful风格**：遵循REST规范
3. **前端友好**：一次请求获取完整展示数据
4. **性能优化**：合理分页，减少N+1查询

### 3.2 新增API端点

#### 3.2.1 创建子评论（回复评论）

```http
POST /api/comments/{commentId}/replies
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数**：
- `commentId`：被回复的评论ID（可以是顶层评论或子评论）

**请求体**：
```json
{
  "content": "回复内容",
  "replyToUserId": 123,  // 可选：被@的用户ID
  "replyToUsername": "张三"  // 可选：被@的用户名（当userId不可用时使用）
}
```

**字段说明**：
- `content`（必填）：回复内容，1-3000字符
- `replyToUserId`（可选）：被@的用户ID，优先使用此字段
- `replyToUsername`（可选）：被@的用户名，当replyToUserId不可用时使用。系统会根据用户名查找用户

**成功响应**：`201 Created`
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
  "authorUsername": "李四",
  "authorAvatarUrl": "/uploads/4/avatars/abc.jpg",
  "createdAt": "2025-10-19T10:00:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**错误响应**：
- `400 Bad Request`：内容长度不符合要求、层级超限
- `401 Unauthorized`：未登录
- `403 Forbidden`：回复草稿文章的评论
- `404 Not Found`：父评论不存在或replyToUserId不存在

#### 3.2.2 获取评论的所有回复（子评论列表）

```http
GET /api/comments/{commentId}/replies?page=0&size=20
```

**路径参数**：
- `commentId`：顶层评论ID

**查询参数**：
- `page`（可选）：页码，从0开始，默认0
- `size`（可选）：每页数量，默认20

**成功响应**：`200 OK`
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
      "authorUsername": "用户A",
      "authorAvatarUrl": "/uploads/a/avatars/a.jpg",
      "createdAt": "2025-10-19T10:00:00",
      "updatedAt": null,
      "likeCount": 3,
      "liked": true
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

### 3.3 修改现有API

#### 3.3.1 获取文章的所有评论（修改）

```http
GET /api/posts/{postId}/comments?page=0&size=20
```

**现有行为**：返回文章的所有顶层评论

**修改后行为**：
- **仅返回顶层评论**（parent_id = NULL）
- **额外包含子评论数量**

**响应格式调整**：
```json
{
  "content": [
    {
      "id": 1,
      "content": "顶层评论内容",
      "postId": 10,
      "postTitle": "文章标题",
      "parentId": null,
      "replyToUserId": null,
      "replyToUsername": null,
      "level": 0,
      "replyCount": 5,  // 新增：子评论数量
      "authorUsername": "用户名",
      "authorAvatarUrl": "/uploads/1/avatars/a.jpg",
      "createdAt": "2025-10-19T09:00:00",
      "updatedAt": null,
      "likeCount": 10,
      "liked": true
    }
  ],
  "totalElements": 50,
  "totalPages": 3
}
```

**新增字段说明**：
- `parentId`：父评论ID（顶层评论为null）
- `replyToUserId`：被@用户ID（顶层评论为null）
- `replyToUsername`：被@用户名（顶层评论为null）
- `level`：评论层级（顶层评论为0）
- `replyCount`：该评论的直接和间接子评论总数

#### 3.3.2 获取我的评论（修改）

```http
GET /api/comments/my?page=0&size=10
```

**修改后行为**：
- **包含顶层评论和子评论**
- 区分显示（通过parentId、level判断）

**响应格式**：
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
      "authorUsername": "我",
      "authorAvatarUrl": "/uploads/me.jpg",
      "createdAt": "2025-10-19T09:00:00",
      "updatedAt": null,
      "likeCount": 5,
      "liked": false
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
      "authorUsername": "我",
      "authorAvatarUrl": "/uploads/me.jpg",
      "createdAt": "2025-10-19T10:00:00",
      "updatedAt": null,
      "likeCount": 2,
      "liked": false
    }
  ],
  "totalElements": 25,
  "totalPages": 3
}
```

### 3.4 点赞API（无需修改）

参考[COMMENTS.md - 评论点赞功能](./COMMENTS.md#评论点赞功能)

现有API完全适用于子评论：

```http
POST /api/comments/{commentId}/likes      # 点赞（commentId可以是子评论ID）
DELETE /api/comments/{commentId}/likes    # 取消点赞
GET /api/comments/{commentId}/likes       # 获取点赞信息
```

---

## 4. 业务逻辑

### 4.1 创建子评论流程

```
1. 验证用户身份（必须登录）
   ↓
2. 查找父评论（不存在则抛出404）
   ↓
3. 检查父评论所属文章是否为草稿
   - 是草稿 → 抛出403错误
   - 非草稿 → 继续
   ↓
4. 验证replyToUserId（如果提供）
   - 不存在 → 抛出400错误
   - 存在 → 继续
   ↓
5. 计算评论层级
   level = parent.level + 1
   ↓
6. 检查层级限制（建议≤3）
   - 超限 → 抛出400错误
   - 未超限 → 继续
   ↓
7. 创建子评论记录
   ↓
8. 保存到数据库
   ↓
9. 返回CommentResponse
```

### 4.2 删除评论流程

得益于在 `Comment.java` 实体中通过 `cascade = CascadeType.ALL` 声明了级联关系，Service层的删除逻辑变得极其简洁：

```java
@Transactional
public void deleteComment(Long commentId, UserDetails currentUser) {
    // 1. 查找评论实体
    Comment comment = commentRepository.findById(commentId).orElseThrow(...);
    
    // 2. 权限检查 (评论作者或文章作者)
    // ...
    
    // 3. 直接删除评论实体
    // JPA/Hibernate 会自动处理所有级联删除：
    // a. 递归删除所有子评论 (replies)
    // b. 删除所有被删除评论（包括子评论）关联的点赞记录 (likes)
    commentRepository.delete(comment);
}
```

---

## 5. 级联删除

### 5.1 删除关系链

参考[COMMENTS.md - 级联删除流程](./COMMENTS.md#级联删除流程)

**增强后的删除关系**：

```
文章删除（Post）
  ↓
  ├─→ 文章点赞删除（Likes where post_id）
  ├─→ 顶层评论删除（Comments where parent_id IS NULL）
  │     ↓
  │     ├─→ 一级子评论删除（Comments where parent_id = 顶层评论ID）
  │     │     ↓
  │     │     ├─→ 二级子评论删除（Comments where parent_id = 一级子评论ID）
  │     │     │     ↓
  │     │     │     └─→ 二级子评论点赞删除（Likes where comment_id）
  │     │     └─→ 一级子评论点赞删除（Likes where comment_id）
  │     └─→ 顶层评论点赞删除（Likes where comment_id）
  └─→ 文章本身删除
```

### 5.2 数据库外键级联策略

**Comment表外键设置**：

```sql
-- 父评论级联删除
FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
  → 删除父评论时，自动删除所有子评论（递归）

-- 被@用户删除时设为NULL
FOREIGN KEY (reply_to_user_id) REFERENCES users(id) ON DELETE SET NULL
  → 删除被@用户时，reply_to_user_id自动设为NULL，保留评论内容

-- 文章级联删除
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
  → 删除文章时，自动删除所有评论（包括子评论）

-- 评论作者级联删除
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
  → 删除用户时，自动删除该用户的所有评论（包括子评论）
```

### 5.3 级联删除测试矩阵

| 操作 | 直接影响 | 级联影响 | 验证点 |
|------|---------|---------|--------|
| 删除文章 | 文章、文章点赞 | 所有评论（顶层+子评论）、所有评论点赞 | 1. 评论表为空<br>2. 点赞表无该文章相关记录 |
| 删除顶层评论 | 顶层评论、顶层评论点赞 | 所有子评论、所有子评论点赞 | 1. 子评论表为空<br>2. 点赞表无该评论及子评论记录 |
| 删除子评论 | 子评论、子评论点赞 | 该子评论的子孙评论、子孙评论点赞 | 1. 子孙评论被删除<br>2. 父评论和兄弟评论不受影响 |
| 删除用户 | 用户账号 | 该用户的所有评论、该用户的所有点赞 | 1. 用户评论被删除<br>2. 其他用户对该评论的回复：reply_to_user_id设为NULL |

---

## 6. 前端展示设计

### 6.1 UI布局方案

**嵌套层级展示**：

```
┌─────────────────────────────────────────────────┐
│ 【顶层评论】                                      │
│ 用户A：这是一条顶层评论                           │
│ 👍 10  💬 3条回复                                 │
│                                                   │
│   ┌─────────────────────────────────────────┐   │
│   │ 【一级回复】                              │   │
│   │ 用户B：这是回复                           │   │
│   │ 👍 5                                      │   │
│   │                                           │   │
│   │     ┌───────────────────────────────┐   │   │
│   │     │ 【二级回复】                   │   │   │
│   │     │ 用户C：@用户B 再次回复         │   │   │
│   │     │ 👍 2                           │   │   │
│   │     └───────────────────────────────┘   │   │
│   └─────────────────────────────────────────┘   │
│                                                   │
│ [回复] [收起回复]                                │
└─────────────────────────────────────────────────┘
```

**视觉特点**：
- 顶层评论：无缩进，背景白色
- 一级回复：左侧缩进20px，浅灰色背景
- 二级回复：左侧缩进40px，更浅灰色背景
- 三级回复：左侧缩进60px（最大层级）

### 6.2 组件结构设计

```
CommentList.vue           （现有，需修改）
  ├─ CommentItem.vue      （现有，需修改）
  │   └─ ReplyList.vue    （新增）
  │       └─ ReplyItem.vue（新增）
  └─ CommentForm.vue      （可选：抽取表单为独立组件）
```

### 6.3 数据加载策略

**初始加载**：
```
1. 加载文章详情
2. 加载顶层评论（第一页，10条）
3. 每条顶层评论显示"N条回复"（replyCount）
4. 子评论不加载（懒加载）
```

**懒加载回复**：
```
用户点击"查看3条回复" 按钮
  ↓
调用 GET /api/comments/{commentId}/replies?page=0&size=20
  ↓
显示子评论列表（包括所有层级）
  ↓
若子评论数量 > 20，显示"加载更多回复"按钮
```

---

## 7. 架构图示

### 7.1 数据库关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                         users (用户表)                           │
│  - id                                                             │
│  - username                                                       │
│  - email                                                          │
│  - avatar_url                                                     │
└───────┬───────────────────────────┬─────────────────────────────┘
        │                           │
        │ user_id                   │ reply_to_user_id
        │                           │ (ON DELETE SET NULL)
        ▼                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                       comments (评论表)                          │
│  - id                                                             │
│  - content                                                        │
│  - user_id          (外键 → users.id, CASCADE)                  │
│  - post_id          (外键 → posts.id, CASCADE)                  │
│  - parent_id        (外键 → comments.id, CASCADE) 【新增】      │
│  - reply_to_user_id (外键 → users.id, SET NULL)  【新增】      │
│  - level            (INT, 0-3)                    【新增】      │
│  - created_at                                                    │
│  - updated_at                                                    │
└───────┬───────────────────────────────────────────────────┬─────┘
        │                                                     │
        │ comment_id                                         │ parent_id
        │ (ON DELETE CASCADE)                                │ (自引用)
        ▼                                                     ▼
┌───────────────────────┐                           ┌────────────────┐
│   likes (点赞表)       │                           │  子评论 (递归) │
│  - id                 │                           │  parent_id → id│
│  - user_id            │                           └────────────────┘
│  - post_id (可空)     │
│  - comment_id (可空)  │
│  - created_at         │
└───────────────────────┘
```

### 7.2 API架构图

```
┌────────────────────────────────────────────────────────────────┐
│                         前端 (Vue.js)                           │
└───────┬────────────────────────────────────────────────────────┘
        │
        │ HTTP/JSON
        ▼
┌────────────────────────────────────────────────────────────────┐
│                    后端 API (Spring Boot)                       │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  现有API (保持不变):                                            │
│  ├─ PUT  /api/comments/{id}          (更新评论)               │
│  ├─ DELETE /api/comments/{id}        (删除评论，增强级联)      │
│  ├─ POST /api/comments/{id}/likes    (点赞评论/子评论)        │
│  ├─ DELETE /api/comments/{id}/likes  (取消点赞)               │
│  └─ GET /api/comments/{id}/likes     (获取点赞信息)           │
│                                                                 │
│  修改API (返回格式调整):                                        │
│  ├─ GET /api/posts/{id}/comments     (只返回顶层评论)         │
│  │    新增字段: parentId, level, replyCount                    │
│  └─ GET /api/comments/my             (包含顶层+子评论)        │
│       新增字段: parentId, replyToUsername                      │
│                                                                 │
│  新增API:                                                       │
│  ├─ POST /api/comments/{commentId}/replies                    │
│  │    创建子评论 (回复)                                        │
│  └─ GET /api/comments/{commentId}/replies                     │
│       获取子评论列表 (分页)                                    │
│                                                                 │
└───────┬────────────────────────────────────────────────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────────┐
│                        数据库 (MySQL)                           │
└────────────────────────────────────────────────────────────────┘
```

### 7.3 级联删除流程图

```
用户操作: 删除评论 (DELETE /api/comments/{id})
  │
  └─► CommentServiceImpl.deleteComment()
        │
        ├─► 1. 权限检查
        │
        └─► 2. commentRepository.delete(comment)
              │
              └─► JPA/Hibernate 自动触发级联删除
                    │
                    ├─► 自动删除所有关联的 Like 记录
                    │     (因为 @OneToMany(cascade=ALL) on 'likes')
                    │
                    └─► 自动删除所有关联的子 Comment 记录 (Replies)
                          (因为 @OneToMany(cascade=ALL) on 'replies')
                          │
                          └─► 对每个子评论，递归执行以上删除流程
```

### 7.4 数据加载流程图

```
【初始加载】
前端页面加载
  ├─→ 加载文章详情
  ├─→ 加载顶层评论列表 (第1页，10条)
  │     每条顶层评论显示:
  │     - 评论内容
  │     - 作者信息
  │     - 点赞数
  │     - 「查看 N 条回复」按钮 (如果 replyCount > 0)
  └─→ 子评论不加载 (懒加载)

【懒加载子评论】
用户点击「查看 3 条回复」
  ├─→ 调用 GET /api/comments/{commentId}/replies
  ├─→ 显示回复列表 (包括所有层级)
  │     - 一级回复 (缩进 20px)
  │     - 二级回复 (缩进 40px，@用户名)
  │     - 三级回复 (缩进 60px，@用户名)
  └─→ 如果回复数 > 20，显示「加载更多回复」按钮

【创建回复】
用户点击「回复」按钮
  ├─→ 展开回复输入框
  │     - 顶层评论回复: 不自动填充
  │     - 子评论回复: 自动填充 @用户名
  ├─→ 用户输入内容
  ├─→ 点击「发送」
  ├─→ 调用 POST /api/comments/{commentId}/replies
  │     {
  │       "content": "回复内容",
  │       "replyToUserId": 123  // 可选
  │     }
  └─→ 成功后刷新回复列表
```

### 7.5 UI展示示例

```
┌─────────────────────────────────────────────────────────────────┐
│ 【顶层评论 - level 0】                                           │
│ ┌───────────────────────────────────────────────────────────┐   │
│ │ 👤 张三  •  2小时前                              ✏️ 🗑️    │   │
│ │ 这是一条顶层评论内容...                                   │   │
│ │ 👍 10  💬 3条回复                    [回复]              │   │
│ └───────────────────────────────────────────────────────────┘   │
│                                                                   │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ 【一级回复 - level 1】                                 │     │
│   │ 👤 李四  •  1小时前                         ✏️ 🗑️     │     │
│   │ 这是第一条回复...                                      │     │
│   │ 👍 5                                [回复]             │     │
│   │                                                         │     │
│   │     ┌───────────────────────────────────────────┐     │     │
│   │     │ 【二级回复 - level 2】                     │     │     │
│   │     │ 👤 王五  •  30分钟前              ✏️ 🗑️  │     │     │
│   │     │ @李四 我来回复你...                        │     │     │
│   │     │ 👍 2                        [回复]         │     │     │
│   │     └───────────────────────────────────────────┘     │     │
│   │                                                         │     │
│   │     ┌───────────────────────────────────────────┐     │     │
│   │     │ 【二级回复 - level 2】                     │     │     │
│   │     │ 👤 赵六  •  15分钟前              ✏️ 🗑️  │     │     │
│   │     │ @李四 +1                                   │     │     │
│   │     │ 👍 1                        [回复]         │     │     │
│   │     └───────────────────────────────────────────┘     │     │
│   └───────────────────────────────────────────────────────┘     │
│                                                                   │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ 【一级回复 - level 1】                                 │     │
│   │ 👤 孙七  •  10分钟前                        ✏️ 🗑️     │     │
│   │ 又一条回复...                                          │     │
│   │ 👍 3                                [回复]             │     │
│   └───────────────────────────────────────────────────────┘     │
│                                                                   │
│ [收起回复]                                                       │
└─────────────────────────────────────────────────────────────────┘

样式特征:
- 顶层评论: 无缩进，白色背景
- 一级回复: 左侧缩进20px，浅灰色背景 (bg-gray-50)
- 二级回复: 左侧缩进40px，更浅灰色背景 (bg-gray-100)
- 三级回复: 左侧缩进60px，最浅灰色背景 (bg-gray-150)
- @用户名: 蓝色文字 (text-primary-600)
```

---

## 参考文档

- [评论功能实现文档 (COMMENTS.md)](./COMMENTS.md)
- [点赞功能实现文档 (LIKES.md)](./LIKES.md)
- [API接口文档 (API.md)](../API.md)
- [项目架构说明 (ARCHITECTURE.md)](../ARCHITECTURE.md)

---

**文档版本**：v1.0  
**创建日期**：2025-10-19  
**最后更新**：2025-10-22  
**状态**：已实现
