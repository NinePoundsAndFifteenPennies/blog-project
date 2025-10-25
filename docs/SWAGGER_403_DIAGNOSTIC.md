# Swagger 403 错误诊断指南

由于你遇到了持续的 403 错误，本文档提供了详细的诊断步骤来帮助解决问题。

## 最新修复 (Commit 80b57f3)

使用了更明确的 `AntPathRequestMatcher` 来匹配 Swagger 路径，并启用了调试日志。

## 诊断步骤

### 1. 清理并重新构建

```bash
cd backend/blog
mvn clean
rm -rf target/
mvn install -DskipTests
```

### 2. 启动应用并查看日志

```bash
mvn spring-boot:run
```

**重要**：应用启动后，请查看控制台输出，寻找以下关键信息：

- Spring Security 的过滤器链信息
- Swagger/SpringDoc 的初始化日志
- 访问 Swagger UI 时的安全调试日志

### 3. 测试不同的 URL

依次尝试访问以下 URL，并记录每个 URL 的结果：

1. `http://localhost:8080/swagger-ui/index.html`
2. `http://localhost:8080/swagger-ui.html`
3. `http://localhost:8080/v3/api-docs`
4. `http://localhost:8080/`

### 4. 检查浏览器开发者工具

打开浏览器开发者工具（F12），切换到 Network 标签，然后访问 Swagger UI：

**需要提供的信息**：
- 失败的请求 URL（完整的）
- 响应状态码
- 响应头信息
- 请求头信息
- 响应正文（如果有）

### 5. 检查控制台日志

当访问 Swagger UI 出现 403 错误时，请从控制台复制以下日志：

**查找包含这些关键词的日志**：
- `SecurityFilterChain`
- `JwtAuthenticationFilter`
- `swagger-ui`
- `v3/api-docs`
- `403` 或 `Forbidden`

### 6. 验证配置文件

确认 `application.properties` 文件中有以下配置：

```properties
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
logging.level.org.springframework.security=DEBUG
logging.level.org.springdoc=DEBUG
```

### 7. 检查 Spring Boot 版本

你的项目使用 Spring Boot 3.5.7-SNAPSHOT（快照版本），这可能导致兼容性问题。

**建议**：修改 `pom.xml` 中的 Spring Boot 版本为稳定版本：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version> <!-- 改为稳定版本 -->
    <relativePath/>
</parent>
```

修改后重新构建：
```bash
mvn clean install
```

## 常见问题和解决方案

### 问题 1：端口被占用

**症状**：应用无法启动，提示端口 8080 被占用

**解决方案**：
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### 问题 2：数据库连接失败

**症状**：应用启动时报错，无法连接到 MySQL

**解决方案**：
1. 确认 MySQL 服务正在运行
2. 确认数据库 `blog` 已创建
3. 检查 `application.properties` 中的数据库用户名和密码

### 问题 3：依赖下载失败

**症状**：Maven 构建失败，无法下载 Spring Boot SNAPSHOT 依赖

**解决方案**：将 Spring Boot 版本改为稳定版本（见上文第 7 步）

## 需要提供的诊断信息

如果问题仍未解决，请提供以下信息：

1. **完整的启动日志**（从运行 `mvn spring-boot:run` 到应用启动完成）
2. **访问 Swagger UI 时的完整日志**（包括 DEBUG 级别的日志）
3. **浏览器开发者工具截图**：
   - Network 标签中失败请求的详细信息
   - Console 标签中的错误信息
4. **Spring Boot 版本**（pom.xml 中的版本号）
5. **Java 版本**（运行 `java -version` 的输出）
6. **操作系统**（Windows/Mac/Linux）

## 替代测试方法

如果 Swagger UI 仍然无法访问，可以使用以下方法测试 API 文档是否生成：

### 使用 curl 测试

```bash
# 测试 OpenAPI JSON 文档
curl -v http://localhost:8080/v3/api-docs

# 应该返回 200 状态码和 JSON 文档
```

如果 curl 返回 200，说明文档生成成功，问题可能在 Swagger UI 的静态资源上。

### 直接访问 OpenAPI 文档

在浏览器中访问：`http://localhost:8080/v3/api-docs`

应该看到 JSON 格式的 OpenAPI 文档。如果能看到，说明：
- SpringDoc 配置正确
- 安全配置对 `/v3/api-docs` 已放行
- 问题可能在 Swagger UI 的静态资源路径上

## 最后的检查清单

- [ ] Maven 清理并重新构建
- [ ] 使用稳定版本的 Spring Boot（不是 SNAPSHOT）
- [ ] 确认 MySQL 数据库正在运行
- [ ] 确认应用能够成功启动
- [ ] 查看启动日志中的 SpringDoc 初始化信息
- [ ] 尝试访问 `/v3/api-docs` 端点
- [ ] 查看浏览器开发者工具的 Network 标签
- [ ] 提供完整的错误日志

## 联系支持

如果完成以上所有步骤后问题仍未解决，请在 GitHub Issue 或 PR 评论中提供上述诊断信息，我们会进一步协助你解决问题。
