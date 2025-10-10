# "记住我" 功能实现说明

## 问题背景

之前的实现存在以下问题：
1. 登录后不管有没有点击"记住我"，下次进网站都会默认已经登录
2. JWT过期之后需要退出再重新登录一次，不然所有需要JWT认证的操作都会失效

## 解决方案（已更新）

### 核心实现思路

**"记住我"功能控制JWT过期时间，而非浏览器会话：**
- **勾选"记住我"**：生成30天有效期的JWT token
- **未勾选"记住我"**：生成1小时有效期的JWT token
- **两种情况都使用localStorage存储**，确保关闭浏览器后重新打开仍能保持登录（只要JWT未过期）
- **自动刷新机制**：用户活跃时（有API请求），如果token在5分钟内即将过期，自动刷新token

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
- 在登录时根据 `rememberMe` 参数生成相应的token
- **新增** `refreshToken` 端点：允许前端刷新即将过期的token

```java
@PostMapping("/refresh-token")
public ResponseEntity<?> refreshToken(@AuthenticationPrincipal UserDetails currentUser,
                                     @RequestBody(required = false) LoginRequest refreshRequest) {
    // 根据rememberMe标志生成新token
    boolean rememberMe = refreshRequest != null && refreshRequest.isRememberMe();
    String jwt = tokenProvider.generateToken(authentication, rememberMe);
    return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
}
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
- 只使用 `localStorage` 存储token和用户信息
- 移除了sessionStorage相关逻辑
- `rememberMe` 标志保存在localStorage中，用于token刷新时确定过期时间

#### 2. Login.vue
- 将 `formData.rememberMe` 传递给登录action
- **新增**：在组件挂载时检查URL查询参数中的message，显示token过期提示

```javascript
onMounted(() => {
  if (route.query.message) {
    errorMessage.value = route.query.message
  }
})
```

#### 3. auth.js
- 将 `rememberMe` 参数发送到后端
- **新增**：`refreshToken()` API函数，用于刷新token

#### 4. router/index.js
只检查 `localStorage` 中的token：
```javascript
const token = localStorage.getItem("token");
```

#### 5. utils/request.js（核心改动）
**新增依赖**：安装并使用 `jwt-decode` 库解析JWT token

**自动刷新机制**：
```javascript
// 检查token是否在5分钟内过期
const isTokenExpiringSoon = (token) => {
  const decoded = jwtDecode(token);
  const timeUntilExpiry = decoded.exp - (Date.now() / 1000);
  return timeUntilExpiry < 300; // 300秒 = 5分钟
};

// 在请求拦截器中自动刷新
request.interceptors.request.use(async (config) => {
  let token = localStorage.getItem("token");
  
  if (token && isTokenExpiringSoon(token)) {
    // 刷新token
    const newToken = await refreshAuthToken(rememberMe);
    token = newToken;
  }
  
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

**防止并发刷新**：使用队列机制，确保同时只有一个请求在刷新token，其他请求等待

**401错误处理**：
```javascript
case 401:
  store.commit('CLEAR_AUTH');
  router.push({ 
    path: "/login", 
    query: { 
      redirect: router.currentRoute.value.fullPath,
      message: "登录已过期，请重新登录" 
    } 
  });
```

## 功能说明

### 场景1：勾选"记住我"登录

1. 用户勾选"记住我"复选框后登录
2. 后端生成有效期为30天的JWT token
3. 前端将token和用户信息存储到 `localStorage`
4. **关闭浏览器后重新打开**，用户仍然保持登录状态
5. **用户活跃时**（有API请求），如果token在5分钟内即将过期，自动刷新token
6. 直到30天后token过期且没有活跃操作刷新，或用户主动点击"退出登录"

### 场景2：不勾选"记住我"登录

1. 用户不勾选"记住我"复选框登录
2. 后端生成有效期为1小时的JWT token
3. 前端将token和用户信息存储到 `localStorage`
4. **关闭浏览器后重新打开**，如果JWT未过期，用户仍然保持登录状态
5. **用户活跃时**（有API请求），如果token在5分钟内即将过期，自动刷新token
6. **不活跃的情况下**：
   - 1小时内：正常使用，无需重新登录
   - 1小时后且无活跃操作：token过期，需要重新登录

### 场景3：用户活跃期间写文章

1. 用户登录后开始写文章（未勾选"记住我"，1小时过期）
2. 写了50分钟，此时token还有10分钟过期
3. 用户点击保存或其他操作，触发API请求
4. **自动刷新逻辑**：请求拦截器检测到token在5分钟内过期
5. 自动调用 `/users/refresh-token` 刷新token
6. 新token有效期重新计算（1小时）
7. 用户继续写作，不会被打断

### JWT完全过期处理

如果token已经完全过期（后端返回401）：
1. 后端返回401错误
2. 前端响应拦截器捕获401错误
3. 清除所有认证信息
4. 跳转到登录页面，显示"登录已过期，请重新登录"
5. 如果用户在编辑页面，redirect参数会保存当前路径，登录后自动返回

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

#### 测试短期token过期（1小时）
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

## 技术细节

### 为什么使用localStorage而不是sessionStorage？

根据用户需求，即使未勾选"记住我"，也不应该在每次打开浏览器时都要重新登录。只要JWT未过期，用户就应该保持登录状态。

- `sessionStorage`：在关闭标签页/浏览器时清除
- `localStorage`：永久保存，除非主动清除或代码清除

因此，使用`localStorage`配合JWT过期时间来控制登录状态的持久性。

### 自动刷新的时机为什么是5分钟？

- **太早刷新**：会增加不必要的服务器请求
- **太晚刷新**：可能在用户操作过程中token过期，导致操作失败
- **5分钟**：平衡点，给予足够的缓冲时间，确保用户操作不会被打断

### 为什么需要防止并发刷新？

如果用户同时发起多个API请求，且token即将过期，可能会同时触发多个刷新请求。使用队列机制：
1. 第一个请求发起刷新，设置 `isRefreshing = true`
2. 后续请求检测到正在刷新，加入 `failedQueue`
3. 刷新成功后，处理队列中的所有请求
4. 避免了重复刷新和竞态条件

### JWT token的结构

JWT包含三部分：Header.Payload.Signature

使用 `jwt-decode` 可以解析Payload部分，获取：
- `exp`：过期时间（Unix时间戳，秒）
- `sub`：用户标识（username）
- `iat`：签发时间

## 注意事项

1. **安全性**：Token存储在localStorage中可能存在XSS风险，应确保网站有适当的XSS防护
2. **时间同步**：客户端和服务器时间应该同步，否则可能影响过期检测
3. **网络延迟**：刷新请求需要时间，5分钟的阈值考虑了网络延迟因素
4. **并发控制**：队列机制确保token刷新的原子性

## 后续优化建议

1. **滑动过期**：每次活跃操作都刷新token，实现真正的"活跃即有效"
2. **多标签页同步**：使用localStorage事件监听，在多个标签页间同步token更新
3. **Token黑名单**：服务端维护已撤销的token列表，增强安全性
4. **加密存储**：对localStorage中的敏感数据进行加密
5. **离线检测**：网络离线时暂停自动刷新，恢复后再刷新

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
