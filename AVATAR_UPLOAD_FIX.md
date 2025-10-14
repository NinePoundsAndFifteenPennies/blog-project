# 头像上传错误修复 - 实现摘要

## 问题分析

头像上传功能尚未实现。用户所需：
1. `POST /api/files/upload/avatar` - 上传并返回头像 URL
2. `POST /api/users/me/avatar` - 将 URL 保存到数据库
3. `GET /api/users/me` - 返回包含 avatarUrl 的用户信息

## 已实现的解决方案

### 1. 数据库层 (User.java)
- 使用 `@Column(name = "avatar_url")` 添加 `avatarUrl` 字段
- 添加 getter 和 setter 方法

### 2. 文件上传端点 (FileController.java)
- **POST /api/files/upload/avatar**
- 图片格式验证：仅限 JPG 和 PNG
- 文件大小验证：最大 5MB
- 图片尺寸验证：50x50 到 2000x2000 像素
- 生成基于 UUID 的唯一文件名
- 保存到 `uploads/avatars/` 目录
- 返回 JSON： `{"avatarUrl": "/uploads/avatars/{filename}"}`

**重要提示：**响应字段为 `avatarUrl`（而非 `url`），以匹配保存端点的预期输入。

### 3. 保存头像端点 (UserController.java)
- **POST /api/users/me/avatar**
- 接受 JSON: `{"avatarUrl": "url"}`
- 使用 `@NotBlank` 和 `@JsonProperty("avatarUrl")` 进行正确验证
- 通过 `UserService.updateUserAvatar()` 将 URL 保存到数据库
- 返回更新后的 UserResponse DTO

### 4. 获取用户信息 (UserController.java)
- **GET /api/users/me**
- 返回包含 avatarUrl 的完整 UserResponse DTO

### 5. 服务层
- 添加 `updateUserAvatar(username, avatarUrl)` 方法
- 添加 `findByUsername(username)` 方法

### 6. DTO 和映射器
- **AvatarUrlRequest** - 使用正确的 JSON 映射进行请求验证
- **UserResponse** - 完整的用户信息，包括avatarUrl
- **UserMapper** - 实体到 DTO 的转换

### 7. 配置
- **WebConfig** - `/uploads/**` 的静态文件服务
- **application.properties** - 文件上传设置（最大 10MB）

## 测试工作流程

### 步骤 1：上传头像
```http
POST http://localhost:8080/api/files/upload/avatar
授权：Bearer {token}
Content-Type：multipart/form-data

正文：文件（JPG/PNG，最大 5MB，50x50 到 2000x2000 像素）

响应：
{
"avatarUrl": "/uploads/avatars/{uuid}.jpg"
}
```

### 步骤 2：保存头像 URL
```http
POST http://localhost:8080/api/users/me/avatar
授权：Bearer {token}
内容类型：application/json

正文：
{
"avatarUrl": "/uploads/avatars/{uuid}.jpg"
}

响应：
{
"id": 2,
"username": "seconduser",
"email": "seconduser@lost.com",
"avatarUrl": "/uploads/avatars/{uuid}.jpg"
}
```

### 步骤 3：验证
```http
GET http://localhost:8080/api/users/me
授权：Bearer {token}

响应：
{
"id": 2,
"username": "seconduser",
"email": "seconduser@lost.com",
"avatarUrl": "/uploads/avatars/{uuid}.jpg"
}
```

## 关键技术细节

- **上传验证**：格式 (JPG/PNG)、大小（最大 5MB）、尺寸（50x50 至 2000x2000px）
- **唯一文件名**：基于 UUID 以防止冲突
- **一致的字段命名**：上传和保存端点均使用 `avatarUrl`
- **JWT 身份验证**：所有端点均必需
- **JPA 自动更新**：使用 `ddl-auto=update` 自动更新数据库架构

## 修改的文件

1. `backend/blog/src/main/java/com/lost/blog/model/User.java`
2. `backend/blog/src/main/java/com/lost/blog/dto/AvatarUrlRequest.java`
3. `backend/blog/src/main/java/com/lost/blog/dto/UserResponse.java`
4. `backend/blog/src/main/java/com/lost/blog/mapper/UserMapper.java`
5. `backend/blog/src/main/java/com/lost/blog/service/UserService.java`
6. `backend/blog/src/main/java/com/lost/blog/service/UserServiceImpl.java`
7. `backend/blog/src/main/java/com/lost/blog/controller/FileController.java`
8. `backend/blog/src/main/java/com/lost/blog/controller/UserController.java`
9. `backend/blog/src/main/java/com/lost/blog/config/WebConfig.java`
10. `backend/blog/src/main/resources/application.properties`