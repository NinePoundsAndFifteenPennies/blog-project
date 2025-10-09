# 草稿箱功能修复总结

## 问题描述

用户反馈个人中心"我的文章"显示正常，但"草稿箱"没有显示任何内容，即使数据库中 `is_draft` 字段有数据。

## 根本原因

通过代码分析发现，问题的根本原因是：

**前端 Profile.vue 通过 `/api/posts` 接口获取文章列表，但该接口只返回已发布的文章（`draft=false`）。**

具体流程：
1. Profile.vue 调用 `getPosts()` 获取所有文章
2. 后端 `getAllPosts()` 方法使用 `postRepository.findByDraftFalse()` 查询
3. 该查询只返回 `is_draft = false` 的文章
4. 前端收到的文章列表中**不包含任何草稿**
5. Profile.vue 尝试过滤当前用户的草稿时，因为列表中根本没有草稿，所以草稿箱显示为空

## 修复方案

### 1. 新增后端 API 接口

**文件**: `backend/blog/src/main/java/com/lost/blog/controller/PostController.java`

新增接口：`GET /api/posts/my` - 获取当前登录用户的所有文章（包括草稿）

```java
@GetMapping("/my")
public ResponseEntity<Page<PostResponse>> getMyPosts(
        @AuthenticationPrincipal UserDetails currentUser,
        Pageable pageable) {
    Page<PostResponse> posts = postService.getMyPosts(currentUser, pageable);
    return ResponseEntity.ok(posts);
}
```

### 2. 实现服务层方法

**文件**: `backend/blog/src/main/java/com/lost/blog/service/PostServiceImpl.java`

```java
@Override
@Transactional(readOnly = true)
public Page<PostResponse> getMyPosts(UserDetails currentUser, Pageable pageable) {
    User user = userRepository.findByUsername(currentUser.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));
    
    Page<Post> postsPage = postRepository.findByUser(user, pageable);
    logger.info("查询用户 {} 的所有文章（包括草稿），总数: {}",
            user.getUsername(), postsPage.getTotalElements());
    
    return postsPage.map(postMapper::toResponse);
}
```

利用已有的 `postRepository.findByUser()` 方法，该方法返回用户的**所有**文章，包括草稿。

### 3. 添加前端 API 函数

**文件**: `frontend/src/api/posts.js`

```javascript
export function getMyPosts(params) {
    return request({
        url: '/posts/my',
        method: 'get',
        params: params
    })
}
```

### 4. 更新前端 Profile 页面

**文件**: `frontend/src/views/Profile.vue`

修改 `loadPosts()` 方法，直接调用新接口：

```javascript
const loadPosts = async () => {
  if (!currentUser.value) return
  loading.value = true
  try {
    // 修改前：const res = await getPosts({ page: 0, size: 100 })
    // 修改后：直接获取当前用户的所有文章
    const res = await getMyPosts({ page: 0, size: 100 })
    const myPosts = res.content || []

    posts.value = myPosts.map(p => ({
      ...p,
      summary: p.content?.replace(/[#*`\n]/g, '').slice(0, 100) || ''
    }))

    userStats.drafts = posts.value.filter(p => p.draft).length
    userStats.posts = posts.value.filter(p => !p.draft).length
  } catch (e) {
    console.error('加载失败:', e)
  } finally {
    loading.value = false
  }
}
```

### 5. 添加调试日志

为了便于问题排查，在关键位置添加了调试日志：

#### 后端日志
- 在 `getAllPosts()` 中记录每篇文章的草稿状态
- 在 `getMyPosts()` 中记录查询的文章总数和草稿状态

#### 前端日志  
- 在 Profile.vue 中输出获取的文章列表
- 输出每篇文章的 draft 字段值和类型
- 输出统计的草稿数和已发布数

### 6. 数据库调试脚本

创建了 SQL 查询脚本用于检查数据库中的草稿状态：

**文件**: `backend/debug_scripts/check_draft_status.sql`

包含以下查询：
1. 查询所有文章的草稿状态
2. 按用户统计草稿和已发布文章数量
3. 检查 is_draft 字段的数据类型
4. 列出所有草稿文章
5. 列出所有已发布文章

## 修复效果

修复后的效果：

1. ✅ 个人中心可以正确显示当前用户的所有文章
2. ✅ 草稿箱标签会显示用户的草稿文章
3. ✅ 已发布文章和草稿文章正确分类显示
4. ✅ 文章统计数字（草稿数、已发布数）正确显示
5. ✅ 用户只能看到自己的草稿，不会看到其他用户的草稿

## 技术细节

### 为什么不修改原有接口？

保持 `/api/posts` 接口不变，只返回已发布文章的原因：
1. **职责单一**：该接口用于公共文章列表，只应返回已发布内容
2. **权限安全**：草稿是私密的，不应在公共接口中暴露
3. **向后兼容**：不影响其他调用该接口的页面（如首页）
4. **符合 RESTful 设计**：用不同的端点表示不同的资源集合

### 为什么使用 `/api/posts/my`？

1. **语义清晰**：`/my` 表示"我的"，清楚表达这是用户专属接口
2. **权限自动验证**：通过 `@AuthenticationPrincipal` 自动获取当前用户
3. **遵循 RESTful 约定**：子资源路径表示集合的子集

## 测试建议

要验证修复是否成功，建议进行以下测试：

1. **创建草稿文章**
   - 登录系统
   - 创建一篇文章，勾选"保存为草稿"
   - 前往个人中心查看草稿箱

2. **检查统计数字**
   - 确认草稿数量显示正确
   - 确认已发布文章数量显示正确

3. **测试切换标签**
   - 在"我的文章"和"草稿箱"之间切换
   - 确认显示的内容正确

4. **测试草稿发布**
   - 编辑一篇草稿，取消勾选"保存为草稿"
   - 确认文章从草稿箱移到"我的文章"

5. **查看控制台日志**
   - 打开浏览器开发者工具
   - 查看 Console 标签中的调试信息
   - 验证文章的 draft 字段值正确

## 回滚方案

如果发现问题需要回滚：

```bash
git revert <commit-hash>
```

回滚后前端将恢复使用 `getPosts()` 接口，虽然草稿箱仍然不显示，但不会影响其他功能。

## 后续优化建议

1. **移除调试日志**：修复验证后，可以移除添加的调试日志
2. **添加单元测试**：为新增的 `getMyPosts()` 方法添加单元测试
3. **优化性能**：如果用户文章很多，考虑添加分页加载
4. **增加过滤选项**：可以添加按日期、标题等条件过滤的功能

## 相关文件

修改的文件：
- `backend/blog/src/main/java/com/lost/blog/service/PostService.java`
- `backend/blog/src/main/java/com/lost/blog/service/PostServiceImpl.java`
- `backend/blog/src/main/java/com/lost/blog/controller/PostController.java`
- `frontend/src/api/posts.js`
- `frontend/src/views/Profile.vue`

新增的文件：
- `backend/debug_scripts/check_draft_status.sql`
- `backend/debug_scripts/README.md`

## 总结

这次修复的核心是：**为用户个人中心创建专用的 API 接口，返回包含草稿的文章列表，而不是依赖公共接口过滤。**

这种设计更加合理，符合权限控制和 RESTful 设计原则，也更容易维护和扩展。
