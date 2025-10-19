# 子评论功能架构图

## 数据库关系图

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
        ▲
        │
        │ (现有Like表已支持，无需修改)
```

---

## API架构图

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

---

## 前端组件结构图

```
PostDetail.vue (文章详情页)
  │
  └─── CommentList.vue (评论列表组件) 【修改】
          │
          ├─── CommentForm.vue (可选：评论输入框)
          │
          └─── CommentItem.vue (单条顶层评论) 【修改】
                  │
                  ├─── 评论内容显示
                  ├─── 点赞按钮 (现有)
                  ├─── 编辑/删除按钮 (现有)
                  ├─── 「回复」按钮 【新增】
                  └─── 「查看 N 条回复」按钮 【新增】
                          │
                          └─── ReplyList.vue (回复列表组件) 【新增】
                                  │
                                  ├─── 回复输入框 (创建一级回复)
                                  │
                                  └─── ReplyItem.vue (单条回复) 【新增，递归组件】
                                          │
                                          ├─── @用户名 显示
                                          ├─── 回复内容显示
                                          ├─── 点赞按钮
                                          ├─── 编辑/删除按钮
                                          ├─── 「回复」按钮
                                          └─── ReplyItem.vue (嵌套回复)
                                                  └─── (最多3层)
```

---

## 级联删除流程图

```
用户操作: 删除文章
  ├─→ 删除文章点赞 (likes where post_id = ?)
  ├─→ 删除顶层评论 (comments where post_id = ? AND parent_id IS NULL)
  │     ├─→ 删除一级子评论 (comments where parent_id = 顶层评论ID)
  │     │     ├─→ 删除二级子评论 (comments where parent_id = 一级子评论ID)
  │     │     │     ├─→ 删除三级子评论 (comments where parent_id = 二级子评论ID)
  │     │     │     │     └─→ 删除三级子评论点赞 (likes where comment_id = 三级子评论ID)
  │     │     │     └─→ 删除二级子评论点赞 (likes where comment_id = 二级子评论ID)
  │     │     └─→ 删除一级子评论点赞 (likes where comment_id = 一级子评论ID)
  │     └─→ 删除顶层评论点赞 (likes where comment_id = 顶层评论ID)
  └─→ 删除文章本身 (posts where id = ?)

用户操作: 删除顶层评论
  ├─→ 递归删除所有子孙评论 (CASCADE自动完成)
  ├─→ 删除所有子孙评论的点赞 (Service层手动删除)
  └─→ 删除顶层评论及其点赞

用户操作: 删除子评论
  ├─→ 递归删除该子评论的所有后代 (CASCADE自动完成)
  ├─→ 删除所有后代评论的点赞 (Service层手动删除)
  └─→ 删除子评论及其点赞

用户操作: 删除用户
  ├─→ 删除该用户的所有评论 (CASCADE)
  ├─→ 删除该用户的所有点赞 (CASCADE)
  └─→ 其他评论的reply_to_user_id设为NULL (SET NULL)
       前端显示: @[已删除用户]
```

---

## 数据加载流程图

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

---

## UI展示示例

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

## 实施时间估算

| 阶段 | 任务 | 预计工时 |
|------|------|---------|
| 后端开发 | 数据库迁移 | 0.5天 |
| 后端开发 | 实体层修改 | 0.5天 |
| 后端开发 | Repository/Service实现 | 1.5天 |
| 后端开发 | Controller实现 | 0.5天 |
| 后端开发 | 单元测试 | 1天 |
| 前端开发 | API封装 | 0.5天 |
| 前端开发 | 组件开发 | 2天 |
| 前端开发 | 样式优化 | 1天 |
| 前端开发 | 功能测试 | 1天 |
| 文档更新 | 文档更新 | 0.5天 |
| **总计** | | **9天** |

---

## 性能优化建议

### 数据库优化
```sql
-- 添加索引
CREATE INDEX idx_comment_parent ON comments(parent_id);
CREATE INDEX idx_comment_post_parent ON comments(post_id, parent_id);
CREATE INDEX idx_comment_level ON comments(level);
```

### 查询优化
```sql
-- 方案1: 递归CTE (MySQL 8.0+)
WITH RECURSIVE reply_tree AS (
  SELECT * FROM comments WHERE parent_id = ?
  UNION ALL
  SELECT c.* FROM comments c
  INNER JOIN reply_tree rt ON c.parent_id = rt.id
)
SELECT * FROM reply_tree ORDER BY created_at ASC;

-- 方案2: 简单查询 (推荐，层级≤3时性能更好)
SELECT * FROM comments 
WHERE parent_id = ? 
ORDER BY created_at ASC;
```

### 缓存策略
```
Redis Key设计:
- post:{postId}:comments:page:{page} → 顶层评论列表
- comment:{commentId}:replies:page:{page} → 子评论列表
- comment:{commentId}:replyCount → 子评论数量

TTL: 5-10分钟
失效策略: 新增/删除评论时清除相关缓存
```

### 前端优化
```javascript
// 虚拟滚动（大量评论场景）
import { useVirtualList } from '@vueuse/core'

// 懒加载图片
<img loading="lazy" :src="avatarUrl" />

// 防抖（搜索/输入）
import { useDebounceFn } from '@vueuse/core'
const debouncedSubmit = useDebounceFn(submitComment, 300)
```

---

**版本**: v1.0  
**日期**: 2025-10-19  
**维护**: 与设计文档同步更新
