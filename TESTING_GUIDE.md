# Testing Guide for Avatar Upload Feature

## Testing Scenario (测试场景)

### Prerequisites
1. Backend server running on `http://localhost:8080`
2. User registered and logged in with JWT token
3. An image file ready to upload

### Test Case 1: Complete Avatar Upload Flow

#### Step 1: Upload Avatar File
```bash
curl -X POST http://localhost:8080/api/files/upload/avatar \
  -F "file=@/path/to/avatar.jpg"
```

**Expected Response:**
```json
{
  "url": "/uploads/avatars/uuid-generated-filename.jpg"
}
```

#### Step 2: Update User Avatar
```bash
curl -X POST http://localhost:8080/api/users/me/avatar \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "avatarUrl": "/uploads/avatars/uuid-generated-filename.jpg"
  }'
```

**Expected Response:**
```json
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/avatars/uuid-generated-filename.jpg"
}
```

**Previous Bug:** Would return `"avatarUrl": null`
**Now Fixed:** Returns the correct avatar URL

#### Step 3: Verify Avatar Persisted
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "id": 2,
  "username": "seconduser",
  "email": "seconduser@lost.com",
  "avatarUrl": "/uploads/avatars/uuid-generated-filename.jpg"
}
```

#### Step 4: Access Avatar Image
```
http://localhost:8080/uploads/avatars/uuid-generated-filename.jpg
```

Should display the uploaded image.

## Postman Testing

### 1. Upload Avatar
- **Method:** POST
- **URL:** `http://localhost:8080/api/files/upload/avatar`
- **Body:** 
  - Type: form-data
  - Key: `file`
  - Type: File
  - Value: Select an image file
- **Expected:** Returns JSON with `url` field

### 2. Update User Avatar
- **Method:** POST
- **URL:** `http://localhost:8080/api/users/me/avatar`
- **Headers:**
  - `Authorization: Bearer YOUR_JWT_TOKEN`
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "avatarUrl": "/uploads/avatars/the-url-from-step-1.jpg"
  }
  ```
- **Expected:** Returns user object with avatarUrl populated

### 3. Get Current User
- **Method:** GET
- **URL:** `http://localhost:8080/api/users/me`
- **Headers:**
  - `Authorization: Bearer YOUR_JWT_TOKEN`
- **Expected:** Returns user object with avatarUrl field

## Database Verification

After running the tests, check the database:

```sql
SELECT id, username, email, avatar_url FROM users WHERE username = 'seconduser';
```

Expected result:
```
id | username    | email                | avatar_url
2  | seconduser  | seconduser@lost.com  | /uploads/avatars/uuid-filename.jpg
```

## Common Issues and Solutions

### Issue 1: "文件不能为空"
**Cause:** No file provided in the request
**Solution:** Ensure the file field is properly populated in the multipart form

### Issue 2: "头像URL不能为空"
**Cause:** Missing or empty `avatarUrl` in request body
**Solution:** Ensure the JSON body contains the `avatarUrl` field with a valid value

### Issue 3: "未登录"
**Cause:** Missing or invalid JWT token
**Solution:** Login first and include the Bearer token in Authorization header

### Issue 4: "用户不存在"
**Cause:** Username in token doesn't match any user in database
**Solution:** Verify user exists and token is valid

## What Was Fixed

The original bug was that when calling `POST /api/users/me/avatar`, even though the endpoint might have existed, it would:
1. Not persist the avatarUrl to the database (missing @Transactional)
2. Return avatarUrl as null because:
   - User model didn't have the field
   - UserService didn't have update method
   - Response mapping was incorrect

Now with the fix:
1. ✅ User model has `avatarUrl` field
2. ✅ UserService has `@Transactional updateAvatar()` method
3. ✅ UserController properly updates and returns the avatar URL
4. ✅ GET /api/users/me returns complete user info with avatar
5. ✅ File upload endpoint properly handles multipart files
6. ✅ Static file serving configured for uploaded avatars
