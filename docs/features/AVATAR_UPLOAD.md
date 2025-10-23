# 头像上传功能

本文档详细说明头像上传和管理功能的实现。

## 功能概述

头像上传系统支持用户上传和更新个人头像，具有以下特点：

- ✅ 按用户ID组织文件存储
- ✅ 首次上传限制，防止文件冗余
- ✅ 更新时自动删除旧文件
- ✅ 完善的文件验证机制
- ✅ JWT 认证保护

## 文件存储结构

用户上传的头像按用户 ID 组织，结构清晰：

```
uploads/
  ├── 1/
  │   └── avatars/
  │       └── abc123.jpg
  ├── 2/
  │   └── avatars/
  │       └── def456.png
  └── ...
```

### 优势

1. **用户隔离**: 每个用户的文件独立存储，便于管理
2. **可扩展性**: 未来可以在用户目录下添加其他类型文件（如文章图片、附件等）
3. **易于清理**: 删除用户时可以整体清理其文件目录
4. **安全性**: 文件路径与用户 ID 关联，防止越权访问

## API 接口

### 1. 上传头像（首次）

**端点:** `POST /api/files/upload/avatar`

**用途:** 用户首次上传头像

**限制:** 
- 需要 JWT 认证
- 用户必须没有头像（如果已有头像，必须使用更新接口）

**请求示例:**
```http
POST /api/files/upload/avatar
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [图片文件]
```

**成功响应:**
```json
{
  "avatarUrl": "/uploads/1/avatars/abc123.jpg"
}
```

**错误响应:**
```
"用户已有头像，请使用更新接口替换头像"
```

---

### 2. 更新头像

**端点:** `PUT /api/files/update/avatar`

**用途:** 更新用户头像

**功能:**
- 自动删除旧头像文件
- 上传并保存新头像
- 返回新头像 URL

**请求示例:**
```http
PUT /api/files/update/avatar
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [图片文件]
```

**成功响应:**
```json
{
  "avatarUrl": "/uploads/1/avatars/def456.jpg"
}
```

**说明:** 旧头像文件会被自动删除，不会占用额外空间。

---

### 3. 保存头像 URL

**端点:** `POST /api/users/me/avatar`

**用途:** 将头像 URL 保存到用户信息

**请求示例:**
```http
POST /api/users/me/avatar
Authorization: Bearer {token}
Content-Type: application/json

{
  "avatarUrl": "/uploads/1/avatars/abc123.jpg"
}
```

**成功响应:**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "avatarUrl": "/uploads/1/avatars/abc123.jpg"
}
```

## 文件验证规则

### 格式限制

- **支持格式**: JPG, JPEG, PNG
- **Content-Type**: `image/jpeg`, `image/png`

### 大小限制

- **最大文件大小**: 5 MB
- **配置位置**: `application.properties` 中的 `spring.servlet.multipart.max-file-size`

### 尺寸限制

- **最小尺寸**: 50 x 50 像素
- **最大尺寸**: 2000 x 2000 像素

## 工作流程

### 完整的头像上传流程

```
1. 用户选择图片文件
   ↓
2. 前端调用 uploadAvatar 或 updateAvatar 接口
   ↓
3. 后端验证：
   - JWT Token 是否有效
   - 文件格式是否支持
   - 文件大小是否符合要求
   - 图片尺寸是否在范围内
   - （uploadAvatar）用户是否已有头像
   ↓
4. 保存文件到 uploads/{userId}/avatars/
   ↓
5. （updateAvatar）删除旧头像文件
   ↓
6. 返回新头像 URL
   ↓
7. 前端调用 saveUserAvatar 接口保存 URL
   ↓
8. 更新用户信息中的 avatarUrl 字段
```

### 流程图

```
┌─────────┐
│ 选择文件 │
└────┬────┘
     │
     ▼
┌─────────────┐
│ 上传/更新头像 │
└────┬────────┘
     │
     ▼
┌──────────┐     验证失败     ┌──────────┐
│ 文件验证  │ ────────────────►│ 返回错误  │
└────┬─────┘                  └──────────┘
     │ 验证通过
     ▼
┌──────────┐
│ 保存文件  │
└────┬─────┘
     │
     ▼
