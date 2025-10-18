# 评论点赞功能 API 快速参考

## 快速开始

这是一个快速参考指南，帮助你快速测试评论点赞功能。完整的测试指南请查看 `comment-like-testing-guide.md`。

## 前提条件

### 1. 数据库迁移

**⚠️ 重要提示**：由于Hibernate的限制，需要执行SQL语句。

**必需步骤（只需执行一次）**：

```sql
-- 1. 将 post_id 列改为可空（Hibernate无法自动修改现有列的约束）
ALTER TABLE likes MODIFY COLUMN post_id BIGINT NULL;

-- 2. 添加/更新外键约束以支持级联删除
-- 注意：如果外键已存在但没有 ON DELETE CASCADE，需要先删除再重建
-- 使用 SHOW CREATE TABLE likes; 查看现有约束

-- 添加评论点赞的外键（带级联删除）
ALTER TABLE likes 
ADD CONSTRAINT fk_likes_comment 
FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;
```

**为什么需要这个**：
- 如果你的 `likes` 表已存在，`post_id` 列可能是 NOT NULL
- Hibernate的 `ddl-auto=update` 模式**无法修改现有列的约束**
- 外键的级联删除需要数据库层面的支持，应用层也会确保删除评论前先删除相关点赞

执行上述SQL后，重启应用，Hibernate会自动：
- ✅ 添加 `comment_id` 列和相关约束
- ✅ 创建索引
- ✅ 保留所有现有数据

**如果是全新安装**（数据库中没有 `likes` 表），直接启动应用即可，无需执行任何SQL。

**可选步骤**：如果想添加数据库层面的CHECK约束（MySQL 8.0.16+），在首次启动应用后执行：

```sql
-- 可选：额外的数据完整性保护
ALTER TABLE likes ADD CONSTRAINT ck_like_target 
    CHECK ((post_id IS NOT NULL AND comment_id IS NULL) OR 
           (post_id IS NULL AND comment_id IS NOT NULL));
```

注意：即使不添加CHECK约束，应用层代码也会确保数据完整性。

### 2. 测试数据准备
确保系统中已存在：
- 至少一个用户账号（用于登录）
- 至少一篇文章
- 至少一条评论

## API 端点

### 1. 点赞评论

**请求**
```http
POST /api/comments/{commentId}/likes
Authorization: Bearer {your_jwt_token}
```

**成功响应 (200 OK)**
```json
{
  "likeCount": 1,
  "isLiked": true
}
```

**错误响应**
- `401 Unauthorized` - 未登录
- `404 Not Found` - 评论不存在

**示例（curl）**
```bash
curl -X POST "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. 取消点赞评论

**请求**
```http
DELETE /api/comments/{commentId}/likes
Authorization: Bearer {your_jwt_token}
```

**成功响应 (200 OK)**
```json
{
  "likeCount": 0,
  "isLiked": false
}
```

**错误响应**
- `401 Unauthorized` - 未登录
- `404 Not Found` - 评论不存在

**示例（curl）**
```bash
curl -X DELETE "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3. 获取评论点赞信息

**请求**
```http
GET /api/comments/{commentId}/likes
Authorization: Bearer {your_jwt_token}  # 可选
```

**成功响应 (200 OK)**
```json
{
  "likeCount": 5,
  "isLiked": true  // 未登录时始终为 false
}
```

**错误响应**
- `404 Not Found` - 评论不存在

**示例（curl - 已登录）**
```bash
curl -X GET "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**示例（curl - 匿名）**
```bash
curl -X GET "http://localhost:8080/api/comments/1/likes"
```

---

### 4. 获取评论列表（包含点赞信息）

**请求**
```http
GET /api/posts/{postId}/comments?page=0&size=10
Authorization: Bearer {your_jwt_token}  # 可选
```

**成功响应 (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "content": "这是一条很棒的评论！",
      "postId": 1,
      "postTitle": "我的第一篇博客",
      "authorUsername": "user123",
      "authorAvatarUrl": "https://example.com/avatar.jpg",
      "createdAt": "2023-10-15T10:30:00",
      "updatedAt": null,
      "likeCount": 5,
      "isLiked": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**示例（curl）**
```bash
curl -X GET "http://localhost:8080/api/posts/1/comments?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 快速测试流程

