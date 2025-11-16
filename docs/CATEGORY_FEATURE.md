# 博客系统分类功能说明

## 功能概述

本次更新为博客系统添加了**分类（Category）**功能。分类功能与已有的标签功能互补，但在使用上有所不同：

- **标签（Tag）**：多对多关系，一篇文章可以拥有多个标签，用于细粒度的内容分类
- **分类（Category）**：多对一关系，一篇文章只能属于一个分类，用于宏观的内容分组

## 数据模型

### Category 实体

Category 实体包含以下字段：

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | Long | 分类ID | 主键，自增 |
| name | String | 分类名称 | 非空，唯一，1-50字符 |
| description | String | 分类描述 | 可选，最多200字符 |
| color | String | 分类颜色 | 可选，格式：#RRGGBB |
| icon | String | 分类图标 | 可选，最多50字符 |
| sortOrder | Integer | 排序顺序 | 可选，数字越小越靠前 |
| createdBy | User | 创建者 | 非空，外键关联 |
| createdAt | LocalDateTime | 创建时间 | 自动生成 |
| updatedAt | LocalDateTime | 更新时间 | 自动更新 |

### Post 与 Category 的关系

在 Post 实体中添加了分类字段：

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;
```

- 一个分类可以包含多篇文章
- 一篇文章只能属于一个分类（可以不属于任何分类）
- 管理员可以硬删除分类（关联的文章不会被删除，只是category字段会被设置为null）
- 用户可以软删除文章与分类的关联（分类本身仍保留在数据库中）

## API 接口

所有分类相关接口的基础路径为：`/api/categories`

### 1. 创建分类

**请求方式**：`POST /api/categories`

**认证要求**：需要登录

**请求体示例**：
```json
{
  "name": "技术文章",
  "description": "技术相关的文章分类",
  "color": "#3B82F6",
  "icon": "tech-icon",
  "sortOrder": 1
}
```

**响应示例**：
```json
{
  "id": 1,
  "name": "技术文章",
  "description": "技术相关的文章分类",
  "color": "#3B82F6",
  "icon": "tech-icon",
  "sortOrder": 1,
  "createdById": 1,
  "createdByUsername": "admin",
  "postCount": 0,
  "createdAt": "2025-11-08T14:00:00",
  "updatedAt": null
}
```

### 2. 获取单个分类

**请求方式**：`GET /api/categories/{id}`

**认证要求**：无

**响应**：返回分类详情（格式同上）

### 3. 根据名称获取分类

**请求方式**：`GET /api/categories/name/{name}`

**认证要求**：无

**示例**：`GET /api/categories/name/技术文章`

### 4. 获取所有分类（分页）

**请求方式**：`GET /api/categories`

**认证要求**：无

**查询参数**：
- `page`：页码（从0开始）
- `size`：每页数量
- `sort`：排序字段，如 `sortOrder,asc` 或 `createdAt,desc`

**响应示例**：
```json
{
  "content": [
    {
      "id": 1,
      "name": "技术文章",
      "description": "技术相关的文章分类",
      "color": "#3B82F6",
      "icon": "tech-icon",
      "sortOrder": 1,
      "createdById": 1,
      "createdByUsername": "admin",
      "postCount": 5,
      "createdAt": "2025-11-08T14:00:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 5. 获取热门分类

**请求方式**：`GET /api/categories/popular`

**认证要求**：无

**说明**：按文章数量降序返回所有分类

**响应**：分类数组（格式同上）

### 6. 更新分类

**请求方式**：`PUT /api/categories/{id}`

**认证要求**：需要登录

**请求体**：与创建分类相同

### 7. 删除分类

**请求方式**：`DELETE /api/categories/{id}`

**认证要求**：需要登录

**说明**：如果有文章使用该分类，将返回错误，禁止删除

**成功响应**：
```json
"分类删除成功"
```

**错误响应**：
- `401 Unauthorized` - 未登录或 token 无效
- `404 Not Found` - 分类不存在

**注意**：管理员可以硬删除分类，即使有文章使用该分类。删除后，关联的文章不会被删除，只是category字段会被设置为null。

---

### 8. 从文章中移除标签（软删除）

用户可以从自己的文章中移除指定标签，标签本身不会被删除。

**请求方式**：`DELETE /api/posts/{postId}/tags/{tagName}`

**认证要求**：需要登录，且只能操作自己的文章

**路径参数**：
- `postId`：文章ID
- `tagName`：标签名称

**成功响应**：返回更新后的文章详情

---

### 9. 从文章中移除分类（软删除）

用户可以从自己的文章中移除分类，分类本身不会被删除。

**请求方式**：`DELETE /api/posts/{postId}/category`

**认证要求**：需要登录，且只能操作自己的文章

**路径参数**：
- `postId`：文章ID

**成功响应**：返回更新后的文章详情

## 文章与分类的集成

### 创建/更新文章时指定分类

在创建或更新文章时，可以通过 `category` 字段（分类名称）指定分类：

**请求示例**：
```json
{
  "title": "我的第一篇技术文章",
  "content": "文章内容...",
  "contentType": "MARKDOWN",
  "draft": false,
  "category": "技术文章",
  "tags": ["Java", "Spring Boot"]
}
```

- `category` 为可选字段，使用分类名称而非ID（与标签一致）
- 如果分类不存在，系统会自动创建该分类
- 更新文章时，如果不提供 `category` 或提供空字符串，则会移除文章的分类（软删除）

### 文章响应中的分类信息

获取文章时，响应中会包含完整的分类信息：

```json
{
  "id": 1,
  "title": "我的第一篇技术文章",
  "content": "文章内容...",
  "category": {
    "id": 1,
    "name": "技术文章",
    "description": "技术相关的文章分类",
    "color": "#3B82F6",
    "icon": "tech-icon",
    "sortOrder": 1,
    "createdById": 1,
    "createdByUsername": "admin",
    "postCount": 5,
    "createdAt": "2025-11-08T14:00:00",
    "updatedAt": null
  },
  "tags": [...],
  ...
}
```

## 代码架构

分类功能完全遵循项目的三层架构设计：

### 1. 控制层（Controller）
- **CategoryController**：处理所有分类相关的 HTTP 请求

### 2. 服务层（Service）
- **CategoryService**：分类业务逻辑接口
- **CategoryServiceImpl**：分类业务逻辑实现

### 3. 数据访问层（Repository）
- **CategoryRepository**：分类数据访问接口

### 4. 数据传输对象（DTO）
- **CategoryRequest**：创建/更新分类请求
- **CategoryResponse**：分类响应

### 5. 映射器（Mapper）
- **CategoryMapper**：Category 实体与 DTO 的转换

### 6. 实体（Model）
- **Category**：分类实体类

## 数据库变更

系统会自动创建以下数据库表：

### categories 表

```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    color VARCHAR(7),
    icon VARCHAR(50),
    sort_order INT,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

### posts 表更新

在 `posts` 表中添加分类关联：

```sql
ALTER TABLE posts ADD COLUMN category_id BIGINT;
ALTER TABLE posts ADD FOREIGN KEY (category_id) REFERENCES categories(id);
```

## 使用建议

1. **分类策略**：建议创建少量的顶级分类（如"技术"、"生活"、"随笔"等），用于文章的宏观分组
2. **标签策略**：使用标签进行细粒度分类（如"Java"、"Python"、"前端"等）
3. **组合使用**：一篇文章可以同时拥有一个分类和多个标签，实现多维度的内容组织
4. **软删除vs硬删除**：
   - 用户可以从文章中移除标签或分类（软删除），不影响标签/分类本身
   - 管理员可以硬删除标签或分类，关联的文章不会被删除
5. **自动创建**：创建文章时，如果指定的分类不存在，系统会自动创建

## 示例场景

### 场景 1：创建技术博客分类体系

```
1. 创建分类：
   - 技术文章（sortOrder: 1）
   - 生活随笔（sortOrder: 2）
   - 读书笔记（sortOrder: 3）

2. 创建标签：
   - Java, Python, JavaScript (技术类)
   - 旅行, 美食, 运动 (生活类)
   - 技术书籍, 小说, 散文 (读书类)

3. 发布文章：
   - 文章："Spring Boot 实战" → 分类：技术文章，标签：Java, Spring Boot
   - 文章："周末爬山记" → 分类：生活随笔，标签：旅行, 运动
   - 文章："深入理解 JVM" → 分类：读书笔记，标签：Java, 技术书籍
```

### 场景 2：按分类浏览文章

前端可以实现以下功能：

1. 在首页侧边栏显示所有分类及文章数量
2. 点击分类后，只显示该分类下的文章
3. 在文章详情页显示所属分类和标签
4. 提供"分类"和"标签"两种导航方式

## 兼容性说明

- 已有的文章不会受到影响，`category` 字段默认为 `null`
- 标签功能保持不变，可以与分类功能共存
- 所有已有的 API 接口保持向后兼容

## 后续优化建议

1. **分类层级**：如需要，可以扩展为支持父子分类的树形结构
2. **分类图片**：可以为每个分类添加封面图片
3. **分类统计**：提供更丰富的分类统计信息（浏览量、点赞数等）
4. **批量操作**：支持批量修改文章的分类
5. **权限管理**：可以限制只有管理员才能创建/删除分类
