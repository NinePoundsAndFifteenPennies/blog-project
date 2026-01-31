# 关注功能 - 前端对接文档

本文档为前端工程师提供关注功能的API对接指南，详细说明如何实现关注/取关、好友关系、列表展示及可见性控制等功能。

## 功能概述

关注功能支持以下特性：

1. **单向关注**: 用户A关注用户B，B的粉丝列表增加A
2. **朋友关系**: 当A和B互相关注时，系统自动标记为"朋友"
3. **取消关注**: 可以取消单向关注关系
4. **三种列表**: 关注列表（Following）、粉丝列表（Followers）、朋友列表（Friends）
5. **统计数据**: 关注数、粉丝数、朋友数
6. **可见性控制**: 用户可以独立设置每种信息对谁可见

---

## API 端点汇总

| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/users/{userId}/follow` | 必需 | 关注用户 |
| DELETE | `/api/users/{userId}/follow` | 必需 | 取消关注 |
| GET | `/api/users/{userId}/follow/stats` | 可选 | 获取关注统计 |
| GET | `/api/users/{userId}/following` | 可选 | 获取关注列表 |
| GET | `/api/users/{userId}/followers` | 可选 | 获取粉丝列表 |
| GET | `/api/users/{userId}/friends` | 可选 | 获取朋友列表 |
| GET | `/api/follow/visibility` | 必需 | 获取可见性设置 |
| PUT | `/api/follow/visibility` | 必需 | 更新可见性设置 |

---

## 核心功能对接

### 1. 关注/取消关注按钮

**场景**: 在用户主页或用户列表中显示关注/取关按钮

#### 关注用户

```javascript
// POST /api/users/{userId}/follow
const followUser = async (userId) => {
  const response = await fetch(`/api/users/${userId}/follow`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// 响应示例
{
  "following": true,
  "friend": false,       // 如果对方也关注了你，这里会是 true
  "message": "关注成功"  // 或 "关注成功，你们已成为朋友"
}
```

#### 取消关注

```javascript
// DELETE /api/users/{userId}/follow
const unfollowUser = async (userId) => {
  const response = await fetch(`/api/users/${userId}/follow`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// 响应示例
{
  "following": false,
  "friend": false,
  "message": "取消关注成功"
}
```

#### 前端UI建议

```vue
<template>
  <button @click="toggleFollow" :class="buttonClass">
    {{ buttonText }}
  </button>
</template>

<script>
export default {
  props: ['userId', 'isFollowing', 'isFriend'],
  computed: {
    buttonText() {
      if (this.isFriend) return '互相关注';
      if (this.isFollowing) return '已关注';
      return '关注';
    },
    buttonClass() {
      return {
        'btn-primary': !this.isFollowing,
        'btn-secondary': this.isFollowing && !this.isFriend,
        'btn-success': this.isFriend
      };
    }
  },
  methods: {
    async toggleFollow() {
      if (this.isFollowing) {
        await this.unfollowUser(this.userId);
      } else {
        await this.followUser(this.userId);
      }
    }
  }
};
</script>
```

---

### 2. 用户主页统计展示

**场景**: 在用户主页显示关注数、粉丝数、朋友数

```javascript
// GET /api/users/{userId}/follow/stats
const getFollowStats = async (userId) => {
  const response = await fetch(`/api/users/${userId}/follow/stats`, {
    headers: token ? { 'Authorization': `Bearer ${token}` } : {}
  });
  return response.json();
};

// 响应示例（正常）
{
  "followingCount": 42,
  "followerCount": 128,
  "friendCount": 15,
  "isFollowing": true,    // 当前登录用户是否关注了他（未登录为null）
  "isFriend": false       // 是否为朋友（未登录为null）
}

// 响应示例（被隐藏）
{
  "followingCount": -1,   // -1 表示数据被隐藏
  "followerCount": -1,
  "friendCount": -1,
  "isFollowing": true,    // 关系状态仍然返回
  "isFriend": false
}
```

#### 前端UI建议

```vue
<template>
  <div class="follow-stats">
    <div class="stat-item" @click="showFollowing">
      <span class="count">{{ formatCount(stats.followingCount) }}</span>
      <span class="label">关注</span>
    </div>
    <div class="stat-item" @click="showFollowers">
      <span class="count">{{ formatCount(stats.followerCount) }}</span>
      <span class="label">粉丝</span>
    </div>
    <div class="stat-item" @click="showFriends">
      <span class="count">{{ formatCount(stats.friendCount) }}</span>
      <span class="label">朋友</span>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    formatCount(count) {
      if (count === -1) return '-';  // 数据被隐藏
      if (count >= 10000) return (count / 10000).toFixed(1) + '万';
      return count;
    }
  }
};
</script>
```

---

### 3. 关注/粉丝/朋友列表

**场景**: 展示用户的关注列表、粉丝列表或朋友列表

```javascript
// GET /api/users/{userId}/following?page=0&size=20
// GET /api/users/{userId}/followers?page=0&size=20
// GET /api/users/{userId}/friends?page=0&size=20

const getFollowList = async (userId, type, page = 0, size = 20) => {
  const response = await fetch(
    `/api/users/${userId}/${type}?page=${page}&size=${size}`,
    {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {}
    }
  );
  return response.json();
};

// 响应示例
{
  "content": [
    {
      "id": 2,
      "username": "alice",
      "nickname": "Alice",
      "avatarUrl": "/uploads/2/avatars/xxx.jpg",
      "bio": "Hello world",
      "followedAt": "2026-01-30T10:30:00",
      "friend": true  // 是否互相关注
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0,       // 当前页码
  "first": true,
  "last": false
}
```

**注意**: 如果用户设置了可见性限制，无权查看时返回空列表。

#### 前端UI建议

```vue
<template>
  <div class="user-list">
    <!-- 列表为空的提示 -->
    <div v-if="users.length === 0" class="empty-state">
      <p v-if="isPrivate">该用户已隐藏此列表</p>
      <p v-else>暂无数据</p>
    </div>
    
    <!-- 用户列表 -->
    <div v-for="user in users" :key="user.id" class="user-card">
      <img :src="user.avatarUrl" class="avatar" />
      <div class="info">
        <span class="nickname">{{ user.nickname }}</span>
        <span class="username">@{{ user.username }}</span>
        <p class="bio">{{ user.bio }}</p>
      </div>
      <span v-if="user.friend" class="friend-badge">互相关注</span>
      <FollowButton :userId="user.id" />
    </div>
    
    <!-- 分页 -->
    <Pagination 
      :total="totalPages" 
      :current="currentPage"
      @change="loadPage"
    />
  </div>
</template>
```

---

### 4. 可见性设置页面

**场景**: 用户在设置页面配置关注信息的可见性

#### 获取当前设置

```javascript
// GET /api/follow/visibility
const getVisibilitySettings = async () => {
  const response = await fetch('/api/follow/visibility', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return response.json();
};

// 响应示例
{
  "following": {
    "isPublic": true,
    "visibleToFriends": false,
    "visibleToFollowing": false,
    "allowedNicknames": [],
    "blockedNicknames": []
  },
  "followers": { ... },
  "friends": { ... },
  "stats": { ... }
}
```

#### 更新设置

```javascript
// PUT /api/follow/visibility
const updateVisibility = async (settings) => {
  const response = await fetch('/api/follow/visibility', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(settings)
  });
  return response.json();
};

// 请求示例（只更新需要修改的部分）
{
  "following": {
    "isPublic": false,
    "visibleToFriends": true
  },
  "stats": {
    "isPublic": true
  }
}
```

#### 前端UI建议

```vue
<template>
  <div class="visibility-settings">
    <!-- 选择要设置的类型 -->
    <div class="tabs">
      <button 
        v-for="type in types" 
        :key="type.key"
        :class="{ active: currentType === type.key }"
        @click="currentType = type.key"
      >
        {{ type.label }}
      </button>
    </div>
    
    <!-- 可见性选项 -->
    <div class="options">
      <label>
        <input type="checkbox" v-model="settings[currentType].isPublic" />
        公开（所有人可见）
      </label>
      
      <label>
        <input type="checkbox" v-model="settings[currentType].visibleToFriends" />
        仅朋友可见（互相关注的用户）
      </label>
      
      <label>
        <input type="checkbox" v-model="settings[currentType].visibleToFollowing" />
        仅我关注的人可见
      </label>
      
      <!-- 允许的用户 -->
      <div class="nickname-list">
        <label>允许查看的用户（输入昵称）:</label>
        <TagInput v-model="settings[currentType].allowedNicknames" />
      </div>
      
      <!-- 禁止的用户（黑名单） -->
      <div class="nickname-list">
        <label>禁止查看的用户（黑名单，优先级最高）:</label>
        <TagInput v-model="settings[currentType].blockedNicknames" />
      </div>
    </div>
    
    <button @click="saveSettings">保存设置</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentType: 'following',
      types: [
        { key: 'following', label: '关注列表' },
        { key: 'followers', label: '粉丝列表' },
        { key: 'friends', label: '朋友列表' },
        { key: 'stats', label: '统计数据' }
      ],
      settings: {}
    };
  }
};
</script>
```

---

## 可见性规则说明

用户可以为每种类型（关注列表/粉丝列表/朋友列表/统计数据）独立设置可见性。

### 规则优先级（从高到低）

1. **本人查看** → 始终允许
2. **黑名单** (`blockedNicknames`) → 始终拒绝
3. **公开** (`isPublic = true`) → 允许
4. **白名单** (`allowedNicknames`) → 允许
5. **仅朋友** (`visibleToFriends = true` 且互相关注) → 允许
6. **仅关注的人** (`visibleToFollowing = true` 且用户关注了查看者) → 允许
7. **其他情况** → 拒绝

### 组合示例

| 场景 | 设置 |
|------|------|
| 完全公开 | `isPublic: true` |
| 仅自己可见 | 全部设为 `false` |
| 仅朋友可见 | `isPublic: false, visibleToFriends: true` |
| 公开但拉黑某人 | `isPublic: true, blockedNicknames: ["spammer"]` |
| 对某些人开放 | `isPublic: false, allowedNicknames: ["alice", "bob"]` |

---

## 错误处理

### 常见错误

| 状态码 | 错误 | 处理建议 |
|--------|------|----------|
| 400 | 不能关注自己 | 禁用自己页面的关注按钮 |
| 401 | 未登录 | 跳转登录页或显示登录弹窗 |
| 404 | 用户不存在 | 显示用户不存在提示 |

### 错误响应格式

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "未找到ID为: 999 的用户",
  "path": "/api/users/999/follow",
  "timestamp": "2026-01-31T10:30:00"
}
```

---

## 最佳实践

### 1. 乐观更新

关注/取消关注操作可以使用乐观更新提升用户体验：

```javascript
const toggleFollow = async (userId) => {
  // 乐观更新UI
  this.isFollowing = !this.isFollowing;
  
  try {
    if (this.isFollowing) {
      const result = await followUser(userId);
      this.isFriend = result.friend;
    } else {
      await unfollowUser(userId);
      this.isFriend = false;
    }
  } catch (error) {
    // 回滚
    this.isFollowing = !this.isFollowing;
    this.$toast.error('操作失败');
  }
};
```

### 2. 缓存与刷新

- 关注统计可以缓存，但关注/取关操作后需刷新
- 列表可以使用分页加载，避免一次加载过多数据
- 切换tab时可以缓存已加载的列表

### 3. 隐私提示

当数据被隐藏时（count 返回 -1 或列表为空），建议显示友好提示：

```javascript
const isPrivate = (count) => count === -1;

// 模板中
<span v-if="isPrivate(stats.followingCount)">
  该用户已隐藏此信息
</span>
```

---

## 数据库说明

如果从旧版本升级，需要执行数据库迁移：

```sql
-- 删除旧表让Hibernate重新创建
DROP TABLE IF EXISTS follow_visibility;

-- 然后重启后端应用
```

或使用迁移脚本：
`backend/blog/src/main/resources/db/migration/V2__update_follow_visibility_table.sql`

---

## 联系方式

如有问题，请联系后端工程师或在项目Issue中提问。
