# "记住我" 功能实现说明

## 核心实现思路

**"记住我"功能控制JWT过期时间：**
- **勾选"记住我"**：生成30天有效期的JWT token
- **未勾选"记住我"**：生成1小时有效期的JWT token
- **统一使用localStorage存储**，确保关闭浏览器后重新打开仍能保持登录（只要JWT未过期）
- **自动刷新机制**：用户活跃时（有API请求），如果token在5分钟内即将过期，自动刷新token

## 主要改动

### 后端修改

#### 1. LoginRequest.java
添加 `rememberMe` 字段，接收前端传来的"记住我"状态

#### 2. JwtTokenProvider.java
支持生成两种不同过期时间的token：
- **普通登录**：1小时 (3600000ms)
- **记住我登录**：30天 (2592000000ms)

#### 3. UserController.java
- 在登录时根据 `rememberMe` 参数生成相应的token
- **新增** `refreshToken` 端点：允许前端刷新即将过期的token

#### 4. application.properties
新增配置项：
```properties
# 普通Token有效期：1小时
app.jwt.expiration-ms=3600000
# "记住我"Token有效期：30天
app.jwt.remember-me-expiration-ms=2592000000
```

### 前端修改

#### 1. store/index.js
- 统一使用 `localStorage` 存储token和用户信息
- `rememberMe` 标志保存在localStorage中，用于token刷新时确定过期时间

#### 2. Login.vue
- 将 `formData.rememberMe` 传递给登录action
- 在组件挂载时检查URL查询参数中的message，显示token过期提示

#### 3. auth.js
- 将 `rememberMe` 参数发送到后端
- 新增 `refreshToken()` API函数，用于刷新token

#### 4. router/index.js
只检查 `localStorage` 中的token

#### 5. utils/request.js（核心改动）
- 安装并使用 `jwt-decode` 库解析JWT token
- 实现自动刷新机制：如果token在5分钟内过期，自动调用刷新接口
- 使用队列机制防止并发刷新
- 401错误时跳转登录页并显示提示消息

## 使用说明

### 场景1：勾选"记住我"登录

1. 用户勾选"记住我"复选框后登录
2. 后端生成有效期为30天的JWT token
3. 关闭浏览器后重新打开，用户仍然保持登录状态
4. 用户活跃时，如果token在5分钟内即将过期，自动刷新token
5. 直到30天后token过期且没有活跃操作刷新，或用户主动点击"退出登录"

### 场景2：不勾选"记住我"登录

1. 用户不勾选"记住我"复选框登录
2. 后端生成有效期为1小时的JWT token
3. 关闭浏览器后重新打开，如果JWT未过期，用户仍然保持登录状态
4. 用户活跃时，如果token在5分钟内即将过期，自动刷新token
5. 不活跃的情况下：1小时后token过期，需要重新登录

### JWT完全过期处理

如果token已经完全过期（后端返回401）：
1. 前端响应拦截器捕获401错误
2. 清除所有认证信息
3. 跳转到登录页面，显示"登录已过期，请重新登录"
4. 如果用户在编辑页面，redirect参数会保存当前路径，登录后自动返回


## 测试方法

### 测试1：验证"记住我"功能

1. 打开浏览器开发者工具 (F12) -> Application/存储 标签
2. 勾选"记住我"并登录
3. 检查 `localStorage` 中应该有 `token`、`user` 和 `rememberMe=true` 三个键
4. 关闭浏览器，重新打开，访问网站
5. **预期**：用户仍然保持登录状态（token未过期）

### 测试2：验证不勾选"记住我"但关闭浏览器后仍保持登录

1. 清除所有存储数据
2. 不勾选"记住我"并登录
3. 检查 `localStorage` 中应该有 `token`、`user` 和 `rememberMe=false`
4. **不等待token过期**，直接关闭浏览器
5. 立即重新打开浏览器，访问网站
6. **预期**：用户仍然保持登录状态（因为JWT未过期）

### 测试3：验证Token过期后跳转并显示消息

1. 修改 `application.properties` 中的 `app.jwt.expiration-ms=60000`（改为1分钟）
2. 重启后端服务器
3. 不勾选"记住我"并登录
4. 等待1分钟后，访问需要认证的页面（如个人中心 `/profile`）
5. **预期**：
   - 自动跳转到登录页
   - 显示红色错误消息："登录已过期，请重新登录"
   - URL包含 `?redirect=/profile&message=登录已过期，请重新登录`

### 测试4：验证自动刷新功能

1. 修改 `application.properties` 中的 `app.jwt.expiration-ms=360000`（改为6分钟）
2. 重启后端服务器
3. 不勾选"记住我"并登录
4. 打开浏览器开发者工具 -> Network标签
5. 等待约2分钟（token剩余4分钟，满足<5分钟条件）
6. 访问任意需要认证的页面或操作
7. **预期**：
   - 在Network标签中看到 `/api/users/refresh-token` 请求
   - 请求成功返回新token
   - localStorage中的token被更新
   - 操作正常完成，不会被打断

### 测试5：验证退出登录

1. 登录后点击"退出登录"
2. 检查 `localStorage` 应该被清空
3. **预期**：跳转到登录页面

## 后续优化建议

1. **滑动过期**：每次活跃操作都刷新token，实现真正的"活跃即有效"
2. **多标签页同步**：使用localStorage事件监听，在多个标签页间同步token更新
3. **Token黑名单**：服务端维护已撤销的token列表，增强安全性
4. **加密存储**：对localStorage中的敏感数据进行加密
5. **离线检测**：网络离线时暂停自动刷新，恢复后再刷新
