# Avatar Upload - Code Flow Diagram

## Complete Request Flow (完整请求流程)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        1. UPLOAD AVATAR FILE                        │
└─────────────────────────────────────────────────────────────────────┘

Frontend                FileController                      File System
   │                           │                                  │
   │  POST /api/files/        │                                  │
   │  upload/avatar           │                                  │
   │  (multipart file)        │                                  │
   ├─────────────────────────>│                                  │
   │                           │  1. Validate file not empty     │
   │                           │  2. Create uploads/avatars/     │
   │                           ├────────────────────────────────>│
   │                           │                                  │
   │                           │  3. Generate UUID filename      │
   │                           │  4. Save file                   │
   │                           ├────────────────────────────────>│
   │                           │                                  │
   │                           │  5. Return URL                  │
   │  { "url": "/uploads/     │                                  │
   │    avatars/uuid.jpg" }   │                                  │
   │<─────────────────────────┤                                  │
   │                           │                                  │

┌─────────────────────────────────────────────────────────────────────┐
│                       2. SAVE AVATAR URL                            │
└─────────────────────────────────────────────────────────────────────┘

Frontend            UserController         UserService          Database
   │                      │                     │                   │
   │  POST /api/users/   │                     │                   │
   │  me/avatar          │                     │                   │
   │  { "avatarUrl":     │                     │                   │
   │    "/uploads/..."} │                     │                   │
   ├────────────────────>│                     │                   │
   │                      │  1. Get username   │                   │
   │                      │     from JWT token │                   │
   │                      │                     │                   │
   │                      │  2. updateAvatar() │                   │
   │                      ├────────────────────>│                   │
   │                      │                     │  3. Find user    │
   │                      │                     ├──────────────────>│
   │                      │                     │                   │
   │                      │                     │  4. Set avatarUrl│
   │                      │                     │  5. @Transactional│
   │                      │                     │     save()       │
   │                      │                     ├──────────────────>│
   │                      │                     │  ✅ SAVED!       │
   │                      │                     │                   │
   │                      │  6. Return User    │                   │
   │                      │<────────────────────┤                   │
   │                      │  7. Map to DTO     │                   │
   │  { "id": 2,         │                     │                   │
   │    "username": "...",│                     │                   │
   │    "email": "...",  │                     │                   │
   │    "avatarUrl":     │                     │                   │
   │    "/uploads/..." } │                     │                   │
   │<────────────────────┤                     │                   │

┌─────────────────────────────────────────────────────────────────────┐
│                     3. RETRIEVE USER INFO                           │
└─────────────────────────────────────────────────────────────────────┘

Frontend            UserController         UserService          Database
   │                      │                     │                   │
   │  GET /api/users/me  │                     │                   │
   ├────────────────────>│                     │                   │
   │                      │  1. Get username   │                   │
   │                      │     from JWT token │                   │
   │                      │                     │                   │
   │                      │  2. findByUsername()│                  │
   │                      ├────────────────────>│                   │
   │                      │                     │  3. SELECT *     │
   │                      │                     │     FROM users   │
   │                      │                     ├──────────────────>│
   │                      │                     │  ✅ avatar_url   │
   │                      │                     │<──────────────────┤
   │                      │  4. Return User    │                   │
   │                      │<────────────────────┤                   │
   │                      │  5. Map to DTO     │                   │
   │  { "id": 2,         │                     │                   │
   │    "username": "...",│                     │                   │
   │    "email": "...",  │                     │                   │
   │    "avatarUrl":     │                     │                   │
   │    "/uploads/..." } │                     │                   │
   │<────────────────────┤                     │                   │
```

## Key Components Added/Modified (关键组件添加/修改)

### 1. Database Layer
```
┌──────────────────────┐
│   users table        │
├──────────────────────┤
│ id                   │
│ username             │
│ email                │
│ password             │
│ avatar_url  ← NEW!   │  ✅ Added this column
│ created_at           │
│ updated_at           │
└──────────────────────┘
```

### 2. Model Layer
```java
User.java
  + private String avatarUrl;        ← NEW field
  + public String getAvatarUrl()     ← NEW getter
  + public void setAvatarUrl(String) ← NEW setter
```

### 3. Service Layer
```java
UserService.java
  + User updateAvatar(String username, String avatarUrl)  ← NEW
  + User findByUsername(String username)                  ← NEW

UserServiceImpl.java
  + @Transactional                                        ← CRITICAL!
    public User updateAvatar(...) {                       ← NEW
      user.setAvatarUrl(avatarUrl);
      return userRepository.save(user);  ← Persists to DB
    }
```

### 4. Controller Layer
```java
FileController.java
  + POST /api/files/upload/avatar  ← NEW endpoint
    - Accepts MultipartFile
    - Saves to uploads/avatars/
    - Returns { "url": "..." }

UserController.java
  ✏️ GET /api/users/me               ← MODIFIED
    - OLD: return "当前登录用户是: username"
    - NEW: return UserResponse with all fields including avatarUrl
  
  + POST /api/users/me/avatar       ← NEW endpoint
    - Accepts { "avatarUrl": "..." }
    - Calls userService.updateAvatar()
    - Returns complete user info
```

### 5. DTO Layer
```java
UserResponse.java  ← IMPLEMENTED
  - id
  - username
  - email
  - avatarUrl  ← Includes this!

UserMapper.java    ← IMPLEMENTED
  + toResponse(User) → UserResponse
```

### 6. Configuration Layer
```java
WebConfig.java     ← CONFIGURED
  + addResourceHandlers()
    - Maps /uploads/avatars/** to file:uploads/avatars/
  + addCorsMappings()
    - Allows frontend to access API
```

## Bug Fix Summary (Bug修复总结)

### ❌ Before (Bug存在时)
```
POST /api/users/me/avatar
Request: { "avatarUrl": "/uploads/avatars/abc.jpg" }
Response: { 
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": null  ← ❌ NULL! Bug!
}

Database: avatar_url column doesn't exist or not updated
```

### ✅ After (Bug修复后)
```
POST /api/users/me/avatar
Request: { "avatarUrl": "/uploads/avatars/abc.jpg" }
Response: { 
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/avatars/abc.jpg"  ← ✅ Correct!
}

Database: avatar_url = "/uploads/avatars/abc.jpg"  ← ✅ Persisted!
```

## Why the Bug Occurred (Bug产生原因)

1. ❌ Missing `avatarUrl` field in User model
   → Data couldn't be stored

2. ❌ Missing `updateAvatar()` in UserService
   → No method to update the avatar

3. ❌ Missing `@Transactional` annotation
   → Changes might not persist to database

4. ❌ Missing `/me/avatar` endpoint in UserController
   → No way to trigger the update

5. ❌ GET `/me` returned string instead of UserResponse
   → Frontend couldn't see the avatar URL even if it existed

All issues are now resolved! ✅
