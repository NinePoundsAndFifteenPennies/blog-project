# 评论点赞功能测试指南

## 功能概述
新增了对评论进行点赞和取消点赞的功能，与现有的文章点赞功能保持一致的设计风格。

## 数据库变更

### 自动迁移（推荐）

**无需手动执行SQL脚本！** 项目已配置 `spring.jpa.hibernate.ddl-auto=update`，Hibernate会在应用启动时自动执行以下操作：

1. ✅ 将 `post_id` 列改为可空
2. ✅ 添加 `comment_id` 列
3. ✅ 添加外键约束（`comment_id` -> `comments.id`，级联删除）
4. ✅ 添加唯一约束（`user_id`, `comment_id`）
5. ✅ 为 `comment_id` 添加索引

**重启应用时不会覆盖数据**：Hibernate的`update`模式会保留现有数据，只添加缺失的表结构。

### 可选：手动添加CHECK约束

Hibernate无法自动创建CHECK约束。如果需要数据库层面的额外保护，可以在首次启动应用后手动执行：

```sql
-- 可选：添加检查约束，确保 post_id 和 comment_id 只有一个不为空
-- 注意：MySQL 8.0.16+ 支持 CHECK 约束
-- 应用层代码已经确保数据完整性，此约束为额外保护
ALTER TABLE likes ADD CONSTRAINT ck_like_target 
    CHECK ((post_id IS NOT NULL AND comment_id IS NULL) OR 
           (post_id IS NULL AND comment_id IS NOT NULL));
```

**重要提示**：
- 即使不添加CHECK约束，应用层代码也会确保数据完整性
- CHECK约束需要 MySQL 8.0.16 或更高版本
- 旧版本MySQL可以跳过此步骤

## 新增 API 端点

### 1. 点赞评论
- **端点**: `POST /api/comments/{commentId}/likes`
- **认证**: 需要登录
- **请求参数**: 无请求体
- **响应**:
```json
{
  "likeCount": 5,
  "isLiked": true
}
```

### 2. 取消点赞评论
- **端点**: `DELETE /api/comments/{commentId}/likes`
- **认证**: 需要登录
- **请求参数**: 无
- **响应**:
```json
{
  "likeCount": 4,
  "isLiked": false
}
```

### 3. 获取评论点赞信息
- **端点**: `GET /api/comments/{commentId}/likes`
- **认证**: 可选（匿名用户 isLiked 始终为 false）
- **响应**:
```json
{
  "likeCount": 4,
  "isLiked": false
}
```

## 现有 API 的变更

