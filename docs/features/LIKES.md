# 点赞功能实现文档

## 概述

本文档描述了为博客系统添加的文章点赞功能的实现细节。

## 功能特性

- ✅ 用户可以对文章进行点赞
- ✅ 用户可以取消对文章的点赞
- ✅ 匿名用户可以查看点赞数量
- ✅ 登录用户可以查看自己的点赞状态
- ✅ 防止重复点赞（数据库唯一约束）
- ✅ 点赞信息自动包含在文章响应中

## 数据库设计

### Like 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 外键，关联 users 表 |
| post_id | BIGINT | 外键，关联 posts 表 |
| created_at | TIMESTAMP | 点赞时间 |

**约束:**
- UNIQUE(user_id, post_id) - 防止同一用户对同一文章重复点赞

## API 端点

### 1. 点赞文章
- **方法**: POST
- **路径**: `/api/posts/{postId}/likes`
- **认证**: 需要
- **响应**: `{ "likeCount": 5, "isLiked": true }`

### 2. 取消点赞
- **方法**: DELETE
- **路径**: `/api/posts/{postId}/likes`
- **认证**: 需要
- **响应**: `{ "likeCount": 4, "isLiked": false }`

### 3. 获取点赞信息
- **方法**: GET
- **路径**: `/api/posts/{postId}/likes`
- **认证**: 不需要（匿名用户可访问）
- **响应**: `{ "likeCount": 5, "isLiked": false }`

## 实现细节

### 新增文件

1. **model/Like.java** - 点赞实体类
   - 定义了用户和文章之间的多对多关系
   - 包含唯一约束防止重复点赞

2. **repository/LikeRepository.java** - 数据访问层
   - `existsByUserAndPost()` - 检查用户是否已点赞
   - `findByUserAndPost()` - 查找特定点赞记录
   - `countByPost()` - 统计文章点赞数
   - `deleteByUserAndPost()` - 删除点赞记录

3. **service/LikeService.java** - 服务接口
   - 定义点赞业务逻辑接口

4. **service/LikeServiceImpl.java** - 服务实现
   - 实现点赞、取消点赞、查询功能
   - 包含事务管理和日志记录

5. **controller/LikeController.java** - 控制器
   - 提供 RESTful API 端点
   - 处理 HTTP 请求和响应

6. **dto/LikeResponse.java** - 响应 DTO
   - 包含 likeCount 和 isLiked 两个字段

### 修改文件

1. **dto/PostResponse.java**
   - 添加 `likeCount` 字段 - 文章点赞数
   - 添加 `isLiked` 字段 - 当前用户是否已点赞

2. **mapper/PostMapper.java**
   - 注入 LikeService
   - 增加重载方法 `toResponse(Post, UserDetails)`
   - 在转换时自动填充点赞信息

3. **service/PostService.java** 和 **PostServiceImpl.java**
   - 更新 `getAllPosts()` 方法接受 currentUser 参数
   - 更新所有调用 mapper 的地方传递 currentUser

4. **controller/PostController.java**
   - 更新 `getAllPosts()` 方法接受 currentUser 参数

5. **config/SecurityConfig.java**
   - 添加规则允许匿名访问 GET `/api/posts/*/likes`

## 业务逻辑

### 点赞流程
1. 验证用户身份（必须登录）
2. 查找文章（不存在则抛出 404）
3. 检查是否已点赞
   - 如果已点赞，返回当前点赞数（不重复添加）
   - 如果未点赞，创建新的点赞记录
4. 返回更新后的点赞数和状态

### 取消点赞流程
1. 验证用户身份（必须登录）
2. 查找文章（不存在则抛出 404）
3. 检查是否已点赞
   - 如果未点赞，返回当前点赞数（无需操作）
   - 如果已点赞，删除点赞记录
4. 返回更新后的点赞数和状态

### 查询点赞信息流程
1. 查找文章（不存在则抛出 404）
2. 统计文章的总点赞数
3. 如果用户已登录，检查该用户是否已点赞
4. 返回点赞数和用户点赞状态

## 安全性

- 点赞和取消点赞操作需要用户认证
- 查看点赞信息不需要认证（公开）
- 使用数据库唯一约束防止重复点赞
- 所有操作都有事务保护

## 性能考虑

- 使用懒加载（FetchType.LAZY）关联关系
- 使用数据库索引（唯一约束自动创建索引）
- 只读操作使用 @Transactional(readOnly = true)

## 扩展性

此实现可以轻松扩展以支持：
- 点赞通知
- 点赞用户列表
- 点赞趋势分析
- 热门文章排序（基于点赞数）

## 测试建议

1. **单元测试**
   - LikeService 的各个方法
   - 边界条件（文章不存在、用户不存在等）

2. **集成测试**
   - API 端点的完整流程
   - 并发点赞场景
   - 事务回滚场景

3. **性能测试**
   - 大量点赞数据下的查询性能
   - 并发点赞/取消点赞操作

## 数据库迁移

首次运行时，JPA 会自动创建 `likes` 表。如果需要手动创建，可以使用以下 SQL：

```sql
CREATE TABLE likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_post (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE INDEX idx_post_id ON likes(post_id);
CREATE INDEX idx_user_id ON likes(user_id);
```

## 常见问题

### Q: 用户可以重复点赞吗？
A: 不可以。数据库有唯一约束，业务逻辑也会检查防止重复点赞。

### Q: 匿名用户可以点赞吗？
A: 不可以。点赞和取消点赞需要用户登录认证。

### Q: 匿名用户可以看到点赞数吗？
A: 可以。获取点赞信息的接口对所有用户开放。

### Q: 删除文章时点赞记录会被删除吗？
A: 会的。外键约束设置了 CASCADE 删除。

### Q: 删除用户时点赞记录会被删除吗？
A: 会的。外键约束设置了 CASCADE 删除。
