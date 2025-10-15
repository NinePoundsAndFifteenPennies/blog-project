# 开发说明

本文档说明项目的开发、测试和部署相关内容。

## 开发环境要求

### 后端
- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+
- IDE: IntelliJ IDEA 推荐

### 前端
- Node.js 16+
- npm 8+
- IDE: VS Code 推荐

## 本地开发设置

### 1. 克隆仓库

```bash
git clone https://github.com/NinePoundsAndFifteenPennies/blog-project.git
cd blog-project
```

### 2. 配置数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置后端

修改 `backend/blog/src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT 配置
app.jwt.secret=your_secret_key_here_minimum_256_bits
app.jwt.expiration-ms=3600000
app.jwt.remember-me-expiration-ms=2592000000

# 文件上传配置
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 4. 启动后端

```bash
cd backend/blog
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 运行。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 运行。

## 项目结构

### 后端目录结构

```
backend/blog/
├── src/
│   ├── main/
│   │   ├── java/com/lost/blog/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── exception/       # 异常处理
│   │   │   ├── mapper/          # 实体与DTO转换
│   │   │   ├── model/           # 数据库实体
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── security/        # 安全相关
│   │   │   └── service/         # 业务逻辑层
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # 测试代码
└── pom.xml                      # Maven 配置
```

### 前端目录结构

```
frontend/
├── src/
│   ├── api/              # API 请求封装
│   ├── components/       # 可复用组件
│   ├── router/           # 路由配置
│   ├── views/            # 页面组件
│   ├── App.vue
│   └── main.js
├── public/
└── package.json
```

## 开发注意事项

### 后端开发

#### 1. 数据库自动更新

`spring.jpa.hibernate.ddl-auto=update` 配置在开发阶段使用，Hibernate 会自动更新数据库表结构。

**⚠️ 生产环境警告:** 生产环境应设置为 `validate` 或 `none`，并使用专业的数据库迁移工具（如 Flyway 或 Liquibase）。

#### 2. JWT 密钥

开发环境的 JWT 密钥写在配置文件中，**绝对不能**用于生产环境。

**生产环境必须:**
- 使用环境变量注入密钥
- 密钥长度至少 256 位
- 定期轮换密钥

#### 3. 跨域配置

开发环境允许所有跨域请求，生产环境需要配置具体的允许域名。

#### 4. 文件上传

- 上传的文件存储在 `uploads/` 目录
- 该目录已添加到 `.gitignore`，不会提交到版本控制
- 生产环境建议使用对象存储服务（如阿里云OSS、AWS S3）

### 前端开发

#### 1. API 基础 URL

开发环境 API 地址配置在 `frontend/src/api/request.js`：

```javascript
const API_BASE_URL = 'http://localhost:8080'
```

生产环境需要修改为实际的后端地址。

#### 2. Token 存储

JWT Token 存储在 `localStorage` 中，关闭浏览器后仍然保留。

#### 3. 自动刷新 Token

当 Token 在 5 分钟内即将过期时，前端会自动调用刷新接口。

## 测试

### 后端测试

```bash
cd backend/blog
mvn test
```

### 前端测试

```bash
cd frontend
npm run test
```

## 常见问题

### 1. 后端启动失败

**问题:** 数据库连接失败

**解决方案:**
- 确认 MySQL 服务已启动
- 检查 `application.properties` 中的数据库配置
- 确认数据库已创建

---

**问题:** 端口 8080 已被占用

**解决方案:**
```bash
# 查找占用端口的进程
lsof -i :8080

# 杀死进程
kill -9 <PID>

# 或者修改端口
# 在 application.properties 中添加：
server.port=8081
```

### 2. 前端启动失败

**问题:** 依赖安装失败

**解决方案:**
```bash
# 清除缓存
npm cache clean --force

# 重新安装
rm -rf node_modules package-lock.json
npm install
```

---

**问题:** 编译错误

**解决方案:**
- 确认 Node.js 版本 >= 16
- 检查是否有语法错误
- 查看控制台错误信息

### 3. 头像上传失败

**问题:** 文件上传返回 413

**解决方案:**
- 检查文件大小是否超过 5MB
- 确认 `application.properties` 中的 `max-file-size` 配置

---

**问题:** 上传成功但访问 404

**解决方案:**
- 确认 `WebConfig.java` 中的静态资源映射配置正确
- 检查 `uploads/` 目录是否存在
- 确认文件路径正确

### 4. JWT Token 相关

**问题:** Token 过期

**解决方案:**
- 确认登录时是否勾选"记住我"
- 检查 Token 有效期配置
- 使用刷新 Token 接口

---

**问题:** Token 验证失败

**解决方案:**
- 确认请求头格式：`Authorization: Bearer {token}`
- 检查 Token 是否正确存储在 localStorage
- 查看后端日志中的错误信息

## 代码规范

### Java 代码规范

- 遵循 Google Java Style Guide
- 类名使用大驼峰命名（PascalCase）
- 方法和变量使用小驼峰命名（camelCase）
- 常量使用全大写加下划线（CONSTANT_CASE）

### JavaScript 代码规范

- 遵循 ESLint 配置
- 使用 2 空格缩进
- 优先使用 const，必要时使用 let
- 使用模板字符串而不是字符串拼接

## Git 工作流

### 分支策略

- `main`: 主分支，保持稳定
- `develop`: 开发分支
- `feature/*`: 功能分支
- `bugfix/*`: 修复分支

### 提交信息规范

```
<type>: <subject>

<body>
```

**Type:**
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

**示例:**
```
feat: 添加头像上传功能

- 实现文件上传接口
- 添加文件验证
- 支持按用户分类存储
```

## 性能优化建议

### 后端

1. 使用数据库索引优化查询
2. 实现 Redis 缓存热点数据
3. 使用连接池管理数据库连接
4. 分页查询大量数据

### 前端

1. 使用懒加载减少初始加载时间
2. 压缩图片和静态资源
3. 使用 CDN 加载第三方库
4. 实现虚拟滚动处理长列表

## 部署

### Docker 部署（推荐）

TODO: 添加 Docker 配置和部署说明

### 传统部署

TODO: 添加传统部署说明

## 更多资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue.js 官方文档](https://vuejs.org/)
- [MySQL 文档](https://dev.mysql.com/doc/)
- [JWT 规范](https://jwt.io/)