### 获取评论列表
- **端点**: `GET /api/posts/{postId}/comments`
- **变更**: 响应中每个评论现在包含 `likeCount` 和 `isLiked` 字段
- **响应示例**:
```json
{
  "content": [...],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

每个评论对象现在包含：
```json
{
  "id": 1,
  "content": "这是一条评论",
  "postId": 1,
  "postTitle": "文章标题",
  "authorUsername": "user123",
  "authorAvatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2023-10-15T10:30:00",
  "updatedAt": null,
  "likeCount": 5,
  "isLiked": true
}
```

## 测试场景

### 基本功能测试

#### 场景 1: 点赞评论
1. 以用户 A 身份登录
2. 创建或找到一条评论（commentId = 1）
3. 调用 `POST /api/comments/1/likes`
4. **预期结果**:
   - 响应状态码 200
   - `isLiked` 为 `true`
   - `likeCount` 增加 1

#### 场景 2: 重复点赞（幂等性）
1. 继续使用用户 A 的会话
2. 再次调用 `POST /api/comments/1/likes`
3. **预期结果**:
   - 响应状态码 200
   - `likeCount` 不变（不会重复添加）
   - 日志中应显示"已经点赞过"的信息

#### 场景 3: 取消点赞
1. 继续使用用户 A 的会话
2. 调用 `DELETE /api/comments/1/likes`
3. **预期结果**:
   - 响应状态码 200
   - `isLiked` 为 `false`
   - `likeCount` 减少 1

#### 场景 4: 重复取消点赞（幂等性）
1. 继续使用用户 A 的会话
2. 再次调用 `DELETE /api/comments/1/likes`
3. **预期结果**:
   - 响应状态码 200
   - `likeCount` 不变
   - 日志中应显示"未点赞过"的信息

#### 场景 5: 多用户点赞
1. 用户 A 点赞评论 1
2. 用户 B 点赞评论 1
3. 用户 C 点赞评论 1
4. 调用 `GET /api/comments/1/likes`
5. **预期结果**:
   - `likeCount` 应为 3
   - 以用户 A 身份查询时 `isLiked` 为 `true`
   - 以用户 D（未点赞）身份查询时 `isLiked` 为 `false`

### 权限测试

#### 场景 6: 未登录用户点赞
1. 不传递认证令牌
2. 调用 `POST /api/comments/1/likes`
3. **预期结果**:
   - 响应状态码 401 (Unauthorized)

#### 场景 7: 匿名用户查看点赞信息
1. 不传递认证令牌
2. 调用 `GET /api/comments/1/likes`
3. **预期结果**:
   - 响应状态码 200
   - `isLiked` 始终为 `false`
   - `likeCount` 显示正确的点赞总数

### 异常情况测试

#### 场景 8: 点赞不存在的评论
1. 以用户 A 身份登录
2. 调用 `POST /api/comments/999999/likes`（假设 999999 不存在）
3. **预期结果**:
   - 响应状态码 404 (Not Found)
   - 错误消息："未找到ID为: 999999 的评论"

#### 场景 9: 获取已删除评论的点赞
1. 创建评论并记录 commentId
2. 有用户对其点赞
3. 删除该评论
4. 尝试调用 `GET /api/comments/{commentId}/likes`
5. **预期结果**:
   - 响应状态码 404 (Not Found)

### 级联删除测试

#### 场景 10: 删除评论时清理点赞
1. 创建评论（commentId = 1）
2. 多个用户对该评论点赞
3. 删除该评论
4. 检查数据库 `likes` 表
5. **预期结果**:
   - 所有关联的点赞记录应被自动删除
   - 不应残留孤立的点赞记录

### 集成测试

#### 场景 11: 获取评论列表时显示点赞信息
1. 创建文章（postId = 1）
2. 在该文章下创建 3 条评论
3. 用户 A 对评论 1 和 2 点赞
4. 用户 B 对评论 1 点赞
5. 以用户 A 身份调用 `GET /api/posts/1/comments`
6. **预期结果**:
   - 评论 1: `likeCount=2, isLiked=true`
   - 评论 2: `likeCount=1, isLiked=true`
   - 评论 3: `likeCount=0, isLiked=false`

#### 场景 12: 创建评论后立即显示点赞信息
1. 以用户 A 身份创建新评论
2. **预期结果**:
   - 响应中包含 `likeCount=0, isLiked=false`

#### 场景 13: 更新评论后保留点赞信息
1. 用户 A 创建评论
2. 用户 B 和 C 对其点赞
3. 用户 A 编辑评论内容
4. **预期结果**:
   - 更新后的响应显示 `likeCount=2`
   - 点赞记录未丢失

### 性能和并发测试

#### 场景 14: 并发点赞测试
1. 同时有多个用户对同一评论点赞
2. **预期结果**:
   - 所有操作都应成功
   - 最终 `likeCount` 应等于点赞的用户数
   - 不应出现重复点赞

#### 场景 15: 频繁点赞/取消点赞
1. 同一用户快速连续地点赞和取消点赞同一评论
2. **预期结果**:
   - 所有操作都应正确处理
   - 最终状态应一致

## 数据完整性检查

### 检查点 1: 唯一约束验证
```sql
-- 查询是否存在同一用户对同一评论的多次点赞
SELECT user_id, comment_id, COUNT(*) as count
FROM likes
WHERE comment_id IS NOT NULL
GROUP BY user_id, comment_id
HAVING count > 1;
-- 应返回空结果
```

### 检查点 2: 外键约束验证
```sql
-- 查询是否存在指向不存在评论的点赞记录
SELECT l.id, l.comment_id
FROM likes l
LEFT JOIN comments c ON l.comment_id = c.id
WHERE l.comment_id IS NOT NULL AND c.id IS NULL;
-- 应返回空结果
```

### 检查点 3: 点赞目标验证
```sql
-- 查询违反"只能点赞文章或评论其中之一"约束的记录
-- 注意：应用层代码会确保此约束，但可以验证
SELECT id, post_id, comment_id
FROM likes
WHERE (post_id IS NULL AND comment_id IS NULL) 
   OR (post_id IS NOT NULL AND comment_id IS NOT NULL);
