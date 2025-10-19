# 测试步骤 - 头像显示修复验证

## 问题回顾

之前的问题：
- ✅ 头像上传成功（200 OK）
- ❌ 头像图片加载失败（403 Forbidden）
- ❌ 即使在浏览器直接访问图片URL也无法查看

## 修复内容

### 修复1：前端URL处理
- **文件**：`frontend/src/utils/avatar.js`
- **作用**：开发环境使用相对路径，避免CORS错误

### 修复2：后端安全配置（关键修复）
- **文件**：`backend/blog/src/main/java/com/lost/blog/config/SecurityConfig.java`
- **修改**：添加 `.requestMatchers("/uploads/**").permitAll()`
- **作用**：允许匿名访问上传的文件，不再返回403错误

## 测试步骤

### 第一步：重启后端服务

**重要**：必须重启后端才能使SecurityConfig的修改生效！

```bash
# 停止当前运行的后端
Ctrl+C 或关闭窗口

# 重新启动后端
cd backend/blog
mvn spring-boot:run
```

### 第二步：启动前端（如果还没启动）

```bash
cd frontend
npm run serve
```

前端应该运行在 `http://localhost:3000`
后端应该运行在 `http://localhost:8080`

### 第三步：测试头像上传

1. 打开浏览器访问 `http://localhost:3000`
2. 登录系统
3. 进入个人中心（Profile）
4. 点击头像，选择一张图片
5. 上传头像

**预期结果**：
- ✅ 看到"头像更新成功"提示
- ✅ 个人中心的头像立即显示新图片

### 第四步：检查浏览器开发者工具

打开浏览器开发者工具（F12），切换到 **Network（网络）** 标签：

1. **上传请求** (POST 或 PUT)：
   ```
   请求: /api/files/upload/avatar 或 /api/files/update/avatar
   状态: 200 OK
   响应: {"avatarUrl": "/uploads/1/avatars/xxx.jpg"}
   ```

2. **保存请求** (POST)：
   ```
   请求: /api/users/me/avatar
   状态: 200 OK
   ```

3. **图片加载请求** (GET) - 这是关键！：
   ```
   请求: /uploads/1/avatars/xxx.jpg
   状态: ✅ 200 OK （之前是 ❌ 403 Forbidden）
   类型: image/jpeg 或 image/png
   ```

**检查点**：
- ✅ 所有请求状态码都是 200
- ✅ 没有 403 Forbidden 错误
- ✅ 没有 CORS 错误
- ✅ 图片请求能正常返回

### 第五步：测试主页显示

1. 点击"首页"或"Home"
2. 查看自己发表的文章

**预期结果**：
- ✅ 文章卡片上的作者头像显示为新上传的头像
- ✅ 不是默认头像（首字母）

### 第六步：测试直接访问

在浏览器地址栏直接输入头像URL：
```
http://localhost:3000/uploads/1/avatars/xxx.jpg
```
（从Network标签复制实际的URL）

**预期结果**：
- ✅ 浏览器能直接显示图片
- ✅ 不会返回403错误
- ✅ 不需要登录就能查看

### 第七步：测试游客访问

1. 退出登录（Logout）
2. 不登录的情况下，访问主页
3. 查看有头像的文章

**预期结果**：
- ✅ 游客用户能看到所有作者的头像
- ✅ 不会显示默认头像
- ✅ 图片正常加载

## 常见问题排查

### Q1: 还是看到403错误

**A**: 
1. 确认已经重启后端服务（这是最常见的遗漏）
2. 检查SecurityConfig.java文件是否真的包含了 `.requestMatchers("/uploads/**").permitAll()`
3. 清除浏览器缓存或使用无痕模式

### Q2: 看到CORS错误

**A**:
1. 检查前端是否运行在 `localhost:3000`
2. 检查后端是否运行在 `localhost:8080`
3. 检查vue.config.js中的proxy配置是否正确

### Q3: 头像上传成功但不显示

**A**:
1. 打开开发者工具的Network标签
2. 查找头像图片的请求
3. 检查状态码：
   - 如果是403：后端SecurityConfig未生效，需要重启
   - 如果是404：文件路径问题，检查uploads目录
   - 如果是CORS错误：前端URL处理问题

## 成功标志

如果看到以下所有情况，说明修复成功：

- ✅ 上传头像后，Profile页面立即显示新头像
- ✅ 主页上自己的文章显示新头像
- ✅ 浏览器Network标签中，头像图片请求返回200
- ✅ 没有403错误
- ✅ 没有CORS错误
- ✅ 游客（未登录）也能看到用户头像
- ✅ 直接在浏览器地址栏访问图片URL能正常显示

## 验证完成

如果所有测试都通过，说明头像显示功能已完全修复！

这个修复同时解决了两个关键问题：
1. **前端CORS问题** - 使用代理避免跨域
2. **后端权限问题** - 允许公开访问上传文件

现在头像功能应该能正常工作了。
