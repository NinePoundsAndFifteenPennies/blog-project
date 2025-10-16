# 评论功能快速开始指南

## 🚀 快速开始

### 1. 启动应用

```bash
cd backend/blog
mvn spring-boot:run
```

如果遇到网络问题，可以尝试使用国内Maven镜像。

### 2. 数据库准备

确保MySQL数据库正在运行，JPA会自动创建 `comments` 表。

如果需要手动创建，执行：

```sql
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (post_id) REFERENCES posts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. 快速测试

#### 准备工作
1. 确保有已注册的用户账号
2. 确保有已发布的文章（`draft = false`）
3. 获取JWT token（登录后获得）

#### 测试创建评论

```bash
# 替换 {postId} 为真实的文章ID
# 替换 {your_jwt_token} 为你的JWT token

curl -X POST http://localhost:8080/api/posts/{postId}/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {your_jwt_token}" \
  -d '{
    "content": "这是一条测试评论"
  }'
```

#### 测试获取评论列表

```bash
# 获取文章的所有评论（无需登录）
curl http://localhost:8080/api/posts/{postId}/comments?page=0&size=10
```

#### 测试获取我的评论

```bash
# 获取当前用户的所有评论
curl http://localhost:8080/api/comments/my?page=0&size=10 \
  -H "Authorization: Bearer {your_jwt_token}"
```

#### 测试更新评论

```bash
# 更新评论（仅评论作者）
curl -X PUT http://localhost:8080/api/comments/{commentId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {your_jwt_token}" \
  -d '{
    "content": "更新后的评论内容"
  }'
```

#### 测试删除评论

```bash
# 删除评论（评论作者或文章作者）
curl -X DELETE http://localhost:8080/api/comments/{commentId} \
  -H "Authorization: Bearer {your_jwt_token}"
```

## 📝 API端点速查

| HTTP方法 | 路径 | 描述 | 是否需要登录 |
|---------|------|------|-------------|
| POST | `/api/posts/{postId}/comments` | 创建评论 | ✅ 是 |
| GET | `/api/posts/{postId}/comments` | 获取文章评论 | ❌ 否 |
| GET | `/api/comments/my` | 获取我的评论 | ✅ 是 |
| PUT | `/api/comments/{commentId}` | 更新评论 | ✅ 是（仅作者）|
| DELETE | `/api/comments/{commentId}` | 删除评论 | ✅ 是（作者或文章作者）|

## 🔍 常见问题

### Q1: 为什么无法评论？
**A**: 检查以下几点：
- 是否已登录（有JWT token）
- 文章是否为已发布状态（`draft = false`）
- 文章ID是否正确

### Q2: 为什么评论内容保存失败？
**A**: 检查评论内容长度是否在1-3000字符之间。

### Q3: 为什么无法删除评论？
**A**: 只有以下两种情况可以删除评论：
- 你是评论的作者
- 你是评论所在文章的作者

### Q4: 删除文章后评论会怎样？
**A**: 删除文章时会自动删除该文章的所有评论。

### Q5: 文章的评论数在哪里显示？
**A**: 所有返回文章信息的API现在都会包含 `commentCount` 字段。

## 📚 完整文档

- **API文档**: `docs/comment-api.md` - 详细的API接口说明
- **实现文档**: `docs/comment-implementation.md` - 技术实现细节
- **功能总结**: `docs/COMMENT_FEATURE_SUMMARY.md` - 完整的功能说明

## 🎯 前端集成提示

### 评论列表组件
```javascript
// 获取评论列表
fetch(`/api/posts/${postId}/comments?page=0&size=20`)
  .then(response => response.json())
  .then(data => {
    // data.content 是评论数组
    // data.totalElements 是总评论数
  });
```

### 创建评论
```javascript
// 创建评论（需要JWT token）
fetch(`/api/posts/${postId}/comments`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${jwtToken}`
  },
  body: JSON.stringify({
    content: '评论内容'
  })
})
.then(response => response.json())
.then(comment => {
  // 评论创建成功
});
```

### 显示评论数
```javascript
// 文章响应中已包含 commentCount
<div>评论数: {post.commentCount}</div>
```

## 🔐 权限规则

| 操作 | 未登录用户 | 普通用户 | 评论作者 | 文章作者 |
|------|-----------|---------|---------|---------|
| 查看评论 | ✅ | ✅ | ✅ | ✅ |
| 创建评论 | ❌ | ✅ | ✅ | ✅ |
| 编辑评论 | ❌ | ❌ | ✅ | ❌ |
| 删除评论 | ❌ | ❌ | ✅ | ✅ |

## 📊 数据库索引建议

为了提高查询性能，建议添加以下索引：

```sql
CREATE INDEX idx_comment_post ON comments(post_id);
CREATE INDEX idx_comment_user ON comments(user_id);
CREATE INDEX idx_comment_created_at ON comments(created_at);
```

## 🚨 注意事项

1. **评论内容长度**: 1-3000字符
2. **草稿文章**: 不能评论草稿文章
3. **级联删除**: 删除文章会删除所有评论
4. **编辑标记**: 编辑评论后 `updatedAt` 会自动更新
5. **排序方式**: 当前按创建时间升序（最早的在前），后续可调整

## 🔧 故障排查

### 编译失败
- 检查网络连接
- 尝试使用Maven离线模式：`mvn -o spring-boot:run`
- 清理并重新编译：`mvn clean install`

### 数据库错误
- 确认MySQL服务正在运行
- 检查 `application.properties` 数据库配置
- 验证表是否正确创建

### 权限错误
- 确认JWT token有效
- 检查用户是否有相应权限
- 查看后端日志获取详细错误信息

## 📞 获取帮助

- 查看详细API文档：`docs/comment-api.md`
- 查看实现细节：`docs/comment-implementation.md`
- 在GitHub提issue
- 查看后端日志：`logs/spring.log`

---

**版本**: v1.0  
**最后更新**: 2025-10-16  
**状态**: ✅ 生产就绪
