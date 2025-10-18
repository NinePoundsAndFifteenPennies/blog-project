# 评论功能实现文档

## 概述

本文档描述了为博客系统添加的文章评论功能的实现细节。

## 功能特性

- ✅ 登录用户可以对已发布的文章发表评论
- ✅ 支持纯文本和Markdown评论内容（前端渲染）
- ✅ 评论内容长度限制：1-3000字符
- ✅ 评论作者可以编辑自己的评论
- ✅ 评论作者可以删除自己的评论
- ✅ 文章作者可以删除自己文章下的任何评论
- ✅ 已登录用户可以查看自己的所有评论
- ✅ 支持分页查询评论列表
- ✅ 评论信息自动包含在文章响应中（评论数）
- ✅ 草稿文章不支持评论功能
- ✅ 删除文章时级联删除所有评论

## 数据库设计

### Comment 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| content | TEXT | 评论内容，1-3000字符 |
| user_id | BIGINT | 外键，关联 users 表 |
| post_id | BIGINT | 外键，关联 posts 表 |
| created_at | TIMESTAMP | 评论创建时间 |
| updated_at | TIMESTAMP | 评论更新时间（可为空）|

**约束:**
- user_id 和 post_id 为外键，关联到对应的表
- content 字段长度限制 1-3000 字符

## API 端点

### 1. 创建评论
- **方法**: POST
- **路径**: `/api/posts/{postId}/comments`
- **认证**: 需要
- **请求体**: `{ "content": "评论内容" }`
- **响应**: CommentResponse 对象（包含评论详情、作者信息、文章信息）

### 2. 更新评论
- **方法**: PUT
- **路径**: `/api/comments/{commentId}`
- **认证**: 需要（仅评论作者）
- **请求体**: `{ "content": "更新后的评论内容" }`
- **响应**: CommentResponse 对象（包含更新后的 updatedAt 时间）

### 3. 删除评论
- **方法**: DELETE
- **路径**: `/api/comments/{commentId}`
- **认证**: 需要（评论作者或文章作者）
- **响应**: `"评论删除成功"`

### 4. 获取文章的所有评论
- **方法**: GET
- **路径**: `/api/posts/{postId}/comments?page=0&size=20`
- **认证**: 不需要（公开访问）
- **响应**: 分页的 CommentResponse 列表

### 5. 获取我的评论
- **方法**: GET
- **路径**: `/api/comments/my?page=0&size=10`
- **认证**: 需要
- **响应**: 分页的 CommentResponse 列表

## 实现细节

### 新增文件

1. **model/Comment.java** - 评论实体类
   - 定义了用户和文章的多对一关系
   - 包含内容长度验证（1-3000字符）
   - 自动时间戳管理（createdAt, updatedAt）

2. **repository/CommentRepository.java** - 数据访问层
   - `findByPost()` - 查询文章的所有评论（分页）
   - `findByUser()` - 查询用户的所有评论（分页）
   - `countByPost()` - 统计文章的评论数
   - `deleteByPost()` - 删除文章的所有评论（级联删除）

3. **service/CommentService.java** - 服务接口
   - 定义评论业务逻辑接口

4. **service/CommentServiceImpl.java** - 服务实现
   - 实现评论的CRUD操作
   - 包含权限检查和草稿文章保护
   - 包含事务管理和日志记录

5. **controller/CommentController.java** - 控制器
   - 提供 RESTful API 端点
   - 处理 HTTP 请求和响应

6. **dto/CommentRequest.java** - 请求 DTO
   - 包含 content 字段和验证规则

7. **dto/CommentResponse.java** - 响应 DTO
   - 包含评论详情、作者信息、文章信息、时间戳

8. **mapper/CommentMapper.java** - 映射器
   - 将 Comment 实体转换为 CommentResponse

### 修改文件

1. **dto/PostResponse.java**
   - 添加 `commentCount` 字段 - 文章评论数

2. **mapper/PostMapper.java**
   - 注入 CommentService
   - 在转换时自动填充评论数

3. **service/PostServiceImpl.java**
   - 注入 CommentRepository 和 LikeRepository
   - 在 `deletePost()` 方法中添加级联删除逻辑
   - 删除顺序：点赞 → 评论 → 文章

4. **repository/LikeRepository.java**
   - 添加 `deleteByPost()` 方法支持级联删除

## 业务逻辑

