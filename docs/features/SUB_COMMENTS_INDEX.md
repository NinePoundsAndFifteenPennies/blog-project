# 子评论功能文档索引

本目录包含子评论（评论回复）功能的完整设计文档。

---

## 📖 文档导航

### 快速入门（按阅读顺序）

1. **[SUB_COMMENTS_SUMMARY.md](./SUB_COMMENTS_SUMMARY.md)** ⭐ 推荐首读
   - 快速概览（5-10分钟阅读）
   - 核心变更摘要
   - 关键设计决策
   - 实施步骤简化版

2. **[SUB_COMMENTS_DIAGRAMS.md](./SUB_COMMENTS_DIAGRAMS.md)** ⭐ 可视化理解
   - 数据库关系图
   - API架构图
   - 前端组件结构图
   - 级联删除流程图
   - UI展示示例

3. **[SUB_COMMENTS_DESIGN.md](./SUB_COMMENTS_DESIGN.md)** ⭐ 完整设计
   - 详细设计文档（30-60分钟阅读）
   - 10个章节，1300行
   - 涵盖所有技术细节
   - 实施指导

---

## 📊 文档对比

| 文档 | 目标读者 | 阅读时间 | 详细程度 | 用途 |
|------|---------|---------|---------|------|
| SUMMARY | 所有人 | 5-10分钟 | ⭐⭐ | 快速了解方案 |
| DIAGRAMS | 开发者、架构师 | 10-15分钟 | ⭐⭐⭐ | 可视化理解 |
| DESIGN | 开发者、实施者 | 30-60分钟 | ⭐⭐⭐⭐⭐ | 详细实施指南 |

---

## 🎯 按角色阅读指南

### 产品经理 / 项目经理
1. 阅读：SUMMARY（了解功能和实施时间）
2. 参考：DIAGRAMS（理解UI展示）

### 架构师 / 技术负责人
1. 阅读：SUMMARY + DIAGRAMS（快速评估）
2. 深入：DESIGN第1-5章（数据库、API、级联删除）
3. 关注：DESIGN第7章（扩展性设计）

### 后端开发者
1. 必读：DESIGN全文
2. 重点：第2章（数据库）、第3章（API）、第4章（业务逻辑）、第5章（级联删除）
3. 参考：DIAGRAMS（级联删除流程图）
4. 实施：DESIGN第8.1节（后端实施步骤）

### 前端开发者
1. 必读：SUMMARY + DIAGRAMS
2. 重点：DESIGN第3章（API）、第6章（前端展示）
3. 参考：DIAGRAMS（组件结构图、UI示例）
4. 实施：DESIGN第8.2节（前端实施步骤）

### 测试工程师
1. 阅读：SUMMARY（了解功能）
2. 重点：DESIGN第5章（级联删除）、第9章（风险）
3. 参考：SUMMARY（测试要点）
4. 制定：基于级联删除测试矩阵的测试用例

---

## 📋 设计概要

### 核心变更

#### 数据库（Comment表）
```sql
ALTER TABLE comments 
  ADD COLUMN parent_id BIGINT NULL,           -- 父评论ID
  ADD COLUMN reply_to_user_id BIGINT NULL,    -- 被@用户ID
  ADD COLUMN level INT NOT NULL DEFAULT 0;     -- 评论层级
```

#### API端点
```http
# 新增
POST   /api/comments/{commentId}/replies    # 创建子评论
GET    /api/comments/{commentId}/replies    # 获取子评论列表

# 修改
GET    /api/posts/{postId}/comments         # 只返回顶层评论
GET    /api/comments/my                     # 包含顶层+子评论
```

#### 前端组件
```
CommentList.vue (修改)
  └─ CommentItem.vue (修改)
      └─ ReplyList.vue (新增)
          └─ ReplyItem.vue (新增，递归)
```

---

## 🔗 相关文档链接

### 现有功能文档
- [评论功能](./COMMENTS.md) - 现有评论系统实现
- [点赞功能](./LIKES.md) - 点赞系统（支持子评论点赞）
- [API文档](../API.md) - 完整API接口文档
- [架构说明](../ARCHITECTURE.md) - 项目架构

### 主项目文档
- [README](../../README.md) - 项目主页
- [开发说明](../DEVELOPMENT.md) - 本地开发指南

---

## ⚙️ 实施建议

### 开发顺序
1. **后端数据库** → 添加字段、外键、索引
2. **后端实体层** → 修改Comment.java、Repository
3. **后端业务层** → Service、Controller实现
4. **后端测试** → 单元测试、集成测试
5. **前端组件** → ReplyList、ReplyItem开发
6. **前端集成** → 修改CommentItem、CommentList
7. **前端测试** → 功能测试、样式测试
8. **文档更新** → 更新COMMENTS.md、API.md

### 预计工时
- **后端开发**：4天
- **前端开发**：4天
- **测试优化**：1天
- **总计**：约9个工作日

---

## ❓ 常见问题

**Q: 需要修改Like表吗？**
A: 不需要。现有Like表已支持comment_id，可直接用于子评论点赞。

**Q: 最大嵌套层级是多少？**
A: 3层（level: 0→1→2→3）。这是在用户体验和性能之间的最佳平衡点。

**Q: 删除顶层评论会删除子评论吗？**
A: 会。通过外键CASCADE级联删除，Service层会先删除所有点赞记录。

**Q: 被@的用户删除账号后会怎样？**
A: reply_to_user_id设为NULL（外键SET NULL），前端显示"@[已删除用户]"。

**Q: 如何优化大量子评论的性能？**
A: 使用懒加载、分页、索引优化、Redis缓存。详见DESIGN第7章和DIAGRAMS文档。

---

## 📞 联系方式

如有疑问或建议，请：
1. 阅读完整设计文档
2. 查看相关文档链接
3. 提交Issue讨论

---

**文档版本**: v1.0  
**最后更新**: 2025-10-19  
**维护者**: 开发团队  
**状态**: ✅ 设计完成，待实施
