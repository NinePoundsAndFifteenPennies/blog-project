# 端口冲突解决方案

## 问题描述

部署时出现端口冲突错误：
```
ERROR: for blog-frontend Cannot start service frontend: failed to set up container networking: 
driver failed programming external connectivity on endpoint blog-frontend: 
failed to bind host port for 0.0.0.0:80:172.18.0.4:80/tcp: address already in use
```

原因：服务器上的系统 Nginx 已经占用了 80 端口（用于 HTTPS 和证书管理）。

## 解决方案

使用系统 Nginx 作为反向代理，Docker 容器使用内部端口 8081。

**重要说明**：
- ✅ **你的域名继续有效**：系统 Nginx 继续监听 80/443 端口，域名访问不受影响
- ✅ **证书自动续期正常**：certbot 通过系统 Nginx 的 webroot 方式续期，不需要停止任何服务
- ✅ **HTTPS 继续工作**：所有 SSL 配置保持在系统 Nginx，无需修改证书配置

### 步骤 1：修改 docker-compose.yml

编辑 `docker-compose.yml`，修改前端容器的端口映射：

```yaml
  # 前端应用
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: blog-frontend
    restart: always
    depends_on:
      - backend
    ports:
      - "8081:80"  # 改为 8081:80，不直接使用 80 端口
    networks:
      - blog-network
```

**说明**：前端容器内部使用 80 端口，映射到宿主机的 8081 端口。证书文件由系统 Nginx 直接访问，不需要挂载到容器。

### 步骤 2：配置系统 Nginx

编辑系统 Nginx 配置文件：

```bash
sudo nano /etc/nginx/sites-available/blog
```

添加以下配置：

```nginx
server {
    listen 80;
    listen 443 ssl http2;
    server_name myblogsystem.icu;  # 改为你的域名

    # SSL 证书配置
    ssl_certificate /etc/letsencrypt/live/myblogsystem.icu/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/myblogsystem.icu/privkey.pem;

    # HTTP 自动跳转 HTTPS
    if ($scheme = http) {
        return 301 https://$host$request_uri;
    }

    # Let's Encrypt 证书验证
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # 前端页面代理
    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 步骤 3：启用配置并重启 Nginx

```bash
# 创建软链接启用配置
sudo ln -sf /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/blog

# 删除默认配置（如果存在）
sudo rm -f /etc/nginx/sites-enabled/default

# 测试配置是否正确
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 步骤 4：启动 Docker 容器

```bash
cd /path/to/blog-project

# 停止旧容器（如果正在运行）
docker-compose down

# 启动新配置的容器
docker-compose up -d --build
```

### 步骤 5：验证部署

```bash
# 检查容器状态
docker-compose ps

# 检查 Nginx 状态
sudo systemctl status nginx

# 测试访问
curl -I https://myblogsystem.icu
```

## 证书自动续期配置

**重要**：使用这个方案后，证书续期变得更简单，因为不需要停止任何容器。

### 证书续期方式

系统 Nginx 配置中已经包含了 `/.well-known/acme-challenge/` 路径，certbot 可以直接通过这个路径验证域名所有权。

```bash
# 测试证书续期（不会真正续期）
sudo certbot renew --dry-run

# 实际续期
sudo certbot renew
```

### 自动续期设置

如果还没有设置自动续期，可以添加定时任务：

```bash
# 编辑 crontab
sudo crontab -e

# 添加以下行（每天凌晨 2 点检查并续期）
0 2 * * * certbot renew --quiet && systemctl reload nginx
```

**说明**：
- certbot 会自动检查证书是否需要续期（30天内过期才续期）
- 续期后自动重新加载 Nginx 配置
- 整个过程不需要停止任何 Docker 容器
- 你的域名 `myblogsystem.icu` 会一直有效

## 配置说明

### 端口分配
- **80/443**：系统 Nginx（处理 HTTPS 和反向代理）
- **8080**：后端容器（直接暴露）
- **8081**：前端容器（通过 Nginx 代理）
- **3306**：MySQL 容器（仅内部网络访问）

### 访问流程
1. 用户访问 `https://myblogsystem.icu`
2. 请求到达系统 Nginx（80/443 端口）
3. Nginx 将请求代理到：
   - 前端静态文件：`http://127.0.0.1:8081`
   - API 请求：`http://127.0.0.1:8080`
4. Docker 容器处理请求并返回响应

### 优势
- **解决端口冲突**：系统 Nginx 使用 80/443，容器使用其他端口
- **HTTPS 集中管理**：所有 SSL 证书在系统 Nginx 配置
- **证书自动续期不受影响**：certbot 可以直接续期，不需要停止容器
- **性能更好**：系统 Nginx 直接处理静态文件缓存

## 常见问题

### Q1: 如何查看 Nginx 日志？
```bash
# 系统 Nginx 日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# 容器日志
docker-compose logs -f frontend
docker-compose logs -f backend
```

### Q2: 修改配置后如何生效？
```bash
# 修改 docker-compose.yml 后
docker-compose up -d --build

# 修改系统 Nginx 配置后
sudo nginx -t
sudo systemctl reload nginx
```

### Q3: 如何回滚到之前的配置？
如果遇到问题，可以快速回滚：

```bash
# 停止容器
docker-compose down

# 恢复 docker-compose.yml 中的端口映射为 "80:80"
# 停止系统 Nginx
sudo systemctl stop nginx

# 重新启动容器
docker-compose up -d
```

## 防火墙配置

确保防火墙允许必要的端口：

```bash
# 开放 HTTP/HTTPS 端口（系统 Nginx 使用）
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 可选：如果需要直接访问后端 API
sudo ufw allow 8080/tcp

# 重新加载防火墙
sudo ufw reload
sudo ufw status
```

## 总结

这个方案的核心思想是：
1. **系统 Nginx** 作为流量入口，处理 HTTPS 和反向代理
2. **Docker 容器** 使用非标准端口，避免冲突
3. **所有外部流量** 都通过系统 Nginx 转发

这是最佳实践，也是生产环境推荐的部署方式。
