# 子评论功能设计摘要

> 完整设计文档请查看：[SUB_COMMENTS_DESIGN.md](./SUB_COMMENTS_DESIGN.md)

## 快速概览

### 核心变更

#### 1. 数据库变更（Comment表）

新增3个字段：

```sql
ALTER TABLE comments 
  ADD COLUMN parent_id BIGINT NULL,           -- 父评论ID
  ADD COLUMN reply_to_user_id BIGINT NULL,    -- 被@用户ID
  ADD COLUMN level INT NOT NULL DEFAULT 0;     -- 评论层级
```

#### 2. 新增API端点

```http
# 创建子评论
POST /api/comments/{commentId}/replies
{
  "content": "回复内容",
  "replyToUserId": 123  // 可选
}

# 获取子评论列表
GET /api/comments/{commentId}/replies?page=0&size=20
```

#### 3. 修改现有API

```http
# GET /api/posts/{postId}/comments
# 现在只返回顶层评论（parent_id = NULL）
# 每条评论新增replyCount字段
```

#### 4. 前端组件

```
CommentList.vue           （修改）
  ├─ CommentItem.vue      （修改：添加"查看回复"按钮）
  │   └─ ReplyList.vue    （新增：显示回复列表）
  │       └─ ReplyItem.vue（新增：显示单条回复，支持递归）
```

---

## 关键设计决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 子评论存储 | parent_id字段（邻接表模型） | 简单、灵活、支持递归 |
| 层级限制 | ≤3层 | 平衡用户体验和性能 |
| 加载策略 | 懒加载 | 减少初始加载时间 |
| 删除策略 | 级联删除（CASCADE） | 保证数据一致性 |
| UI布局 | 嵌套缩进显示 | 直观展示层级关系 |

---

## 数据模型

### CommentResponse字段（新增/修改）

```json
{
  "id": 100,
  "content": "回复内容",
  "parentId": 5,              // 新增：父评论ID（null=顶层）
  "replyToUserId": 123,       // 新增：被@用户ID
  "replyToUsername": "张三",   // 新增：被@用户名
  "level": 1,                 // 新增：评论层级（0=顶层）
  "replyCount": 5,            // 新增：子评论数量（仅顶层评论）
  "authorUsername": "李四",
  "createdAt": "2025-10-19T10:00:00",
  "likeCount": 3,
  "liked": true
}
```

---

## 级联删除关系

```
文章删除
  ↓
  ├─→ 顶层评论删除
  │     ↓
  │     ├─→ 一级子评论删除
  │     │     ↓
  │     │     └─→ 二级子评论删除
  │     │           ↓
  │     │           └─→ 点赞删除
  │     └─→ 点赞删除
  └─→ 文章点赞删除
```

---

## 实施步骤（简化版）

### 后端（7步）

1. ✅ 数据库迁移（添加3个字段）
2. ✅ 修改Comment实体类
3. ✅ 扩展CommentResponse DTO
4. ✅ 修改CommentRepository（添加查询方法）
5. ✅ 实现Service层（createReply、getReplies、级联删除）
6. ✅ 扩展Controller（添加2个端点）
7. ✅ 编写测试

### 前端（5步）

1. ✅ 扩展API封装（createReply、getReplies）
2. ✅ 创建ReplyItem.vue组件（显示单条回复）
3. ✅ 创建ReplyList.vue组件（显示回复列表）
4. ✅ 修改CommentItem.vue（添加"查看回复"按钮）
5. ✅ 样式优化（嵌套缩进、响应式）

---

## 性能优化

1. **数据库索引**
   ```sql
   CREATE INDEX idx_comment_parent ON comments(parent_id);
   CREATE INDEX idx_comment_post_parent ON comments(post_id, parent_id);
   ```

2. **懒加载**
   - 初始只加载顶层评论
   - 点击"查看回复"时才加载子评论

3. **分页**
   - 顶层评论：10条/页
   - 子评论：20条/页

4. **缓存**（可选）
   - Redis缓存顶层评论列表
   - TTL: 5-10分钟

---

## 扩展性

未来可支持的功能：

- ✅ **评论通知**：reply_to_user_id提供通知基础
- ✅ **评论审核**：添加status字段
- ✅ **评论搜索**：全文索引
- ✅ **评论排序**：按时间/热度排序
- ✅ **匿名评论**：user_id可为NULL

---

## 风险提示

| 风险 | 应对策略 |
|------|---------|
| 递归查询性能 | 限制层级≤3，使用索引 |
| 级联删除数据丢失 | 事务管理，二次确认 |
| 前端渲染性能 | 虚拟滚动，懒加载 |
| 用户滥用回复 | 频率限制，内容审核 |

---

## 测试要点

1. **功能测试**
   - 创建子评论（各层级）
   - 编辑子评论
   - 删除子评论（验证级联）
   - @用户功能

2. **级联删除测试**
   - 删除顶层评论（验证所有子评论被删除）
   - 删除文章（验证所有评论被删除）
   - 删除用户（验证reply_to_user_id设为NULL）

3. **性能测试**
   - 大量子评论场景（100+条）
   - 深层嵌套场景（3层）
   - 并发创建回复

---

## 文档索引

- **完整设计文档**：[SUB_COMMENTS_DESIGN.md](./SUB_COMMENTS_DESIGN.md)
- **评论功能文档**：[COMMENTS.md](./COMMENTS.md)
- **点赞功能文档**：[LIKES.md](./LIKES.md)
- **API接口文档**：[../API.md](../API.md)
- **架构说明**：[../ARCHITECTURE.md](../ARCHITECTURE.md)

---

**版本**：v1.0  
**日期**：2025-10-19  
**状态**：设计完成，待实施
