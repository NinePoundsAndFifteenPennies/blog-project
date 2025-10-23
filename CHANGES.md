# 项目更新说明

本次更新解决了 Docker 部署时的端口冲突问题，并清理了文档结构。

## 🔧 主要变更

### 1. 解决端口冲突问题

**问题**：服务器上已有 nginx 占用 80 和 443 端口，导致 Docker 容器无法启动

**解决方案**：
- 修改 `docker-compose.yml`，将前端端口从 `80:80` 和 `443:443` 改为 `8000:80`
- 移除 SSL 证书卷挂载（不再在容器内处理 HTTPS）
- 简化 `frontend/nginx.conf`，移除 SSL 配置

**现在的访问方式**：
- 前端：`http://你的服务器IP:8000`
- 后端 API：`http://你的服务器IP:8080/api`

**配置 HTTPS 的推荐方式**：
使用服务器上的 nginx 作为反向代理：
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    location / {
        proxy_pass http://localhost:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

然后使用 Let's Encrypt：
```bash
sudo certbot --nginx -d yourdomain.com
```

### 2. 文档清理

**删除的文档**：
- `docs/SUMMARY.md` - 不必要的总结文档
- `docs/DEVELOPMENT.md` - 开发说明（内容已整合到主 README）
- `docs/features/` - 所有功能特性文档（6个文件）
- `docs/deployment/README.md` - 部署索引（简化为直接阅读具体文档）
- `docs/deployment/NOTES.txt` - 部署笔记

**保留的文档**：
- `README.md` - 项目主页（已更新）
- `docs/API.md` - API 文档
- `docs/ARCHITECTURE.md` - 架构说明
- `docs/deployment/DOCKER.md` - Docker 部署指南（已更新）
- `docs/deployment/MAINTENANCE.md` - 日常维护指南
- `frontend/README.md` - 前端文档（已更新）

### 3. 文档更新

**主 README.md**：
- 更新了部署说明，指明前端运行在 8000 端口
- 添加了重要提示，说明如何处理端口冲突和 HTTPS
- 简化了文档导航链接
- 移除了对已删除文档的引用

**docs/deployment/DOCKER.md**：
- 更新了服务端口说明（8000 而不是 80）
- 添加了"配置域名和HTTPS"章节，推荐使用服务器 nginx
- 简化了 SSL 配置说明（移除了容器内 SSL 的复杂配置）
- 更新了故障排查章节，添加了"端口被占用"问题的解决方案

**frontend/README.md**：
- 添加了"Docker 部署"章节
- 说明了端口配置和 HTTPS 配置方法
- 添加了部署文档的链接

## 📊 变更统计

- **修改的文件**：5个
- **删除的文件**：10个
- **删除的行数**：约 3600 行
- **新增的行数**：约 150 行

## 🎯 升级指南

如果你已经部署了旧版本，按以下步骤升级：

### 1. 停止当前运行的容器

```bash
cd /path/to/blog-project
docker-compose down
```

### 2. 拉取最新代码

```bash
git pull
```

### 3. 重新构建并启动

```bash
# 如果后端代码有更新，重新构建
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 启动所有服务
docker-compose up -d --build
```

### 4. 配置服务器 nginx（如果需要使用 80/443 端口和 HTTPS）

参考上面的 nginx 配置示例和 `docs/deployment/DOCKER.md` 中的详细说明。

## ❓ 常见问题

**Q: 为什么要改用 8000 端口？**

A: 因为很多服务器已经安装了 nginx 用于其他网站或 SSL 证书管理，它们占用了 80 和 443 端口。使用 8000 端口可以避免冲突，然后通过服务器 nginx 反向代理来提供标准的 HTTP/HTTPS 服务。

**Q: 我可以继续使用 80 端口吗？**

A: 可以。如果你的服务器上没有其他服务占用 80 端口，可以修改 `docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "80:80"
```

**Q: 之前的 SSL 证书配置怎么办？**

A: 现在推荐的做法是在服务器上使用 nginx 处理 SSL，而不是在 Docker 容器内。这样更灵活，也更容易管理证书续期。详细步骤见 `docs/deployment/DOCKER.md`。

**Q: 文档删除了，我怎么了解各个功能？**

A: 主要功能都在主 README.md 的"核心功能"章节有说明。具体的 API 使用方法请查看 `docs/API.md`。如果需要了解某个功能的实现细节，可以直接查看源代码。

## 📝 更新日期

2025-10-23