┌──────────┐
│ 删除旧文件 │ (仅更新时)
└────┬─────┘
     │
     ▼
┌──────────┐
│ 返回 URL  │
└────┬─────┘
     │
     ▼
┌──────────┐
│ 保存到DB  │
└──────────┘
```

## 错误处理

系统提供明确的错误提示信息：

| 错误场景 | HTTP 状态码 | 错误信息 |
|---------|-----------|---------|
| 未登录 | 401 | "未登录或token无效，请先登录" |
| 文件为空 | 400 | "文件不能为空" |
| 文件过大 | 400 | "文件大小不能超过5MB" |
| 格式不支持 | 400 | "只支持JPG和PNG格式的图片" |
| 无效图片 | 400 | "无效的图片文件" |
| 尺寸太小 | 400 | "图片尺寸太小，最小尺寸为50x50像素" |
| 尺寸太大 | 400 | "图片尺寸太大，最大尺寸为2000x2000像素" |
| 已有头像 | 400 | "用户已有头像，请使用更新接口替换头像" |
| 服务器错误 | 500 | "文件上传失败: {错误详情}" |

## 实现细节

### 后端实现

#### FileController.java

控制器层负责：
- 接收 HTTP 请求
- 验证用户身份（JWT）
- 调用 Service 层处理业务逻辑
- 返回响应

```java
@PostMapping("/upload/avatar")
public ResponseEntity<?> uploadAvatar(
        @AuthenticationPrincipal UserDetails currentUser,
        @RequestParam("file") MultipartFile file) {
    // 验证身份并调用 Service
}

@PutMapping("/update/avatar")
public ResponseEntity<?> updateAvatar(
        @AuthenticationPrincipal UserDetails currentUser,
        @RequestParam("file") MultipartFile file) {
    // 验证身份并调用 Service
}
```

#### FileService.java & FileServiceImpl.java

服务层负责：
- 文件验证（格式、大小、尺寸）
- 检查用户是否已有头像
- 生成唯一文件名（UUID）
- 创建用户目录
- 保存文件
- 删除旧文件

关键方法：

1. `uploadAvatar()` - 首次上传，检查是否已有头像
2. `updateAvatar()` - 更新头像，删除旧文件
3. `deleteAvatar()` - 删除指定头像文件
4. `saveAvatarFile()` - 保存文件的核心逻辑

### 前端实现

#### 文件API模块 (frontend/src/api/files.js)

提供与后端文件上传接口交互的函数：

```javascript
// 首次上传头像
export async function uploadAvatar(file)

// 更新已有头像
export async function updateAvatar(file)

// 保存头像URL到用户信息
export async function saveUserAvatar(avatarUrl)
```

**实现细节：**
- 使用 `FormData` 封装文件数据
- 通过 axios 发送 multipart/form-data 请求
- 自动从 localStorage 获取 JWT token
- 返回后端响应的头像URL

#### Vuex Store 更新 (frontend/src/store/index.js)

添加了用户头像状态管理：

```javascript
mutations: {
  UPDATE_USER_AVATAR(state, avatarUrl) {
    // 更新state中的用户头像
    // 同步更新localStorage
  }
}
```

#### 个人资料页面 (frontend/src/views/Profile.vue)

**UI 特性：**
1. **动态头像显示**
   - 有头像：显示头像图片（`<img>`）
   - 无头像：显示用户名首字母（渐变背景）

2. **交互设计**
   - 点击头像触发文件选择
   - 悬停显示相机图标的半透明遮罩
   - 视觉提示用户可以上传/更换头像

3. **文件验证**
   - 接受格式：JPG、PNG
   - 大小限制：5MB
   - 前端验证，及时反馈错误

**核心功能函数：**

```javascript
// 触发文件选择
triggerFileInput()

// 验证图片文件
validateImageFile(file)

// 处理文件选择
handleFileSelect(event) {
  // 1. 验证文件
  // 2. 判断用户是否已有头像
  // 3. 调用相应API（uploadAvatar 或 updateAvatar）
  // 4. 调用 saveUserAvatar 保存URL
  // 5. 更新Vuex状态
  // 6. 显示成功/失败消息
}
```

**完整流程：**

```
用户点击头像
    ↓
触发文件选择器
    ↓
用户选择图片文件
    ↓
