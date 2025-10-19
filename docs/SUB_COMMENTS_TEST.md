# 子评论功能测试文档

本文档提供Postman测试步骤，用于验证后端子评论功能的完整实现。

---

## 测试环境

- **Base URL**: `http://localhost:8080`
- **数据库**: MySQL (自动使用Hibernate创建/更新表结构)
- **认证**: JWT Bearer Token

---

## 前置条件

1. 启动后端服务
2. 数据库中需要有:
   - 至少一个注册用户(用于登录)
   - 至少一篇已发布的文章(draft=false)
3. 获取JWT Token (通过登录接口)

---

## 测试场景

### 场景1: 创建顶层评论

**请求**:
```http
POST http://localhost:8080/api/posts/1/comments
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "这是一条顶层评论"
}
```

**预期响应**: 201 Created
```json
{
  "id": 1,
  "content": "这是一条顶层评论",
  "postId": 1,
  "postTitle": "文章标题",
  "parentId": null,
  "replyToUserId": null,
  "replyToUsername": null,
  "level": 0,
  "replyCount": 0,
  "authorUsername": "user1",
  "authorAvatarUrl": "/uploads/1/avatars/xxx.jpg",
  "createdAt": "2025-10-19T12:00:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**验证点**:
- ✅ parentId为null
- ✅ level为0
- ✅ replyCount为0

---

### 场景2: 创建一级子评论(回复顶层评论)

**请求**:
```http
POST http://localhost:8080/api/comments/1/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "这是一条一级回复"
}
```

**预期响应**: 201 Created
```json
{
  "id": 2,
  "content": "这是一条一级回复",
  "postId": 1,
  "postTitle": "文章标题",
  "parentId": 1,
  "replyToUserId": null,
  "replyToUsername": null,
  "level": 1,
  "replyCount": 0,
  "authorUsername": "user2",
  "authorAvatarUrl": "/uploads/2/avatars/xxx.jpg",
  "createdAt": "2025-10-19T12:05:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**验证点**:
- ✅ parentId为1(顶层评论ID)
- ✅ level为1
- ✅ replyToUserId为null(未@特定用户)

---

### 场景3: 创建一级子评论并@特定用户

**请求**:
```http
POST http://localhost:8080/api/comments/1/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "这是一条@用户的回复",
  "replyToUserId": 1
}
```

**预期响应**: 201 Created
```json
{
  "id": 3,
  "content": "这是一条@用户的回复",
  "postId": 1,
  "postTitle": "文章标题",
  "parentId": 1,
  "replyToUserId": 1,
  "replyToUsername": "user1",
  "level": 1,
  "replyCount": 0,
  "authorUsername": "user2",
  "authorAvatarUrl": "/uploads/2/avatars/xxx.jpg",
  "createdAt": "2025-10-19T12:10:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**验证点**:
- ✅ replyToUserId为1
- ✅ replyToUsername正确显示被@用户名

---

### 场景4: 创建二级子评论(回复一级子评论)

**请求**:
```http
POST http://localhost:8080/api/comments/2/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "这是二级回复",
  "replyToUserId": 2
}
```

**预期响应**: 201 Created
```json
{
  "id": 4,
  "content": "这是二级回复",
  "postId": 1,
  "postTitle": "文章标题",
  "parentId": 2,
  "replyToUserId": 2,
  "replyToUsername": "user2",
  "level": 2,
  "replyCount": 0,
  "authorUsername": "user3",
  "authorAvatarUrl": "/uploads/3/avatars/xxx.jpg",
  "createdAt": "2025-10-19T12:15:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**验证点**:
- ✅ level为2
- ✅ 父评论是一级子评论(id=2)

---

### 场景5: 创建三级子评论(最大层级)

**请求**:
```http
POST http://localhost:8080/api/comments/4/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "这是三级回复(最大层级)",
  "replyToUserId": 3
}
```

**预期响应**: 201 Created
```json
{
  "id": 5,
  "content": "这是三级回复(最大层级)",
  "postId": 1,
  "postTitle": "文章标题",
  "parentId": 4,
  "replyToUserId": 3,
  "replyToUsername": "user3",
  "level": 3,
  "replyCount": 0,
  "authorUsername": "user4",
  "authorAvatarUrl": "/uploads/4/avatars/xxx.jpg",
  "createdAt": "2025-10-19T12:20:00",
  "updatedAt": null,
  "likeCount": 0,
  "liked": false
}
```

**验证点**:
- ✅ level为3(最大层级)

---

### 场景6: 尝试创建超过最大层级的回复(应失败)

**请求**:
```http
POST http://localhost:8080/api/comments/5/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "尝试创建第4层回复"
}
```

**预期响应**: 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "回复层级过深，无法继续回复（最大层级: 3）"
}
```

**验证点**:
- ✅ 返回403错误
- ✅ 错误消息提示层级限制

---

### 场景7: 获取文章的顶层评论(只返回level=0的评论)

**请求**:
```http
GET http://localhost:8080/api/posts/1/comments?page=0&size=20
```

**预期响应**: 200 OK
```json
{
  "content": [
    {
      "id": 1,
      "content": "这是一条顶层评论",
      "postId": 1,
      "postTitle": "文章标题",
      "parentId": null,
      "replyToUserId": null,
      "replyToUsername": null,
      "level": 0,
      "replyCount": 4,  // 包含所有子孙评论数量
      "authorUsername": "user1",
      "authorAvatarUrl": "/uploads/1/avatars/xxx.jpg",
      "createdAt": "2025-10-19T12:00:00",
      "updatedAt": null,
      "likeCount": 0,
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

**验证点**:
- ✅ 只返回顶层评论(parentId=null)
- ✅ replyCount正确统计所有子孙评论数量(递归统计)
- ✅ 不包含任何子评论

---

### 场景8: 获取评论的所有回复

**请求**:
```http
GET http://localhost:8080/api/comments/1/replies?page=0&size=20
```

**预期响应**: 200 OK
```json
{
  "content": [
    {
      "id": 2,
      "content": "这是一条一级回复",
      "parentId": 1,
      "level": 1,
      "replyCount": 0,
      ...
    },
    {
      "id": 3,
      "content": "这是一条@用户的回复",
      "parentId": 1,
      "replyToUserId": 1,
      "replyToUsername": "user1",
      "level": 1,
      "replyCount": 0,
      ...
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

**验证点**:
- ✅ 只返回直接子评论(parentId=1)
- ✅ 按创建时间升序排列
- ✅ 不包含更深层级的回复(需要前端递归加载)

---

### 场景9: 获取当前用户的所有评论(包含顶层评论和子评论)

**请求**:
```http
GET http://localhost:8080/api/comments/my?page=0&size=10
Authorization: Bearer {your_token}
```

**预期响应**: 200 OK
```json
{
  "content": [
    {
      "id": 1,
      "content": "这是一条顶层评论",
      "parentId": null,
      "level": 0,
      "replyCount": 4,
      ...
    },
    {
      "id": 2,
      "content": "这是一条一级回复",
      "parentId": 1,
      "level": 1,
      "replyCount": 0,
      ...
    }
  ],
  "totalElements": 2
}
```

**验证点**:
- ✅ 包含顶层评论和子评论
- ✅ 可通过parentId和level区分评论类型

---

### 场景10: 更新子评论

**请求**:
```http
PUT http://localhost:8080/api/comments/2
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "更新后的一级回复内容"
}
```

**预期响应**: 200 OK
```json
{
  "id": 2,
  "content": "更新后的一级回复内容",
  "parentId": 1,
  "level": 1,
  "updatedAt": "2025-10-19T12:30:00",
  ...
}
```

**验证点**:
- ✅ 内容更新成功
- ✅ updatedAt字段已更新
- ✅ 其他字段保持不变

---

### 场景11: 点赞子评论

**请求**:
```http
POST http://localhost:8080/api/comments/2/likes
Authorization: Bearer {your_token}
```

**预期响应**: 200 OK
```json
{
  "likeCount": 1,
  "liked": true
}
```

**验证点**:
- ✅ 子评论点赞成功
- ✅ 与顶层评论点赞功能一致

---

### 场景12: 取消点赞子评论

**请求**:
```http
DELETE http://localhost:8080/api/comments/2/likes
Authorization: Bearer {your_token}
```

**预期响应**: 200 OK
```json
{
  "likeCount": 0,
  "liked": false
}
```

**验证点**:
- ✅ 取消点赞成功

---

### 场景13: 删除子评论(级联删除其所有子孙评论)

**准备数据**: 确保评论2有子评论(如评论4)

**请求**:
```http
DELETE http://localhost:8080/api/comments/2
Authorization: Bearer {your_token}
```

**预期响应**: 200 OK
```json
"评论删除成功"
```

**验证点**:
- ✅ 评论2被删除
- ✅ 评论2的所有子孙评论(如评论4、5)也被删除
- ✅ 所有相关点赞记录被删除

**验证方式**:
```http
GET http://localhost:8080/api/comments/1/replies
```
应该看不到评论2及其子评论

---

### 场景14: 删除顶层评论(级联删除所有子评论)

**请求**:
```http
DELETE http://localhost:8080/api/comments/1
Authorization: Bearer {your_token}
```

**预期响应**: 200 OK
```json
"评论删除成功"
```

**验证点**:
- ✅ 顶层评论被删除
- ✅ 所有子孙评论(评论2、3、4、5等)全部删除
- ✅ 所有相关点赞记录被删除

**验证方式**:
```http
GET http://localhost:8080/api/posts/1/comments
```
应该看到totalElements减少

---

### 场景15: 权限验证 - 文章作者可以删除任何评论

**准备**: 使用文章作者的Token

**请求**:
```http
DELETE http://localhost:8080/api/comments/2
Authorization: Bearer {post_author_token}
```

**预期响应**: 200 OK

**验证点**:
- ✅ 文章作者可以删除任何人的评论

---

### 场景16: 权限验证 - 非作者不能删除他人评论

**准备**: 使用其他用户的Token

**请求**:
```http
DELETE http://localhost:8080/api/comments/2
Authorization: Bearer {other_user_token}
```

**预期响应**: 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "您没有权限删除此评论"
}
```

**验证点**:
- ✅ 返回403错误
- ✅ 非作者无法删除他人评论

---

### 场景17: 草稿文章评论限制

**请求**: 尝试回复草稿文章的评论
```http
POST http://localhost:8080/api/comments/100/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "尝试回复草稿文章的评论"
}
```

**预期响应**: 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "不能回复草稿文章的评论"
}
```

**验证点**:
- ✅ 返回403错误
- ✅ 不允许回复草稿文章的评论

---

### 场景18: 无效的replyToUserId验证

**请求**:
```http
POST http://localhost:8080/api/comments/1/replies
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "content": "回复内容",
  "replyToUserId": 99999  // 不存在的用户ID
}
```

**预期响应**: 404 Not Found
```json
{
  "error": "Not Found",
  "message": "未找到被回复的用户ID: 99999"
}
```

**验证点**:
- ✅ 返回404错误
- ✅ 验证replyToUserId存在性

---

## 数据库验证

### 检查Comment表结构

连接MySQL数据库，执行:

```sql
DESC comments;
```

**预期结果**: 应包含以下新字段
- `parent_id` BIGINT NULL
- `reply_to_user_id` BIGINT NULL
- `level` INT NOT NULL DEFAULT 0

### 检查外键约束

```sql
SHOW CREATE TABLE comments;
```

**预期结果**: 应包含外键约束
- `FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE`
- `FOREIGN KEY (reply_to_user_id) REFERENCES users(id) ON DELETE SET NULL`

### 检查索引

```sql
SHOW INDEX FROM comments;
```

**预期结果**: 应包含索引
- `idx_comment_parent` on `parent_id`
- `idx_comment_reply_to_user` on `reply_to_user_id`
- `idx_comment_level` on `level`

---

## 性能测试

### 测试场景1: 深层嵌套回复

创建多层嵌套评论(0→1→2→3层级)，验证:
1. replyCount统计准确
2. 递归查询性能可接受

### 测试场景2: 大量子评论

为一条顶层评论创建100+条子评论，验证:
1. 分页功能正常
2. 响应时间合理(<500ms)

### 测试场景3: 级联删除性能

删除包含大量子孙评论的顶层评论，验证:
1. 所有子孙评论被删除
2. 所有点赞记录被删除
3. 事务成功提交

---

## 错误处理测试

### 1. 内容验证

**请求**: 空内容
```json
{
  "content": ""
}
```
**预期**: 400 Bad Request - "回复内容不能为空"

**请求**: 超长内容(>3000字符)
```json
{
  "content": "x".repeat(3001)
}
```
**预期**: 400 Bad Request - "回复内容长度必须在1到3000个字符之间"

### 2. 认证失败

**请求**: 无Token
```http
POST http://localhost:8080/api/comments/1/replies
```
**预期**: 401 Unauthorized

**请求**: 无效Token
```http
Authorization: Bearer invalid_token
```
**预期**: 401 Unauthorized

### 3. 资源不存在

**请求**: 不存在的评论ID
```http
POST http://localhost:8080/api/comments/99999/replies
```
**预期**: 404 Not Found - "未找到评论ID: 99999"

---

## 测试清单 (Checklist)

核心功能:
- [ ] 创建顶层评论(level=0)
- [ ] 创建一级子评论(level=1)
- [ ] 创建二级子评论(level=2)
- [ ] 创建三级子评论(level=3)
- [ ] 层级限制(level>3返回错误)
- [ ] @用户功能(replyToUserId)
- [ ] 获取顶层评论(只返回parentId=null)
- [ ] 获取子评论列表(分页)
- [ ] replyCount统计正确
- [ ] 更新子评论
- [ ] 删除子评论(级联删除)
- [ ] 删除顶层评论(级联删除所有子评论)

点赞功能:
- [ ] 点赞子评论
- [ ] 取消点赞子评论
- [ ] 子评论点赞数正确显示

权限验证:
- [ ] 评论作者可删除自己的评论
- [ ] 文章作者可删除任何评论
- [ ] 非作者不能删除他人评论
- [ ] 不能回复草稿文章的评论

数据验证:
- [ ] 内容长度验证(1-3000字符)
- [ ] replyToUserId存在性验证
- [ ] 父评论存在性验证

级联删除:
- [ ] 删除顶层评论→所有子评论被删除
- [ ] 删除子评论→其子孙评论被删除
- [ ] 所有相关点赞记录被删除

向后兼容:
- [ ] 现有评论功能不受影响
- [ ] 现有点赞功能不受影响
- [ ] 获取我的评论包含所有类型

---

## 常见问题

**Q: 如何知道一条评论有多少回复?**
A: 查看顶层评论的`replyCount`字段，它递归统计了所有子孙评论数量。

**Q: 如何获取多层嵌套的回复?**
A: 前端需要递归加载。先获取顶层评论的直接回复，然后对每条回复再获取其回复。

**Q: 删除评论后，子评论会怎样?**
A: 所有子孙评论会被级联删除，相关的点赞记录也会被删除。

**Q: 配置最大层级在哪里?**
A: 在`application.properties`中配置`comment.max-nesting-level=3`。

**Q: 如何验证数据库表结构是否正确?**
A: 启动应用后，Hibernate会自动创建/更新表结构。可通过SQL查看表结构和索引。

---

## 总结

本测试文档涵盖了子评论功能的所有核心场景，包括:
- ✅ CRUD操作
- ✅ 层级限制
- ✅ @用户功能
- ✅ 权限控制
- ✅ 级联删除
- ✅ 点赞功能
- ✅ 错误处理

建议按照场景1-18的顺序依次测试，确保每个场景都通过验证。

---

**文档版本**: v1.0  
**创建日期**: 2025-10-19  
**适用版本**: Backend v0.0.1-SNAPSHOT