### 创建评论流程
1. 验证用户身份（必须登录）
2. 查找文章（不存在则抛出 404）
3. 检查文章是否为草稿
   - 如果是草稿，抛出 403 错误
   - 如果不是草稿，继续
4. 创建评论记录
5. 返回评论响应

### 更新评论流程
1. 验证用户身份（必须登录）
2. 查找评论（不存在则抛出 404）
3. 检查权限（只有评论作者可以编辑）
   - 如果不是作者，抛出 403 错误
   - 如果是作者，继续
4. 更新评论内容
5. 自动更新 updatedAt 时间戳
6. 返回更新后的评论响应

### 删除评论流程
1. 验证用户身份（必须登录）
2. 查找评论（不存在则抛出 404）
3. 检查权限（评论作者或文章作者可以删除）
   - 如果不是评论作者也不是文章作者，抛出 403 错误
   - 如果满足条件，继续
4. 删除评论记录
5. 返回成功消息

### 查询评论流程
1. 查找文章（不存在则抛出 404）
2. 查询文章的所有评论（分页）
3. 评论按创建时间升序排列（最早的在前）
4. 返回分页的评论列表

### 级联删除流程
当删除文章时：
1. 先删除文章的所有点赞记录
2. 再删除文章的所有评论记录
3. 最后删除文章本身
4. 使用事务保证操作的原子性

## 安全性

- 创建、更新、删除评论需要用户认证
- 查看评论不需要认证（公开）
- 细粒度权限控制：
  - 评论编辑：仅限评论作者
  - 评论删除：评论作者或文章作者
- 草稿文章不支持评论功能
- 所有操作都有事务保护

## 性能考虑

- 使用懒加载（FetchType.LAZY）关联关系
- 分页查询避免大量数据加载
- 评论数统计使用 COUNT 查询
- 只读操作使用 @Transactional(readOnly = true)

## 扩展性

此实现可以轻松扩展以支持：
- 评论回复（添加 parent_id 字段）
- 评论点赞
- 评论排序（按热度、最新等）
- 评论审核（添加 status 字段）
- 富文本增强（@提及用户、表情包、代码高亮）
- 评论通知

## 测试建议

1. **创建评论**
   - 测试对已发布文章创建评论
   - 测试对草稿文章创建评论（应失败）
   - 测试未登录创建评论（应失败）
   - 测试评论内容长度验证

2. **更新评论**
   - 测试更新自己的评论
   - 测试更新他人评论（应失败）
   - 验证 updatedAt 字段更新

3. **删除评论**
   - 测试作者删除自己的评论
   - 测试文章作者删除评论
   - 测试无关用户删除评论（应失败）

4. **查询评论**
   - 测试分页功能
   - 测试评论排序
   - 测试空评论列表
   - 测试获取我的评论

5. **级联删除**
   - 测试删除文章时评论是否被删除
   - 测试删除有点赞和评论的文章
   - 测试事务回滚

6. **权限验证**
   - 测试各种权限组合
   - 测试JWT token验证

## 数据库迁移

首次运行时，JPA 会自动创建 `comments` 表。如果需要手动创建，可以使用以下 SQL（MySQL 语法）：

```sql
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 建议添加索引以优化查询性能
CREATE INDEX idx_comment_post ON comments(post_id);
CREATE INDEX idx_comment_user ON comments(user_id);
CREATE INDEX idx_comment_created_at ON comments(created_at);
```

## 常见问题

### Q: 未登录用户可以评论吗？
A: 不可以。创建、编辑、删除评论都需要用户登录认证。

### Q: 未登录用户可以看到评论吗？
A: 可以。查看评论列表的接口对所有用户开放。

### Q: 可以对草稿文章评论吗？
A: 不可以。只能对已发布的文章（draft = false）进行评论。

### Q: 谁可以删除评论？
A: 评论作者可以删除自己的评论，文章作者可以删除自己文章下的任何评论。

### Q: 删除文章时评论会被删除吗？
A: 会的。删除文章时会先删除所有点赞和评论，然后删除文章本身。

### Q: 编辑评论后如何标记？
A: 编辑评论后，系统会自动更新 `updatedAt` 字段，前端可以据此显示"已编辑"标记。

### Q: 评论列表如何排序？
A: 当前按创建时间升序排列（最早的评论在前），后续可根据需求调整排序方式。

### Q: 评论支持什么格式？
A: 评论内容支持纯文本和Markdown格式，由前端负责渲染。

### Q: 评论内容有长度限制吗？
A: 有的。评论内容长度限制为1-3000字符，后端会进行验证。
