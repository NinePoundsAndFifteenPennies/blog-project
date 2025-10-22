# 评论功能增强v1 - 子评论与子评论点赞设计文档

## 概述

本文档详细描述为博客系统评论功能增加**子评论（回复）**和**子评论点赞**功能的完整设计方案。

本设计遵循以下原则：
1. **前端友好**：API返回字段考虑前端渲染便利性
2. **级联完整**：处理所有删除操作的连锁效应，确保数据一致性
3. **高扩展性**：设计具备良好的扩展性，支持未来功能演进
4. **文档复用**：最大化复用现有文档，减少维护成本

## 目录

- [1. 需求分析](#1-需求分析)
- [2. 数据库设计](#2-数据库设计)
- [3. API设计](#3-api设计)
- [4. 业务逻辑设计](#4-业务逻辑设计)
- [5. 级联删除设计](#5-级联删除设计)
- [6. 前端展示设计](#6-前端展示设计)
- [7. 扩展性设计](#7-扩展性设计)
- [8. 实现步骤](#8-实现步骤)

---

## 1. 需求分析

### 1.1 功能需求

基于现有评论功能，新增以下能力：

#### 子评论功能
- ✅ 用户可以回复顶层评论（创建子评论）
- ✅ 用户可以回复其他子评论（创建嵌套回复）
- ✅ 子评论显示被回复者信息（@用户名）
- ✅ 子评论支持编辑（仅作者）
- ✅ 子评论支持删除（作者或文章作者）
- ✅ 支持纯文本和Markdown内容
- ✅ 子评论内容长度限制：1-3000字符

#### 子评论点赞功能
- ✅ 用户可以对子评论进行点赞
- ✅ 用户可以取消对子评论的点赞
- ✅ 匿名用户可以查看子评论点赞数量
- ✅ 登录用户可以查看自己的点赞状态
- ✅ 防止重复点赞（数据库唯一约束）

### 1.2 现有基础

参考文档：[COMMENTS.md](./COMMENTS.md)

当前系统已实现：
- ✅ 顶层评论CRUD功能
- ✅ 评论点赞功能
- ✅ 文章点赞功能
- ✅ 完善的级联删除机制（文章→评论→点赞）

### 1.3 技术约束

- 数据库：MySQL（JPA/Hibernate）
- 后端：Spring Boot
- 前端：Vue 3 + Composition API
- 认证：JWT Bearer Token

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

**约束与索引**：

```sql
-- 外键约束
ALTER TABLE comments 
  ADD CONSTRAINT fk_comment_parent 
  FOREIGN KEY (parent_id) 
  REFERENCES comments(id) 
  ON DELETE CASCADE;

ALTER TABLE comments 
  ADD CONSTRAINT fk_comment_reply_to_user 
  FOREIGN KEY (reply_to_user_id) 
  REFERENCES users(id) 
  ON DELETE SET NULL;

-- 索引优化
CREATE INDEX idx_comment_parent ON comments(parent_id);
CREATE INDEX idx_comment_reply_to_user ON comments(reply_to_user_id);
CREATE INDEX idx_comment_level ON comments(level);
CREATE INDEX idx_comment_post_parent ON comments(post_id, parent_id); -- 复合索引，优化查询
```

**业务约束**（应用层保证）：
- parent_id为NULL时，level必须为0
- parent_id不为NULL时，level = parent.level + 1
- level最大值限制（建议≤3）
- reply_to_user_id不为NULL时，parent_id也不能为NULL

**JPA/Hibernate 实体定义**:

为了让JPA能够自动处理级联删除，`Comment.java` 实体类中必须建立对子评论和点赞的 `@OneToMany` 关联，并配置级联选项。这是实现自动化、无异常删除的关键。

```java
// In Comment.java
@Entity
public class Comment {
    // ...

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 关键: 声明对子评论的一对多关系，并设置级联删除
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // 关键: 声明对点赞的一对多关系，并设置级联删除
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    // ...
}

### 2.2 Like表结构

**无需修改**！当前设计已支持评论点赞（参考[COMMENTS.md](./COMMENTS.md#like-表支持文章和评论点赞)）：

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "comment_id", nullable = true)
@OnDelete(action = OnDeleteAction.CASCADE)
private Comment comment;
```

子评论也是Comment实体，因此现有Like表天然支持子评论点赞，无需任何调整。

### 2.3 数据库迁移SQL

**MySQL创建语句**：

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

-- 为已有数据设置默认值（如果需要）
UPDATE comments SET level = 0, parent_id = NULL WHERE parent_id IS NULL;
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
  "replyToUserId": 123  // 可选：被@的用户ID，不填则回复评论本身
}
```

**字段说明**：
- `content`（必填）：回复内容，1-3000字符
- `replyToUserId`（可选）：被@的用户ID
  - 不填：表示回复评论本身
  - 填写：表示@特定用户

**成功响应**：`201 Created`
```json
{
  "id": 100,
  "content": "回复内容",
  "postId": 10,
  "postTitle": "文章标题",
  "parentId": 5,  // 父评论ID
  "replyToUserId": 123,  // 被@用户ID（可为null）
  "replyToUsername": "张三",  // 被@用户名（可为null）
  "level": 1,  // 评论层级
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

**业务规则**：
1. 必须是登录用户
2. 父评论必须存在
3. 父评论所属文章不能是草稿
4. 层级检查：level ≤ 3（可配置）
5. 如果replyToUserId不为空，必须验证用户存在

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
    },
    {
      "id": 101,
      "content": "@用户A 回复内容",
      "postId": 10,
      "postTitle": "文章标题",
      "parentId": 5,
      "replyToUserId": 200,  // 被@用户ID
      "replyToUsername": "用户A",  // 被@用户名
      "level": 2,
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

**说明**：
- 匿名用户可访问
- 按创建时间升序排列（最早的在前）
- 返回完整的子评论信息（包括嵌套回复）
- 包含点赞数和当前用户点赞状态

**错误响应**：
- `404 Not Found`：评论不存在

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
      "parentId": null,  // 新增字段
      "replyToUserId": null,  // 新增字段
      "replyToUsername": null,  // 新增字段
      "level": 0,  // 新增字段
      "replyCount": 5,  // 新增字段：子评论数量
      "authorUsername": "用户名",
      "authorAvatarUrl": "/uploads/1/avatars/a.jpg",
      "createdAt": "2025-10-19T09:00:00",
      "updatedAt": null,
      "likeCount": 10,
      "liked": true
    }
  ],
  "totalElements": 50,  // 注意：这是顶层评论总数，不包括子评论
  "totalPages": 3
}
```

**新增字段说明**：
- `parentId`：父评论ID（顶层评论为null）
- `replyToUserId`：被@用户ID（顶层评论为null）
- `replyToUsername`：被@用户名（顶层评论为null）
- `level`：评论层级（顶层评论为0）
- `replyCount`：该评论的直接和间接子评论总数

**查询优化**：
```sql
-- 只查询顶层评论
SELECT * FROM comments 
WHERE post_id = ? AND parent_id IS NULL 
ORDER BY created_at ASC
LIMIT ? OFFSET ?;

-- 统计每个顶层评论的子评论数
SELECT parent_id, COUNT(*) as reply_count
FROM comments
WHERE post_id = ? AND parent_id IS NOT NULL
GROUP BY parent_id;
```

#### 3.3.2 更新评论（保持不变）

```http
PUT /api/comments/{commentId}
```

**行为不变**：
- 顶层评论和子评论使用相同接口更新
- 权限检查：仅评论作者可编辑
- 响应格式与现有一致（新增字段）

#### 3.3.3 删除评论（行为增强）

```http
DELETE /api/comments/{commentId}
```

**现有行为**：删除评论及其点赞

**增强后行为**：
- 删除顶层评论时，**级联删除所有子评论及其点赞**
- 删除子评论时，仅删除该子评论及其点赞
- 权限检查保持不变（评论作者或文章作者）

**级联删除链**：
```
顶层评论删除 → 一级子评论删除 → 二级子评论删除 → 所有点赞删除
```

#### 3.3.4 获取我的评论（修改）

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
      "parentId": 50,  // 父评论ID
      "replyToUserId": 123,
      "replyToUsername": "张三",
      "level": 1,
      "replyCount": 0,  // 子评论没有replyCount（或始终为0）
      "authorUsername": "我",
      "authorAvatarUrl": "/uploads/me.jpg",
      "createdAt": "2025-10-19T10:00:00",
      "updatedAt": null,
      "likeCount": 2,
      "liked": false
    }
  ],
  "totalElements": 25,  // 包括顶层评论和子评论
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

**响应格式**（保持不变）：
```json
{
  "likeCount": 5,
  "liked": true
}
```

---

## 4. 业务逻辑设计

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
   - 超限 → 抛出400错误（"回复层级过深，无法继续回复"）
   - 未超限 → 继续
   ↓
7. 创建子评论记录
   - content: 请求内容
   - user_id: 当前用户
   - post_id: 父评论的post_id
   - parent_id: 父评论ID
   - reply_to_user_id: 被@用户ID（可为null）
   - level: 计算得到的层级
   ↓
8. 保存到数据库
   ↓
9. 返回CommentResponse（包含点赞信息）
```

**关键点**：
- 子评论的post_id继承自父评论，确保数据一致性
- level自动计算，不由前端传递
- 层级限制可配置（application.properties）

### 4.2 获取子评论列表流程

```
1. 查找父评论（不存在则抛出404）
   ↓
2. 构建查询条件
   WHERE parent_id = {commentId} OR id IN (
     -- 递归查询所有子孙评论
     SELECT id FROM comments WHERE parent_id = {commentId}
     UNION ALL
     SELECT c2.id FROM comments c1
     JOIN comments c2 ON c1.id = c2.parent_id
     WHERE c1.parent_id = {commentId}
   )
   ↓
3. 按创建时间升序排列
   ORDER BY created_at ASC
   ↓
4. 应用分页
   LIMIT ? OFFSET ?
   ↓
5. 对每条子评论：
   - 查询点赞数（likeCount）
   - 查询当前用户点赞状态（liked）
   - 查询被@用户信息（replyToUsername）
   ↓
6. 返回分页结果
```

**查询优化建议**：

**方案1：递归CTE（MySQL 8.0+）**
```sql
WITH RECURSIVE reply_tree AS (
  -- 直接子评论
  SELECT * FROM comments WHERE parent_id = ?
  UNION ALL
  -- 递归查询子孙评论
  SELECT c.* FROM comments c
  INNER JOIN reply_tree rt ON c.parent_id = rt.id
)
SELECT * FROM reply_tree ORDER BY created_at ASC;
```

**方案2：简单查询（兼容性更好）**
```sql
-- 只查询直接子评论（一级回复）
SELECT * FROM comments 
WHERE parent_id = ? 
ORDER BY created_at ASC;

-- 前端需要时，递归加载更深层级
```

**推荐**：方案2（简单查询）
- 理由：层级限制≤3，深度可控，无需复杂递归
- 前端可实现"懒加载"：点击"查看更多回复"时加载下一层级

### 4.3 删除评论流程增强

**删除评论流程**：

得益于在 `Comment.java` 实体中通过 `cascade = CascadeType.ALL` 声明了级联关系，Service层的删除逻辑变得极其简洁和健壮，完全避免了在应用层手动处理级联的复杂性和风险。

**`CommentServiceImpl.deleteComment()` 核心逻辑:**

```java
@Transactional
public void deleteComment(Long commentId, UserDetails currentUser) {
    // 1. 查找评论实体
    Comment comment = commentRepository.findById(commentId).orElseThrow(...);

    // 2. 权限检查 (评论作者或文章作者)
    // ... (权限检查逻辑)

    // 3. 直接删除该评论实体
    // JPA/Hibernate 会自动处理所有级联删除：
    // a. 递归删除所有子评论 (replies)。
    // b. 删除所有被删除评论（包括子评论）关联的点赞记录 (likes)。
    commentRepository.delete(comment);
}
```

**关键点**：
- 使用数据库事务保证原子性
- 利用外键CASCADE级联删除简化逻辑
- 先删除点赞，再删除评论，确保数据一致性

### 4.4 统计子评论数量

**新增Service方法**：
```java
// 统计评论的直接和间接子评论总数
long countReplies(Long commentId);
```

**实现逻辑**：
```sql
-- 方案1：递归统计（精确）
WITH RECURSIVE reply_tree AS (
  SELECT id FROM comments WHERE parent_id = ?
  UNION ALL
  SELECT c.id FROM comments c
  INNER JOIN reply_tree rt ON c.parent_id = rt.id
)
SELECT COUNT(*) FROM reply_tree;

-- 方案2：简单统计（仅直接子评论）
SELECT COUNT(*) FROM comments WHERE parent_id = ?;
```

**使用场景**：
- 在返回顶层评论列表时，附加replyCount字段
- 前端显示"共X条回复"

---

## 5. 级联删除设计

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

**Like表外键设置**（无需修改）：

```sql
-- 评论级联删除
FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
  → 删除评论时，自动删除所有点赞（包括子评论点赞）
```

### 5.3 Service层级联逻辑 (修正后)

得益于在实体层（`Comment.java`）通过 `cascade = CascadeType.ALL` 和 `orphanRemoval = true` 声明了级联关系，Service层的删除逻辑变得非常简洁和健壮。

**`CommentServiceImpl.deleteComment()` 核心逻辑:**

```java
@Transactional
public void deleteComment(Long commentId, UserDetails currentUser) {
    // 1. 查找评论实体
    Comment comment = commentRepository.findById(commentId).orElseThrow(...);

    // 2. 权限检查 (评论作者或文章作者)
    // ...

    // 3. 直接删除评论实体
    // JPA/Hibernate 会自动处理所有级联删除：
    // a. 递归删除所有子评论 (replies)。
    // b. 删除所有被删除评论（包括子评论）关联的点赞记录 (likes)。
    commentRepository.delete(comment);
}
```

### 5.4 级联删除测试矩阵

| 操作 | 直接影响 | 级联影响 | 验证点 |
|------|---------|---------|--------|
| 删除文章 | 文章、文章点赞 | 所有评论（顶层+子评论）、所有评论点赞 | 1. 评论表为空<br>2. 点赞表无该文章相关记录 |
| 删除顶层评论 | 顶层评论、顶层评论点赞 | 所有子评论、所有子评论点赞 | 1. 子评论表为空<br>2. 点赞表无该评论及子评论记录 |
| 删除子评论 | 子评论、子评论点赞 | 该子评论的子孙评论、子孙评论点赞 | 1. 子孙评论被删除<br>2. 父评论和兄弟评论不受影响 |
| 删除用户 | 用户账号 | 该用户的所有评论、该用户的所有点赞 | 1. 用户评论被删除<br>2. 其他用户对该评论的回复：reply_to_user_id设为NULL |
| 删除被@用户 | 用户账号 | reply_to_user_id设为NULL | 1. 评论内容保留<br>2. reply_to_user_id = NULL<br>3. 前端显示"@[已删除用户]" |

---

## 6. 前端展示设计

### 6.1 UI布局方案

**方案：嵌套层级展示（推荐）**

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
│   │                                           │   │
│   │     ┌───────────────────────────────┐   │   │
│   │     │ 【二级回复】                   │   │   │
│   │     │ 用户D：@用户B 我也回复         │   │   │
│   │     │ 👍 1                           │   │   │
│   │     └───────────────────────────────┘   │   │
│   └─────────────────────────────────────────┘   │
│                                                   │
│   ┌─────────────────────────────────────────┐   │
│   │ 【一级回复】                              │   │
│   │ 用户E：又一条回复                         │   │
│   │ 👍 3                                      │   │
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

**新增组件**：

```
CommentList.vue           （现有，需修改）
  ├─ CommentItem.vue      （现有，需修改）
  │   └─ ReplyList.vue    （新增）
  │       └─ ReplyItem.vue（新增）
  └─ CommentForm.vue      （可选：抽取表单为独立组件）
```

**组件职责**：

1. **CommentList.vue**（修改）
   - 显示顶层评论列表
   - 处理创建顶层评论
   - 分页加载顶层评论
   - 传递postAuthorUsername给子组件

2. **CommentItem.vue**（修改）
   - 显示单条顶层评论
   - 点赞、编辑、删除顶层评论
   - **新增**：显示"N条回复"按钮
   - **新增**：展开/收起回复列表
   - **新增**：回复按钮（创建一级回复）

3. **ReplyList.vue**（新增）
   - 显示某条评论的所有回复
   - 懒加载子评论
   - 处理回复的分页

4. **ReplyItem.vue**（新增）
   - 显示单条回复（子评论）
   - 点赞、编辑、删除回复
   - 显示@用户名
   - **嵌套**：可包含更深层级的ReplyItem（递归组件）

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

**性能优化**：
- 顶层评论：分页加载（10条/页）
- 子评论：懒加载（点击才加载）
- 深层嵌套：一次性加载该顶层评论下的所有子评论（层级≤3，数量可控）

### 6.4 CommentResponse字段映射

**前端使用的字段**：

| 字段名 | 用途 | 示例 |
|--------|------|------|
| id | 评论唯一标识 | 100 |
| content | 评论内容 | "这是回复内容" |
| parentId | 是否为子评论 | null=顶层评论，非null=子评论 |
| replyToUserId | 被@用户ID | 123 |
| replyToUsername | 被@用户名 | "张三" |
| level | 评论层级 | 0=顶层，1=一级回复，2=二级回复 |
| replyCount | 子评论数量（仅顶层评论） | 5 |
| authorUsername | 评论作者 | "李四" |
| authorAvatarUrl | 作者头像 | "/uploads/4/avatars/abc.jpg" |
| createdAt | 创建时间 | "2025-10-19T10:00:00" |
| updatedAt | 更新时间 | null 或时间戳 |
| likeCount | 点赞数 | 3 |
| liked | 当前用户是否已点赞 | true/false |

**前端判断逻辑**：

```javascript
// 判断是否为顶层评论
const isTopLevel = comment.parentId === null;

// 判断是否需要显示@用户名
const shouldShowReplyTo = comment.replyToUsername !== null;

// 计算缩进
const indentLevel = comment.level * 20; // 每层级缩进20px

// 显示"X条回复"按钮（仅顶层评论）
const showRepliesButton = isTopLevel && comment.replyCount > 0;
```

### 6.5 用户交互流程

**回复顶层评论**：
```
1. 用户点击顶层评论下的"回复"按钮
2. 展开回复输入框（或弹出对话框）
3. 用户输入回复内容
4. 点击"发送"
5. 调用 POST /api/comments/{顶层评论ID}/replies
6. 成功后，刷新该顶层评论的回复列表
```

**回复子评论（@特定用户）**：
```
1. 用户点击子评论下的"回复"按钮
2. 展开回复输入框，自动填充"@用户名 "
3. 用户输入回复内容
4. 点击"发送"
5. 调用 POST /api/comments/{父评论ID}/replies
   请求体：{ "content": "...", "replyToUserId": 123 }
6. 成功后，刷新回复列表
```

**删除顶层评论**：
```
1. 用户点击"删除"按钮
2. 确认对话框："确定删除该评论及其所有回复吗？"
3. 确认后，调用 DELETE /api/comments/{commentId}
4. 成功后，从列表中移除该评论
```

**删除子评论**：
```
1. 用户点击"删除"按钮
2. 确认对话框："确定删除该回复及其所有子回复吗？"
3. 确认后，调用 DELETE /api/comments/{commentId}
4. 成功后，从列表中移除该回复
5. 更新父评论的replyCount
```

### 6.6 响应式设计

**移动端优化**：
- 减少缩进：每层级10px（移动端）vs 20px（桌面端）
- 最大层级显示：移动端最多显示2级，深层回复折叠
- 头像尺寸：移动端缩小至32px

**交互优化**：
- 长按评论弹出操作菜单（移动端）
- 点击评论内容展开/收起完整内容（超长评论）
- 滑动手势：左滑显示操作按钮（删除、回复）

---

## 7. 扩展性设计

### 7.1 支持的未来功能

**1. 评论通知系统**

数据基础已具备：
- reply_to_user_id：知道谁被@了
- user_id：知道谁发表了回复
- 可扩展Notification表：

```sql
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,  -- 接收通知的用户
  type VARCHAR(50),  -- 通知类型：COMMENT_REPLY, COMMENT_LIKE等
  related_comment_id BIGINT,  -- 相关评论ID
  triggered_by_user_id BIGINT,  -- 触发通知的用户
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP
);
```

**2. 评论审核/举报**

可添加字段：
- status：PENDING, APPROVED, REJECTED, REPORTED
- reported_count：举报次数
- reviewed_at：审核时间

**3. 评论排序**

支持的排序方式：
- 时间排序（默认）：created_at ASC/DESC
- 热度排序：ORDER BY likeCount DESC, created_at DESC
- 智能排序：综合点赞数、回复数、时间

**4. 评论搜索**

建议添加全文索引：
```sql
ALTER TABLE comments ADD FULLTEXT INDEX ft_content (content);
```

**5. 匿名评论**

可扩展：
- user_id可为NULL（匿名评论）
- 添加anonymous_name字段（匿名昵称）

**6. 评论草稿**

可添加：
- draft字段（类似Post的draft）
- 用户可保存评论草稿，稍后发布

### 7.2 配置化参数

建议在application.properties中配置：

```properties
# 评论配置
comment.max-length=3000
comment.max-nesting-level=3
comment.pagination.default-size=20
comment.reply.pagination.default-size=20

# 性能配置
comment.lazy-load-replies=true
comment.cache.enabled=true
comment.cache.ttl=300
```

### 7.3 API版本控制

建议采用URL版本控制：

```
/api/v1/comments/{id}     # 当前版本
/api/v2/comments/{id}     # 未来版本（支持新特性）
```

或使用请求头版本控制：
```
Accept: application/vnd.blog.v1+json
```

### 7.4 数据库扩展性

**水平扩展**：
- 按post_id分片（文章维度）
- 评论与文章在同一分片，避免跨库查询

**读写分离**：
- 写操作：主库
- 读操作：从库（评论查询占大多数）

**缓存策略**：
- Redis缓存：顶层评论列表（key: `post:{postId}:comments:page:{page}`）
- 缓存失效：新增/删除评论时清除
- TTL：5-10分钟

---


## 8. 风险与应对

### 8.1 技术风险

| 风险 | 影响 | 概率 | 应对策略 |
|------|------|------|---------|
| 递归查询性能问题 | 高 | 中 | 1. 限制嵌套层级≤3<br>2. 使用索引优化<br>3. 懒加载子评论 |
| 级联删除数据丢失 | 高 | 低 | 1. 完善事务管理<br>2. 软删除备份<br>3. 操作前二次确认 |
| 并发创建回复冲突 | 中 | 低 | 1. 乐观锁控制<br>2. 事务隔离 |
| 前端渲染性能下降 | 中 | 中 | 1. 虚拟滚动<br>2. 懒加载<br>3. 分页加载 |

### 9.2 业务风险

| 风险 | 影响 | 概率 | 应对策略 |
|------|------|------|---------|
| 用户滥用回复功能 | 中 | 中 | 1. 频率限制（Rate Limiting）<br>2. 内容审核<br>3. 举报功能 |
| 深层嵌套影响阅读体验 | 中 | 高 | 1. 限制最大层级<br>2. 折叠深层回复<br>3. 跳转到上下文 |

---

## 9. 总结

### 9.1 设计亮点

1. **向后兼容**：现有API行为不变，平滑升级
2. **级联完整**：所有删除关系清晰，无数据残留
3. **前端友好**：一次请求获取完整数据，减少往返
4. **高扩展性**：支持未来功能扩展（通知、审核、搜索）
5. **性能优化**：懒加载、分页、索引优化
6. **文档复用**：引用现有文档，减少重复

### 9.2 关键决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 子评论存储方式 | parent_id字段（邻接表） | 简单、灵活、支持递归查询 |
| 层级限制 | ≤3层 | 平衡用户体验和性能 |
| 加载策略 | 懒加载 | 减少初始加载时间 |
| 删除策略 | 级联删除 | 保证数据一致性 |
| UI布局 | 嵌套缩进 | 直观展示层级关系 |


### 9.4 参考文档

- [评论功能实现文档 (COMMENTS.md)](./COMMENTS.md)
- [点赞功能实现文档 (LIKES.md)](./LIKES.md)
- [API接口文档 (API.md)](../API.md)
- [项目架构说明 (ARCHITECTURE.md)](../ARCHITECTURE.md)

---

**文档版本**：v1.0  
**创建日期**：2025-10-19  
**作者**：GitHub Copilot  
**状态**：待审核