### 场景 1: 完整的点赞/取消流程

```bash
# 1. 登录获取 token（根据你的认证端点调整）
TOKEN=$(curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}' \
  | jq -r '.token')

# 2. 点赞评论 ID 为 1
curl -X POST "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN"
# 期望输出: {"likeCount":1,"isLiked":true}

# 3. 再次点赞（测试幂等性）
curl -X POST "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN"
# 期望输出: {"likeCount":1,"isLiked":true}（不会增加）

# 4. 查询点赞信息
curl -X GET "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN"
# 期望输出: {"likeCount":1,"isLiked":true}

# 5. 取消点赞
curl -X DELETE "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN"
# 期望输出: {"likeCount":0,"isLiked":false}

# 6. 再次取消点赞（测试幂等性）
curl -X DELETE "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN"
# 期望输出: {"likeCount":0,"isLiked":false}（不会报错）
```

### 场景 2: 多用户点赞

```bash
# 用户 A 点赞
curl -X POST "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN_A"

# 用户 B 点赞
curl -X POST "http://localhost:8080/api/comments/1/likes" \
  -H "Authorization: Bearer $TOKEN_B"

# 查询点赞数（应该是 2）
curl -X GET "http://localhost:8080/api/comments/1/likes"
# 期望输出: {"likeCount":2,"isLiked":false}（匿名查询）
```

### 场景 3: 查看包含点赞信息的评论列表

```bash
# 获取文章 ID 为 1 的所有评论
curl -X GET "http://localhost:8080/api/posts/1/comments" \
  -H "Authorization: Bearer $TOKEN"
# 每条评论都会包含 likeCount 和 isLiked 字段
```

---

## 使用 Postman 测试

### 设置环境变量
1. 创建环境，添加变量：
   - `baseUrl`: `http://localhost:8080`
   - `token`: 登录后获取的 JWT token
   - `commentId`: 要测试的评论 ID

### 导入请求集合

#### 1. 点赞评论
- **Method**: POST
- **URL**: `{{baseUrl}}/api/comments/{{commentId}}/likes`
- **Headers**: 
  - `Authorization`: `Bearer {{token}}`

#### 2. 取消点赞
- **Method**: DELETE
- **URL**: `{{baseUrl}}/api/comments/{{commentId}}/likes`
- **Headers**: 
  - `Authorization`: `Bearer {{token}}`

#### 3. 获取点赞信息
- **Method**: GET
- **URL**: `{{baseUrl}}/api/comments/{{commentId}}/likes`
- **Headers**: 
  - `Authorization`: `Bearer {{token}}` (可选)

---

## 常见问题

### Q: 为什么重复点赞不会增加点赞数？
A: 这是预期行为。系统确保每个用户对每条评论只能点赞一次，重复点赞会返回当前点赞数而不会创建重复记录。

### Q: 为什么匿名用户的 isLiked 始终是 false？
A: 因为匿名用户没有登录，系统无法判断该用户是否点赞过。只有登录用户才能看到自己是否点赞过某条评论。

### Q: 删除评论后，点赞记录会怎样？
A: 点赞记录会被自动删除（级联删除）。这由数据库外键约束 `ON DELETE CASCADE` 保证。

### Q: 性能如何？
A: 当前实现对于中小型应用（每页 <20 条评论）性能可接受。对于高流量场景，建议查看测试指南中的性能优化建议。

### Q: 我的 MySQL 版本低于 8.0.16，不支持 CHECK 约束怎么办？
A: 跳过 CHECK 约束的创建步骤。应用层代码会确保数据完整性（Like 实体只会关联文章或评论之一）。建议升级到 MySQL 8.0.16+ 以获得数据库层面的约束保护。

---

## 下一步

完成基本测试后，请参考 `comment-like-testing-guide.md` 进行：
- 边界情况测试
- 权限测试
- 并发测试
- 数据完整性验证

## 支持

如有问题，请查看：
1. 完整测试指南: `docs/comment-like-testing-guide.md`
2. 应用日志: 检查是否有错误或警告信息
3. 数据库: 验证数据是否正确存储
