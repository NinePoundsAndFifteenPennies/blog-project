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
   - 如果已点赞，返回当前点赞数（HTTP 200，幂等操作）
   - 如果未点赞，创建新的点赞记录
4. 返回更新后的点赞数和状态

**注意**：重复点赞返回 200 OK 是幂等设计，确保多次相同操作结果一致。

### 取消点赞流程
1. 验证用户身份（必须登录）
2. 查找文章（不存在则抛出 404）
3. 检查是否已点赞
   - 如果未点赞，返回当前点赞数（HTTP 200，幂等操作）
   - 如果已点赞，删除点赞记录
4. 返回更新后的点赞数和状态

**注意**：重复取消点赞返回 200 OK 是幂等设计，确保多次相同操作结果一致。

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

首次运行时，JPA 会自动创建 `likes` 表。如果需要手动创建，可以使用以下 SQL（MySQL 语法）：

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

-- 注意：大多数数据库会自动为外键创建索引
-- 以下索引创建仅在需要额外优化时使用
-- CREATE INDEX idx_post_id ON likes(post_id);
-- CREATE INDEX idx_user_id ON likes(user_id);
```

## 前端实现

### 新增文件

1. **api/likes.js** - 点赞 API 封装
   - `likePost(postId)` - 点赞文章
   - `unlikePost(postId)` - 取消点赞
   - `getLikeInfo(postId)` - 获取点赞信息

### 修改文件

1. **components/PostCard.vue** - 文章卡片组件
   - 添加点赞按钮和点赞数显示
   - 实现点赞/取消点赞功能
   - 未登录用户点击跳转到登录页
   - 已点赞显示红色填充心形图标

2. **views/PostDetail.vue** - 文章详情页
   - 添加点赞按钮和点赞数显示
   - 实现点赞/取消点赞功能
   - 未登录用户点击跳转到登录页
   - 已点赞显示红色填充心形图标和"已点赞"文字

3. **views/Home.vue** - 首页列表
   - 从后端 API 获取 `likeCount` 和 `isLiked` 数据
   - 处理点赞状态变化事件
   - 实时更新列表中的点赞状态

### 用户体验

#### 点赞数量显示
- 所有用户（包括未登录用户）都可以查看点赞数量
- 点赞数量显示在心形图标旁边
- 数据从后端 API 实时获取

#### 点赞/取消点赞
- 登录用户点击心形图标可以点赞或取消点赞
- 操作后状态立即更新，无需刷新页面
- 点击一次点赞，再次点击取消点赞

#### 登录验证
- 未登录用户点击点赞按钮时自动跳转到登录页
- 显示提示信息："请先登录后再点赞"
- 登录成功后自动返回原页面

#### 视觉反馈
- **已点赞状态**：心形图标显示为红色填充（`fill="currentColor"`, `text-red-500`）
- **未点赞状态**：心形图标显示为灰色空心（`fill="none"`, `text-gray-400`）
- **悬停效果**：鼠标悬停时图标变为红色（`hover:text-red-500`）

### 前端实现逻辑

```javascript
// 点赞/取消点赞切换逻辑
const handleLike = async () => {
  // 检查登录状态
  if (!isLoggedIn.value) {
    router.push({
      path: '/login',
      query: {
        redirect: route.fullPath,
        message: '请先登录后再点赞'
      }
    })
    return
  }

  try {
    let response
    if (post.value.isLiked) {
      // 已点赞，取消点赞
      response = await unlikePost(post.value.id)
    } else {
      // 未点赞，点赞
      response = await likePost(post.value.id)
    }

    // 更新本地状态
    post.value.likeCount = response.likeCount
    post.value.isLiked = response.isLiked
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}
```

### 前端数据流

1. **页面加载**：Home.vue 获取文章列表时，后端返回包含 `likeCount` 和 `isLiked` 字段
2. **状态显示**：PostCard/PostDetail 根据 `isLiked` 显示对应的视觉效果
3. **用户操作**：用户点击点赞按钮
4. **权限验证**：检查用户登录状态，未登录则跳转
5. **API 调用**：调用 `likePost` 或 `unlikePost` API
6. **状态更新**：使用后端响应更新本地状态（`likeCount`、`isLiked`）
7. **UI 更新**：Vue 响应式系统自动更新 UI

### 技术特点

- **响应式更新**：使用 Vue 3 Composition API，状态变化自动反映到 UI
- **事件通信**：PostCard 通过 `emit` 向父组件通知点赞状态变化
- **统一错误处理**：使用 axios 拦截器统一处理 401 错误并跳转登录
- **用户体验优化**：未登录用户可以看到点赞数，点击时给予友好提示

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

### Q: 点赞状态在页面刷新后会保持吗？
A: 会的。点赞状态存储在数据库中，刷新页面后会从后端重新获取。

### Q: 在列表页点赞后，进入详情页状态是否一致？
A: 是的。点赞状态由后端统一管理，前端每次都从后端获取最新状态。