-- 应返回空结果（应用层确保数据完整性）
```

## 日志验证

在测试过程中，检查应用日志应包含以下信息：
- 用户点赞评论的记录（包括用户名、评论ID、当前点赞数）
- 用户取消点赞的记录
- 重复点赞/取消点赞的提示信息
- 任何异常或错误的详细堆栈信息

## 潜在问题和注意事项

### 1. 数据库迁移
- ✅ **自动迁移**：Hibernate会在应用启动时自动更新数据库表结构
- ✅ **数据安全**：现有数据会被保留，Hibernate只添加缺失的列和约束
- ✅ **无需手动操作**：不需要执行SQL脚本，直接重启应用即可
- ⚠️ **可选CHECK约束**：
  - Hibernate无法自动创建CHECK约束
  - 如需数据库层面的额外保护，可在首次启动后手动添加
  - 需要 MySQL 8.0.16 或更高版本
  - 即使不添加，应用层代码也会确保数据完整性

### 2. 性能考虑
- **当前实现存在 N+1 查询问题**：获取评论列表时，每个评论都会额外查询点赞数和用户点赞状态
- 对于少量评论（<20条）的场景，性能影响可接受
- 对于大量评论的场景，建议优化方案：
  - 方案1：使用批量查询，一次性获取所有评论的点赞信息
  - 方案2：在数据库层面使用 JOIN 查询
  - 方案3：引入缓存（Redis）存储点赞计数
  - 方案4：在 comments 表添加 like_count 冗余字段（需要维护一致性）
- 已添加 `comment_id` 索引优化查询性能
- 建议监控 API 响应时间，特别是 `GET /api/posts/{postId}/comments` 端点

### 3. 事务一致性
- 确保点赞操作的事务性（已使用 `@Transactional` 注解）
- 测试数据库连接中断时的行为

### 4. API 兼容性
- 现有前端调用 `GET /api/posts/{postId}/comments` 的地方需要更新以处理新的 `likeCount` 和 `isLiked` 字段
- 确认旧版本客户端能够忽略新增字段而不报错

## 测试工具推荐

- **Postman/Insomnia**: 用于手动 API 测试
- **JMeter/K6**: 用于性能和并发测试
- **MySQL Workbench/DBeaver**: 用于数据库验证

## 已知限制和未来优化

### 当前实现的限制

1. **N+1 查询问题**
   - 获取评论列表时，每个评论都会单独查询点赞信息
   - 对于每页 20 条评论，会产生约 40 次额外的数据库查询
   - 适用场景：评论数量较少（<20条/页）的中小型应用

2. **缺少批量操作 API**
   - 当前不支持批量查询多个评论的点赞状态
   - 前端需要多次调用 API 才能获取多个评论的点赞信息

### 未来优化建议

1. **性能优化**
   - 实现批量查询接口：`POST /api/comments/likes/batch` 接受评论ID列表
   - 在 Service 层使用 `IN` 查询批量获取点赞信息
   - 引入缓存层（Redis）缓存点赞计数，减少数据库压力

2. **数据一致性增强**
   - 在应用层添加验证逻辑，确保 Like 记录只能关联文章或评论之一
   - 添加单元测试和集成测试覆盖所有边界情况

3. **监控和告警**
   - 添加慢查询监控
   - 监控点赞相关 API 的响应时间和错误率
   - 设置性能基线和告警阈值

## 总结

完成上述测试场景后，应确保：
1. ✅ 基本点赞/取消点赞功能正常工作
2. ✅ 幂等性保证（重复操作不会产生副作用）
3. ✅ 权限控制正确（未登录用户不能点赞）
4. ✅ 异常处理完善（不存在的评论、已删除的评论等）
5. ✅ 数据完整性保持（唯一约束、外键约束、CHECK约束）
6. ✅ 级联删除正常工作
7. ✅ API 响应一致性（所有返回评论的地方都包含点赞信息）
8. ✅ 性能可接受（特别是在高负载下）

**注意**：当前实现优先保证功能正确性和代码简洁性，对于高流量场景需要进行性能优化。
