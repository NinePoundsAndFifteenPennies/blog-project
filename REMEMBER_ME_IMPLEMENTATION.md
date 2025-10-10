# "记住我" 功能实现说明

## 问题背景

之前的实现存在以下问题：
1. 登录后不管有没有点击"记住我"，下次进网站都会默认已经登录
2. JWT过期之后需要退出再重新登录一次，不然所有需要JWT认证的操作都会失效

## 解决方案

### 核心实现思路

使用 **双存储策略**：
- **勾选"记住我"**：使用 `localStorage` 存储token和用户信息（持久化，关闭浏览器后仍然存在）
- **未勾选"记住我"**：使用 `sessionStorage` 存储token和用户信息（会话级别，关闭浏览器后自动清除）

### 后端修改

#### 1. LoginRequest.java
添加 `rememberMe` 字段，接收前端传来的"记住我"状态：
```java
private boolean rememberMe = false;
```

#### 2. JwtTokenProvider.java
支持生成两种不同过期时间的token：
- **普通登录**：1小时 (3600000ms)
- **记住我登录**：30天 (2592000000ms)

```java
public String generateToken(Authentication authentication, boolean rememberMe) {
    long expirationTime = rememberMe ? jwtRememberMeExpirationInMs : jwtExpirationInMs;
    // ...
}
```

#### 3. UserController.java
在登录时根据 `rememberMe` 参数生成相应的token：
```java
String jwt = tokenProvider.generateToken(authentication, loginRequest.isRememberMe());
```

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
实现双存储策略的核心逻辑：
- 根据 `rememberMe` 决定使用 `localStorage` 还是 `sessionStorage`
- 初始化时从两个存储中读取数据（向后兼容）
- 清除时从两个存储中都删除（确保完全清除）

#### 2. Login.vue
将 `formData.rememberMe` 传递给登录action：
```javascript
await store.dispatch('login', {
  username: formData.username,
  password: formData.password,
  rememberMe: formData.rememberMe  // 新增
})
```

#### 3. auth.js
将 `rememberMe` 参数发送到后端：
```javascript
data: {
    username: credentials.username,
    password: credentials.password,
    rememberMe: credentials.rememberMe || false
}
```

#### 4. router/index.js & utils/request.js
检查token时同时检查两个存储：
```javascript
const token = localStorage.getItem("token") || sessionStorage.getItem("token");
```

## 功能说明

### 场景1：勾选"记住我"登录

1. 用户勾选"记住我"复选框后登录
2. 后端生成有效期为30天的JWT token
3. 前端将token和用户信息存储到 `localStorage`
4. **关闭浏览器后重新打开**，用户仍然保持登录状态
5. 直到30天后token过期，或用户主动点击"退出登录"

### 场景2：不勾选"记住我"登录

1. 用户不勾选"记住我"复选框登录
2. 后端生成有效期为1小时的JWT token
3. 前端将token和用户信息存储到 `sessionStorage`
4. **关闭浏览器后**，sessionStorage被清除，用户需要重新登录
5. **不关闭浏览器**的情况下：
   - 1小时内：正常使用，无需重新登录
   - 1小时后：token过期，需要重新登录（自动跳转到登录页）

### JWT过期处理

无论是否勾选"记住我"，当JWT过期后：
1. 后端会返回401错误
2. 前端请求拦截器捕获401错误
3. 自动清除所有认证信息（从两个存储中清除）
4. 跳转到登录页面

## 测试方法

### 测试1：验证"记住我"功能

1. 打开浏览器开发者工具 (F12) -> Application/存储 标签
2. 勾选"记住我"并登录
3. 检查 `localStorage` 中应该有 `token`、`user` 和 `rememberMe` 三个键
4. 检查 `sessionStorage` 应该为空
5. 关闭浏览器，重新打开，访问网站
6. **预期**：用户仍然保持登录状态

### 测试2：验证不勾选"记住我"

1. 清除所有存储数据
2. 不勾选"记住我"并登录
3. 检查 `sessionStorage` 中应该有 `token` 和 `user`
4. 检查 `localStorage` 中只有 `rememberMe=false`
5. 关闭浏览器，重新打开，访问网站
6. **预期**：用户需要重新登录

### 测试3：验证Token过期

#### 测试短期token过期（1小时）
1. 不勾选"记住我"并登录
2. 修改 `application.properties` 中的 `app.jwt.expiration-ms=60000`（改为1分钟）
3. 重启后端服务器
4. 重新登录（不勾选记住我）
5. 等待1分钟后，访问需要认证的页面（如个人中心）
6. **预期**：自动跳转到登录页

#### 测试长期token过期（30天）
- 同上，但修改 `app.jwt.remember-me-expiration-ms` 的值

### 测试4：验证退出登录

1. 登录后点击"退出登录"
2. 检查 `localStorage` 和 `sessionStorage` 都应该被清空
3. **预期**：跳转到登录页面

## 技术细节

### 为什么rememberMe标志存在localStorage中？

```javascript
localStorage.setItem('rememberMe', rememberMe.toString())
```

这个标志用于在页面刷新时判断应该使用哪个存储来读取/写入用户信息。即使token存储在sessionStorage中，这个标志也需要存在localStorage中以便下次判断。

### 为什么初始化时检查两个存储？

```javascript
user: JSON.parse(getFromAnyStorage('user')) || null,
token: getFromAnyStorage('token') || null,
```

这是为了向后兼容和处理迁移：
1. 老用户可能已经在localStorage中存储了token
2. 用户可能在不同时间以不同方式登录，需要都能识别

### 为什么清除时要清除两个存储？

```javascript
function removeFromBothStorages(key) {
    localStorage.removeItem(key)
    sessionStorage.removeItem(key)
}
```

确保完全清除用户的认证信息，无论之前使用的是哪种存储方式。

## 注意事项

1. **浏览器兼容性**：sessionStorage在所有现代浏览器中都支持
2. **隐私模式**：某些浏览器的隐私模式可能会禁用localStorage，但sessionStorage通常仍可用
3. **跨标签页**：localStorage在同一域名的所有标签页间共享，sessionStorage独立于每个标签页
4. **安全性**：敏感信息不应存储在前端（JWT只包含用户标识符，不包含密码等敏感数据）

## 后续优化建议

1. **Refresh Token**：添加refresh token机制，在token即将过期时自动刷新
2. **Token刷新提示**：在token即将过期时提醒用户
3. **多设备管理**：允许用户查看和管理已登录的设备
4. **活跃度检测**：根据用户活跃度动态调整token有效期
