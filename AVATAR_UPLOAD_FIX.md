# Avatar Upload Bug Fix

## Problem Description (问题描述)
用户反馈：新写的头像上传功能存在bug
- `/api/files/upload/avatar` 可以正确获取头像URL
- `/api/users/me/avatar` 保存时会返回 `avatarUrl: null`

## Root Cause (根本原因)
The avatar upload feature was mentioned but not fully implemented. The following components were missing:

1. **User Model**: Missing `avatarUrl` field
2. **FileController**: Empty implementation 
3. **UserService**: Missing methods to update and retrieve user avatar
4. **UserController**: 
   - `/me` endpoint only returned a string instead of full user data
   - Missing `/me/avatar` endpoint to update avatar
5. **DTOs and Mappers**: Missing proper user response DTO

## Changes Made (修改内容)

### 1. User Model (`User.java`)
Added `avatarUrl` field with getter and setter:
```java
@Column(name = "avatar_url")
private String avatarUrl;
```

### 2. UserResponse DTO (`UserResponse.java`)
Created complete DTO with all user fields including `avatarUrl`:
```java
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    // constructors, getters, setters
}
```

### 3. UserMapper (`UserMapper.java`)
Implemented mapper to convert User entity to UserResponse:
```java
@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatarUrl()
        );
    }
}
```

### 4. UserService Interface and Implementation
Added methods:
- `User updateAvatar(String username, String avatarUrl)` - Updates user's avatar URL
- `User findByUsername(String username)` - Retrieves user by username

The `updateAvatar` implementation uses `@Transactional` to ensure data consistency.

### 5. FileController (`FileController.java`)
Implemented complete file upload functionality:
- Accepts multipart file upload
- Generates unique filename using UUID
- Saves file to `uploads/avatars/` directory
- Returns file URL in format: `/uploads/avatars/{filename}`

### 6. UserController (`UserController.java`)
**Fixed GET `/api/users/me`**:
- Changed from returning simple string to returning complete `UserResponse` object
- Now includes `id`, `username`, `email`, and `avatarUrl`

**Added POST `/api/users/me/avatar`**:
- Accepts JSON body with `avatarUrl` field
- Updates the current user's avatar URL
- Returns updated user information

### 7. WebConfig (`WebConfig.java`)
Added configuration for:
- Static file serving for uploaded avatars
- CORS configuration for frontend access

## API Endpoints (API端点)

### Upload Avatar File
```http
POST /api/files/upload/avatar
Content-Type: multipart/form-data

file: <image file>

Response:
{
  "url": "/uploads/avatars/uuid-filename.jpg"
}
```

### Update User Avatar URL
```http
POST /api/users/me/avatar
Content-Type: application/json
Authorization: Bearer <token>

{
  "avatarUrl": "/uploads/avatars/uuid-filename.jpg"
}

Response:
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/avatars/uuid-filename.jpg"
}
```

### Get Current User Info
```http
GET /api/users/me
Authorization: Bearer <token>

Response:
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/avatars/uuid-filename.jpg"
}
```

## Usage Flow (使用流程)

1. **Upload Avatar Image**:
   - Frontend sends file to `POST /api/files/upload/avatar`
   - Backend saves file and returns URL

2. **Save Avatar URL**:
   - Frontend sends returned URL to `POST /api/users/me/avatar`
   - Backend updates user's `avatarUrl` field in database
   - Returns complete user info with avatar URL

3. **Retrieve User Info**:
   - Frontend calls `GET /api/users/me`
   - Backend returns user info including avatar URL

## Bug Fix Summary (Bug修复总结)

The key issue was that the `avatarUrl` field was not being persisted because:
1. The User model didn't have the field
2. The service layer had no method to update it
3. The controller endpoint was missing

Now the complete flow is implemented:
- File upload → Returns URL
- Save URL → Persists to database with `@Transactional`
- Retrieve user → Returns full user info including avatar

The `avatarUrl` will now be properly saved and returned in API responses.
