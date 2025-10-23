# 问题解决方案总结

## 问题描述

部署时遇到端口冲突：
```
ERROR: for blog-frontend Cannot start service frontend: 
failed to bind host port for 0.0.0.0:80:172.18.0.4:80/tcp: address already in use
```

原因：服务器上的系统 Nginx 已经占用了 80 端口（用于 HTTPS 和证书管理）。

## 解决方案

### 1. 修改 Docker 配置

**文件**：`docker-compose.yml`

**修改内容**：
```yaml
  frontend:
    ports:
      - "8081:80"  # 从 "80:80" 改为 "8081:80"
```

**说明**：
- 前端容器内部仍使用 80 端口
- 映射到宿主机的 8081 端口
- 移除了 SSL 证书和 certbot 的 volume 挂载（由系统 Nginx 管理）

### 2. 配置系统 Nginx

需要在服务器上配置系统 Nginx 作为反向代理：

**创建配置文件**：`/etc/nginx/sites-available/blog`

**核心配置**：
```nginx
server {
    listen 80;
    listen 443 ssl http2;
    server_name myblogsystem.icu;

    # SSL 证书配置
    ssl_certificate /etc/letsencrypt/live/myblogsystem.icu/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/myblogsystem.icu/privkey.pem;

    # 前端代理（注意端口是 8081）
    location / {
        proxy_pass http://127.0.0.1:8081;
        ...
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        ...
    }

    # Let's Encrypt 证书验证
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
}
```

**详细配置步骤**：见 `docs/PORT_CONFLICT_SOLUTION.md`

## 文档整理

### 删除的文档

根据你的要求，删除了所有不必要的功能文档：

- ❌ `docs/features/` 整个目录及其内容：
  - EDITOR.md
  - REMEMBER_ME.md
  - AVATAR_UPLOAD.md
  - LIKES.md
  - COMMENTS.md
  - SUB_COMMENTS.md
- ❌ `docs/DEVELOPMENT.md`
- ❌ `docs/SUMMARY.md`（没用的 summary）
- ❌ `docs/deployment/README.md`

### 保留的核心文档

✅ **根目录**：
- `README.md` - 项目主文档（已更新）

✅ **docs/**：
- `API.md` - API 接口文档
- `ARCHITECTURE.md` - 架构说明
- `PORT_CONFLICT_SOLUTION.md` - **新增**：端口冲突解决方案

✅ **docs/deployment/**：
- `DOCKER.md` - Docker 部署指南
- `MAINTENANCE.md` - 日常维护指南
- `NOTES.txt` - 快速参考手册（已更新为精简版）

✅ **frontend/**：
- `README.md` - 前端项目说明（已更新为精简版）

### 文档组织结构

```
blog-project/
├── README.md                          # 主文档
├── docker-compose.yml                 # Docker 配置（已修改）
├── frontend/
│   └── README.md                      # 前端说明（已精简）
└── docs/
    ├── API.md                         # API 文档
    ├── ARCHITECTURE.md                # 架构说明
    ├── PORT_CONFLICT_SOLUTION.md      # 端口冲突解决（新增）
    └── deployment/
        ├── DOCKER.md                  # 部署指南
        ├── MAINTENANCE.md             # 维护指南
        └── NOTES.txt                  # 快速参考
```

## 部署步骤

### 1. 更新代码

```bash
cd /path/to/blog-project
git pull
```

### 2. 配置系统 Nginx

```bash
# 创建配置文件
sudo nano /etc/nginx/sites-available/blog
# 粘贴配置内容（见 docs/PORT_CONFLICT_SOLUTION.md）

# 启用配置
sudo ln -sf /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/blog
sudo rm -f /etc/nginx/sites-enabled/default

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 3. 停止旧容器

```bash
docker-compose down
```

### 4. 启动新配置的容器

```bash
docker-compose up -d --build
```

### 5. 验证部署

```bash
# 检查容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 测试访问
curl -I https://myblogsystem.icu
```

## 优势

这个方案的优点：

1. ✅ **解决端口冲突**：系统 Nginx 使用 80/443，容器使用其他端口
2. ✅ **HTTPS 集中管理**：所有 SSL 配置在系统 Nginx
3. ✅ **证书续期方便**：certbot 可以直接续期，不需要停止容器
4. ✅ **性能更好**：系统 Nginx 直接处理静态文件和缓存
5. ✅ **文档清晰**：只保留必要的核心文档，易于维护

## 后续维护

### 查看日志

```bash
# 系统 Nginx 日志
sudo tail -f /var/log/nginx/error.log

# 容器日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 更新应用

```bash
git pull
cd backend/blog && ./mvnw clean package -DskipTests && cd ../..
docker-compose up -d --build
```

### 证书续期

```bash
# 测试续期（不会真正续期）
sudo certbot renew --webroot -w /var/www/certbot --dry-run

# 实际续期
sudo certbot renew --webroot -w /var/www/certbot
docker-compose restart frontend
```

## 常见问题

**Q: 为什么不直接停掉系统 Nginx？**

A: 系统 Nginx 管理着 HTTPS 证书和自动续期，停掉会导致证书续期失败。使用反向代理是最佳实践。

**Q: 可以修改其他端口吗？**

A: 可以。只需修改 `docker-compose.yml` 中的端口映射，并相应修改系统 Nginx 的 `proxy_pass` 配置。

**Q: 如何回滚？**

A: 停止容器，恢复旧的配置文件，停止系统 Nginx，重新启动容器。

## 参考文档

- 详细步骤：`docs/PORT_CONFLICT_SOLUTION.md`
- Docker 部署：`docs/deployment/DOCKER.md`
- 日常维护：`docs/deployment/MAINTENANCE.md`
- 快速参考：`docs/deployment/NOTES.txt`
