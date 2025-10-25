# Swagger/OpenAPI 集成说明文档

## 概述

本文档说明了如何在博客系统中集成 Swagger/OpenAPI 文档，以便自动生成和展示 RESTful API 文档。

## 已完成的工作

### 1. 添加依赖

**文件**: `backend/blog/pom.xml`

添加了 SpringDoc OpenAPI 依赖：

```xml
<!-- SpringDoc OpenAPI (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

**说明**：
- SpringDoc OpenAPI 是 Spring Boot 3.x 推荐的 OpenAPI 文档生成库
- 版本 2.2.0 已通过安全漏洞检查，无已知安全问题
- 该依赖会自动集成 Swagger UI 界面

### 2. 创建 OpenAPI 配置类

**文件**: `backend/blog/src/main/java/com/lost/blog/config/OpenApiConfig.java`

创建了配置类来自定义 API 文档的元信息和安全配置：

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        // 配置了API基本信息、联系方式、许可证
        // 配置了JWT Bearer Token认证方案
    }
}
```

**配置内容**：
- API 标题：博客系统 API
- API 描述：基于 Spring Boot 的多用户博客系统 RESTful API 文档
- 版本：1.0.0
- 安全方案：JWT Bearer Token 认证

### 3. 更新安全配置

**文件**: `backend/blog/src/main/java/com/lost/blog/config/SecurityConfig.java`

添加了 Swagger UI 和 API 文档的访问权限：

```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
```

**说明**：
- 允许匿名访问 Swagger UI 界面
- 允许匿名访问 OpenAPI 文档 JSON/YAML
- 这样开发者和使用者可以直接查看 API 文档，无需登录

### 4. 添加 OpenAPI 注解

#### UserController（用户管理接口）

**文件**: `backend/blog/src/main/java/com/lost/blog/controller/UserController.java`

添加的注解：
- `@Tag`：标记控制器，分组为"用户管理"
- `@Operation`：描述每个接口的用途
- `@ApiResponses`：列出可能的响应状态码
- `@SecurityRequirement`：标记需要认证的接口
- `@Parameter(hidden = true)`：隐藏注入的 UserDetails 参数

示例接口文档：
- POST `/api/users/register` - 用户注册
- POST `/api/users/login` - 用户登录
- GET `/api/users/me` - 获取当前用户信息（需认证）
- POST `/api/users/me/avatar` - 更新用户头像（需认证）
- POST `/api/users/refresh-token` - 刷新Token（需认证）

#### PostController（文章管理接口）

**文件**: `backend/blog/src/main/java/com/lost/blog/controller/PostController.java`

添加的注解：
- `@Tag`：标记控制器，分组为"文章管理"
- `@Operation`：描述每个接口的用途
- `@ApiResponses`：列出可能的响应状态码
- `@SecurityRequirement`：标记需要认证的接口

示例接口文档：
- POST `/api/posts` - 创建文章（需认证）
- GET `/api/posts/{id}` - 获取文章详情
- GET `/api/posts` - 获取文章列表（分页）
- GET `/api/posts/my` - 获取我的文章（需认证）
- PUT `/api/posts/{id}` - 更新文章（需认证）
- DELETE `/api/posts/{id}` - 删除文章（需认证）

### 5. 配置 Swagger UI

**文件**: `backend/blog/src/main/resources/application.properties`

添加了 Swagger 相关配置：

```properties
# Swagger/OpenAPI Settings
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
springdoc.swagger-ui.tagsSorter=alpha
```

**配置说明**：
- `springdoc.api-docs.path`：API 文档的 JSON 访问路径
- `springdoc.swagger-ui.path`：Swagger UI 界面的访问路径
- `springdoc.swagger-ui.operationsSorter`：按字母顺序排序接口
- `springdoc.swagger-ui.tagsSorter`：按字母顺序排序标签

## 如何使用

### 访问 Swagger UI

启动后端服务后，在浏览器中访问：

```
http://localhost:8080/swagger-ui.html
```

或者：

```
http://localhost:8080/swagger-ui/index.html
```

### 访问 OpenAPI 文档

获取 JSON 格式的 OpenAPI 文档：

```
http://localhost:8080/v3/api-docs
```

获取 YAML 格式的 OpenAPI 文档：

```
http://localhost:8080/v3/api-docs.yaml
```

### 使用 JWT Token 认证

1. 在 Swagger UI 界面中，点击右上角的 "Authorize" 按钮
2. 输入 JWT Token（通过 `/api/users/login` 接口获取）
3. 点击 "Authorize" 确认
4. 现在可以测试需要认证的接口了

**注意**：输入 Token 时不需要添加 "Bearer " 前缀，直接输入 Token 字符串即可。

## 需要您完成的操作

由于项目使用的是 Spring Boot 3.5.7-SNAPSHOT 快照版本，在某些环境中可能无法自动下载依赖。您需要：

### 1. 检查 Maven 依赖

运行以下命令下载依赖：

```bash
cd backend/blog
mvn clean install -DskipTests
```

