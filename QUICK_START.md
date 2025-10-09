# 🚀 Quick Start - Testing the Draft Box Fix

## What Was Fixed?

The draft box in the Profile page now correctly displays your draft posts!

## How to Test (5 minutes)

### Step 1: Start Your Services
```bash
# Terminal 1 - Start backend
cd backend/blog
mvn spring-boot:run

# Terminal 2 - Start frontend  
cd frontend
npm run serve
```

### Step 2: Create Test Data
1. Open http://localhost:8081 (or your frontend port)
2. Login with your account
3. Create 2 published articles (uncheck "Save as draft")
4. Create 2 draft articles (check "Save as draft")

### Step 3: Verify the Fix
1. Go to Profile page
2. Check statistics at top: Should show "2 文章" and "2 草稿"
3. Click "草稿箱" tab
4. **You should now see your 2 draft articles!** ��

### Step 4: Check Debug Logs

**Browser Console** (F12 → Console):
```
当前用户的所有文章（包括草稿）: 4
统计 - 草稿数: 2 已发布: 2
```

**Backend Console**:
```
INFO - 查询用户 xxx 的所有文章（包括草稿），总数: 4
```

## What Changed?

### New API Endpoint
```
GET /api/posts/my
```
Returns all your posts including drafts (requires login).

### Frontend Change
Profile.vue now uses `getMyPosts()` instead of `getPosts()`.

## If It Still Doesn't Work

1. **Hard refresh** the browser (Ctrl+Shift+R)
2. **Clear browser cache**
3. **Check console** for any error messages
4. **Verify backend is running** and connected to database
5. **Check database** has posts with `is_draft = 1`

## Database Quick Check

```sql
-- Check if you have drafts in database
SELECT id, title, is_draft, user_id 
FROM posts 
WHERE is_draft = 1
ORDER BY created_at DESC;
```

## Need More Help?

📖 Read the detailed guides:
- **TESTING_GUIDE.md** - Complete testing instructions
- **DRAFT_FIX_SUMMARY.md** - Technical explanation
- **PR_SUMMARY.md** - Full implementation details

## Clean Up Debug Logs (Optional)

After confirming everything works, you can remove:
- `console.log()` statements in Profile.vue
- `logger.debug()` statements in PostServiceImpl.java

Keep the `logger.info()` for production monitoring.

## Success Criteria ✅

- [ ] Draft box shows your draft articles
- [ ] Published articles show in "我的文章" tab
- [ ] Statistics are correct
- [ ] Can switch between tabs
- [ ] Can edit and publish drafts
- [ ] Can convert published to draft
- [ ] Other users can't see your drafts

If all checkboxes are ✅, the fix is working perfectly!
