# 标签功能集成说明

## 概述

前端已完整对接后端标签系统，支持标签显示、自动创建和标签筛选功能。

## API接口

所有标签API已封装在 `/frontend/src/api/tags.js`：

- `getTags(params)` - 获取所有标签（分页）
- `getPopularTags()` - 获取热门标签
- `getTagById(id)` / `getTagByName(name)` - 获取标签详情
- `createTag(tagData)` / `updateTag(id, tagData)` / `deleteTag(id)` - CRUD操作（管理员专用）

## 核心组件

### 1. TagBadge - 标签徽章组件
可复用的标签显示组件，支持颜色和图标。

**使用方法：**
```vue
<TagBadge :tag="tag" :show-icon="true" />
```

### 2. PopularTags - 热门标签侧边栏
主页右侧显示热门标签，点击可查看该标签的所有文章。

### 3. PostEdit - 文章编辑
标签输入框支持：
- 回车键添加标签
- 格式验证（1-50字符，中英文数字空格下划线连字符）
- 去重检查

### 4. 标签显示位置
- **PostCard**：显示前3个标签
- **PostDetail**：显示所有标签（带图标）
- **TagPosts**：标签文章列表页

## 标签自动创建

用户创建文章时：
1. 后端检查标签是否存在
2. 不存在：自动创建并分配随机颜色（RGB 60-220）
3. 存在：使用数据库中的颜色和图标

**颜色生成算法：**
- RGB范围：60-220（避免太亮/太暗）
- 饱和度检测：避免灰色

## 标签字段说明

- **name**: 标签名称（必填，唯一）
- **color**: 颜色代码（自动生成或手动设置，格式：#RRGGBB）
- **icon**: Font Awesome图标类名（如：fa-java，可选）
- **description**: 标签描述（可选）
- **sortOrder**: 排序顺序，数字越小越靠前（可选）

## Font Awesome图标

前端已加载Font Awesome 6，支持自动类名转换：
- `fa-java` → `fa-brands fa-java`
- `fa-brands fa-python` → 保持不变
- `fab fa-node-js` → 保持不变
- `fas fa-code` → 保持不变

**设置方法**（管理员通过后端API）：
```bash
PUT /api/tags/{id}
{
  "name": "Java",
  "icon": "fa-java",
  "color": "#FF5733"
}
```

## 路由

- `/` - 首页（包含热门标签侧边栏）
- `/tags/:tagName` - 标签文章列表页

## 权限说明

- 普通用户：可创建带标签的文章（标签自动创建）
- 管理员：可通过后端API编辑标签属性（color、icon、sortOrder等）

标签属性编辑功能预留给未来的管理后台系统。