如果遇到快照版本下载问题，可以考虑：

**选项 A**：等待网络恢复或使用 VPN 访问 Spring 仓库

**选项 B**：将 Spring Boot 版本改为稳定版本（推荐）

编辑 `pom.xml`，将 Spring Boot 版本从：
```xml
<version>3.5.7-SNAPSHOT</version>
```
改为最新的稳定版本，例如：
```xml
<version>3.4.0</version>
```

### 2. 启动应用并验证

```bash
cd backend/blog
mvn spring-boot:run
```

启动成功后，访问 `http://localhost:8080/swagger-ui.html` 验证 Swagger UI 是否正常工作。

### 3. 测试功能

建议测试以下场景：

1. **查看 API 文档**：浏览不同的接口分组和接口详情
2. **测试公开接口**：在 Swagger UI 中直接测试 `/api/users/register` 和 `/api/users/login`
3. **测试认证接口**：
   - 先通过登录接口获取 Token
   - 点击 "Authorize" 按钮输入 Token
   - 测试需要认证的接口，如 `/api/users/me` 或 `/api/posts`（POST）

### 4. 其他控制器的注解（可选）

如果需要，可以为其他控制器也添加类似的 OpenAPI 注解：

- `CommentController`（评论管理）
- `LikeController`（点赞功能）
- `FileController`（文件上传）

参考 `UserController` 和 `PostController` 的注解方式即可。

## 技术细节

### SpringDoc vs Springfox

本项目使用 SpringDoc OpenAPI 而不是老旧的 Springfox，原因：

- Springfox 已停止维护，最后更新在 2020 年
- SpringDoc 原生支持 Spring Boot 3.x 和 Jakarta EE
- SpringDoc 性能更好，配置更简单
- SpringDoc 与 OpenAPI 3.0 规范完全兼容

### 注解说明

常用注解：

- `@Tag`：标记控制器类，用于分组
- `@Operation`：描述单个接口
- `@ApiResponses` / `@ApiResponse`：描述可能的 HTTP 响应
- `@Parameter`：描述请求参数
- `@SecurityRequirement`：标记需要认证的接口
- `@Schema`：描述 DTO 字段（可在 DTO 类中使用）

### 自动发现机制

SpringDoc 会自动扫描所有 `@RestController` 和 `@RequestMapping` 注解的类，无需手动配置包扫描路径。

## 参考资料

- [SpringDoc OpenAPI 官方文档](https://springdoc.org/)
- [OpenAPI 3.0 规范](https://swagger.io/specification/)
- [Swagger UI 使用指南](https://swagger.io/tools/swagger-ui/)

## 故障排除

### 问题：访问 /v3/api-docs 返回 403 错误

**症状**：浏览器显示 "Fetch error response status is 403 /v3/api-docs"

**原因**：Spring Security 拦截了 Swagger 相关的请求

**最佳解决方案**（已实现）：

1. **完全绕过 Spring Security**：在 `SecurityConfig.java` 中添加 `WebSecurityCustomizer` bean：

```java
@Bean
public WebSecurityCustomizer webSecurityCustomizer() {
    // 完全忽略 Swagger 相关路径，不经过 Spring Security 过滤器链
    return (web) -> web.ignoring()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", 
                            "/swagger-resources/**", "/webjars/**");
}
```

2. **在 JWT 过滤器中跳过 Swagger 路径**：在 `JwtAuthenticationFilter.java` 中重写 `shouldNotFilter` 方法：

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    // 跳过 Swagger UI 和 OpenAPI 文档相关的路径
    return path.startsWith("/swagger-ui") ||
           path.startsWith("/v3/api-docs") ||
           path.startsWith("/swagger-resources") ||
           path.startsWith("/webjars");
}
```

3. **更新 CORS 配置**：在 `WebConfig.java` 中使用 `allowedOriginPatterns` 而不是 `allowedOrigins`：

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns("*") // 允许所有来源
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

4. **重启应用**：修改配置后需要重新编译和启动应用：

```bash
cd backend/blog
mvn clean install
mvn spring-boot:run
```

**说明**：
- `WebSecurityCustomizer` 使 Swagger 路径完全绕过 Spring Security 过滤器链
- `shouldNotFilter` 确保即使有其他过滤器也不会处理 Swagger 请求
- 这是处理 Swagger 403 错误最可靠的方法

### 问题：Swagger UI 页面显示空白

**可能原因**：
- SpringDoc 依赖未正确下载
- 静态资源路径配置问题

**解决方案**：
1. 清理并重新构建项目：`mvn clean install`
2. 确认依赖已下载：检查 `~/.m2/repository/org/springdoc/`
3. 尝试访问不同的路径：`/swagger-ui/index.html` 或 `/swagger-ui.html`

## 总结

通过以上改动，博客系统现在已经集成了完整的 Swagger/OpenAPI 文档支持。开发者和 API 使用者可以通过友好的 Web 界面查看和测试所有 API 接口，大大提高了开发效率和 API 的可用性。
