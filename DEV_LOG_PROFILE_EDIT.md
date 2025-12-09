# 用户资料编辑开发记录

## 变更摘要
- 新增 `UserProfileUpdateRequest` 请求体与 `UserResponse` 字段，支持昵称、简介、社交链接、性别、生日、所在地、邮箱与密码的更新。
- 扩展 `User` 实体与 `UserRepository` 唯一性校验，新增资料字段并校验昵称/邮箱唯一。
- 在 `UserService`/`UserController` 中增加 `/api/users/me/profile` 更新接口与业务逻辑，校验仅允许本人修改并校验旧密码。
- 更新 `docs/API.md`，同步返回字段与请求示例。
- 支持通过传空字符串清空社交链接/生日/简介等可选字段；未登录请求现在返回 401。

## 测试指南（Postman 示例）
1. **修改成功**  
   - 请求：`PUT http://localhost:8080/api/users/me/profile`  
   - Header：`Authorization: Bearer <token>`  
   - Body：
     ```json
     {
       "nickname": "new-nick",
       "bio": "更新后的个人简介",
       "socialLink": "https://example.com/me",
       "gender": "female",
       "birthday": "1995-02-03",
       "location": "上海",
       "email": "newmail@example.com",
       "currentPassword": "oldpass123",
       "newPassword": "newpass456"
     }
     ```
   - 期望：`200 OK`，返回最新用户信息。

2. **参数错误**（仅提供新密码）  
   - Body：
     ```json
     {
       "newPassword": "newpass456"
     }
     ```
   - 期望：`400 Bad Request`，提示需同时提供当前密码。

3. **权限不足**（未携带 Token）  
   - 请求：同成功示例但不带 `Authorization` 头  
   - 期望：`401 Unauthorized`。

## 注意事项
- 新增数据库字段（昵称、简介、社交链接、性别、生日、所在地），请确保数据库迁移或开启自动建表/更新以同步结构，昵称设置了唯一约束。
- 更换邮箱和昵称均需唯一校验；修改密码必须同时提供 `currentPassword` 与 `newPassword` 并校验旧密码。
- 敏感词过滤可在 `UserServiceImpl.updateProfile` 中按需扩展。  
