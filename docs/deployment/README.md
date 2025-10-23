# 部署文档

本目录包含博客系统的部署和运维相关文档。

## 📚 文档列表

### 部署指南
- **[Docker部署指南](./DOCKER.md)** - 使用 Docker 和 Docker Compose 部署系统的完整指南
  - 前置准备（服务器要求、安装 Docker）
  - 部署步骤（环境配置、构建、启动）
  - HTTPS 配置（SSL 证书、自动续期）
  - 安全设置（防火墙、数据库备份）
  - 常见问题排查

### 运维指南
- **[日常维护指南](./MAINTENANCE.md)** - 部署后的日常运维操作手册
  - 查看服务状态和日志
  - 更新应用代码
  - 重启和停止服务
  - 进入容器调试
  - 修改配置文件
  - 数据库操作（备份、恢复等）
  - 性能监控
  - 清理 Docker 资源

### 部署要点
- **[部署说明](./NOTES.txt)** - 部署相关的重要提醒和快速参考

## 🚀 快速开始

如果你是第一次部署，建议按照以下顺序阅读：

1. 先阅读 [Docker部署指南](./DOCKER.md) 完成初始部署
2. 部署成功后，参考 [日常维护指南](./MAINTENANCE.md) 进行日常运维
3. 查看 [部署说明](./NOTES.txt) 了解关键要点

## 💡 部署方式对比

### Docker 部署（推荐）
- ✅ 一键启动，简单快速
- ✅ 环境隔离，避免依赖冲突
- ✅ 易于扩展和迁移
- ⚠️ 需要安装 Docker 和 Docker Compose

### 传统部署
- ✅ 资源占用相对较低
- ✅ 适合传统服务器环境
- ⚠️ 需要手动安装和配置各个服务
- ⚠️ 环境依赖管理较复杂

## 📋 环境变量配置

所有敏感配置都通过环境变量注入，主要配置项包括：

- **数据库配置**
  - `MYSQL_ROOT_PASSWORD` - MySQL root 密码
  - `DATABASE_USERNAME` - 数据库用户名
  - `DATABASE_PASSWORD` - 数据库密码
  
- **JWT 配置**
  - `JWT_SECRET` - JWT 密钥（必须使用 `openssl rand -base64 64` 生成）
  - `JWT_EXPIRATION_MS` - Token 过期时间（默认 1 小时）
  - `JWT_REMEMBER_ME_EXPIRATION_MS` - "记住我" Token 过期时间（默认 30 天）

- **应用配置**
  - `DDL_AUTO` - 数据库表管理模式（首次部署用 `update`，之后改为 `validate`）
  - `COMMENT_MAX_NESTING_LEVEL` - 评论最大嵌套层级（可选，默认 3）

详细配置说明请查看项目根目录的 `.env.docker.example` 文件。

## ⚠️ 安全提醒

1. **不要使用示例密码和密钥**
   - 所有密码和 JWT 密钥都必须自行生成
   - 不要将 `.env` 文件提交到版本控制系统

2. **首次部署后修改配置**
   - 数据库表创建完成后，将 `DDL_AUTO` 改为 `validate`
   - 配置 HTTPS 加密传输
   - 设置定期数据库备份

3. **防火墙配置**
   - 只开放必要的端口（80、443）
   - 不要开放 MySQL 端口（3306）给外网
   - 限制 SSH 访问

## 🆘 获取帮助

如果在部署过程中遇到问题：

1. 查看日志：`docker-compose logs -f`
2. 检查容器状态：`docker-compose ps`
3. 参考 [Docker部署指南](./DOCKER.md) 中的"常见问题排查"章节
4. 在 GitHub Issues 中提问

## 📝 相关文档

- [项目主 README](../../README.md)
- [架构说明](../ARCHITECTURE.md)
- [开发指南](../DEVELOPMENT.md)
- [API 文档](../API.md)