前端验证（类型、大小）
    ↓
调用 uploadAvatar 或 updateAvatar
    ↓
后端验证并保存文件
    ↓
返回头像URL
    ↓
调用 saveUserAvatar 保存URL
    ↓
更新Vuex状态（UPDATE_USER_AVATAR）
    ↓
页面自动刷新显示新头像
```

**错误处理：**
- 文件类型错误："只支持 JPG 和 PNG 格式的图片"
- 文件过大："图片大小不能超过 5MB"
- 后端错误：显示后端返回的错误信息
- 网络错误："头像上传失败，请重试"

## 测试建议

### 功能测试

1. **首次上传测试**
   - 使用新用户账号
   - 上传符合要求的图片
   - 验证返回的 URL 正确
   - 验证文件确实保存在正确位置

2. **重复上传测试**
   - 使用已有头像的用户
   - 调用 uploadAvatar 接口
   - 应返回错误："用户已有头像，请使用更新接口替换头像"

3. **更新头像测试**
   - 使用已有头像的用户
   - 记录旧头像文件路径
   - 调用 updateAvatar 接口
   - 验证返回新的 URL
   - 验证旧文件已被删除
   - 验证新文件已保存

4. **文件验证测试**
   - 上传过大的文件（>5MB）
   - 上传不支持的格式（如 GIF、BMP）
   - 上传尺寸不符的图片（过小或过大）
   - 验证所有错误都返回明确的错误信息

5. **认证测试**
   - 不带 Token 调用接口
   - 使用无效的 Token
   - 使用过期的 Token
   - 验证都返回 401 和明确的错误信息

### 性能测试

1. 并发上传测试
2. 大文件上传测试
3. 频繁更新头像测试

## 安全考虑

1. **认证保护**: 所有接口都需要 JWT 认证
2. **文件类型验证**: 严格限制文件类型，防止上传恶意文件
3. **文件大小限制**: 防止上传超大文件消耗服务器资源
4. **用户隔离**: 文件按用户 ID 存储，防止用户间文件访问
5. **唯一文件名**: 使用 UUID 生成文件名，防止文件名冲突和猜测

## 优化建议

### 当前实现

- ✅ 本地文件系统存储
- ✅ 按用户分类
- ✅ 自动清理旧文件

### 未来优化

- 🔲 使用对象存储服务（OSS/S3）
- 🔲 图片压缩和格式转换
- 🔲 生成不同尺寸的缩略图
- 🔲 CDN 加速
- 🔲 防盗链保护
- 🔲 定期清理未使用的文件

## 常见问题

### Q1: 为什么要区分上传和更新接口？

**A:** 主要出于以下考虑：
1. **防止文件冗余**: 限制首次上传，避免多次调用产生大量无用文件
2. **明确的操作语义**: 上传和更新是不同的业务操作
3. **更好的错误提示**: 可以明确告诉用户应该使用哪个接口

### Q2: 旧文件删除失败会影响新文件上传吗？

**A:** 不会。删除旧文件的异常被捕获但不会抛出，确保新文件上传不受影响。但会记录错误日志以便排查问题。

### Q3: 可以支持其他图片格式吗？

**A:** 可以，只需在 `FileServiceImpl` 中修改 `ALLOWED_EXTENSIONS` 和 `ALLOWED_CONTENT_TYPES` 常量即可。

### Q4: 文件大小限制可以调整吗？

**A:** 可以，在 `application.properties` 中修改 `spring.servlet.multipart.max-file-size` 配置。

## 更新日志

### 2025-10-15

- ✅ 实现前端头像上传和更新功能
- ✅ 创建文件API模块 (files.js)
- ✅ 更新个人资料页面支持头像上传
- ✅ 添加Vuex状态管理集成
- ✅ 实现点击头像上传交互
- ✅ 添加悬停效果和视觉反馈
- ✅ 前端文件验证（类型、大小）
- ✅ 智能选择首次上传或更新接口
- ✅ 完整的错误处理和用户提示

### 2025-10-14

- ✅ 实现按用户分类的文件存储结构
- ✅ 添加首次上传限制
- ✅ 实现头像更新接口，自动删除旧文件
- ✅ 增强错误处理，提供明确的错误信息
- ✅ 所有接口添加 JWT 认证保护
