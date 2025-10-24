# Docker 部署指南

本指南专门针对使用 Docker 将博客系统部署到租用服务器的场景。按照本指南操作，你可以让博客系统在服务器上持续运行，并对外提供服务。

## 🏗️ 统一的部署架构

本项目采用**系统 Nginx 作为唯一公网入口**的部署架构，确保安全性和可维护性：

### 端口分配表

| 服务 | 端口 | 绑定地址 | 说明 |
|------|------|----------|------|
| **系统 Nginx** | 80/443 | 0.0.0.0 | 公网访问入口，处理 HTTPS |
| **前端容器** | 8081 | 127.0.0.1 | 仅本机访问，由系统 Nginx 反代 |
| **后端容器** | 8080 | 127.0.0.1 | 仅本机访问，由系统 Nginx 反代 |
| **MySQL 容器** | 3306 | 127.0.0.1 | 仅本机访问，不对外暴露 |

### 架构图

```
互联网
   ↓
系统 Nginx (80/443)
   ├─→ / → 前端容器 (127.0.0.1:8081)
   ├─→ /api → 后端容器 (127.0.0.1:8080)
   └─→ /uploads → 后端容器 (127.0.0.1:8080)
```

### 核心原则

1. **单一入口**：系统 Nginx 为唯一公网入口，统一处理 80/443 端口
2. **容器隔离**：所有容器端口仅绑定 127.0.0.1，不直接暴露到公网
3. **证书管理**：证书由系统 certbot 管理，不挂载到容器内
4. **数据持久化**：上传文件通过宿主目录 `./data/uploads` 挂载到后端容器

## 📋 前置准备

### 1. 服务器要求

- **操作系统**: Linux 系统（推荐 Ubuntu 20.04+ 或 CentOS 7+）
- **内存**: 至少 2GB RAM
- **磁盘空间**: 至少 10GB 可用空间
- **域名**: 必须有域名（HTTPS 证书需要域名，**禁止用 IP 访问 HTTPS**）

### 2. 域名配置

在部署前，请确保：

1. **购买域名**：如 `myblogsystem.icu`
2. **配置 DNS A 记录**：将域名指向服务器公网 IP
3. **域名生效验证**：
   ```bash
   ping myblogsystem.icu
   # 确认返回的 IP 是你的服务器 IP
   ```

**重要提示**：
- 域名、DNS、Nginx `server_name`、证书 CN/SAN 必须完全一致
- 如需支持 `www` 子域，需同时配置 `www.myblogsystem.icu` 的 A 记录
- 建议使用 301 重定向将 www 统一到主域名或反之

### 3. 安装系统 Nginx

系统 Nginx 作为公网入口，必须安装在宿主机上：

```bash
# 安装 Nginx
sudo apt update
sudo apt install nginx -y

# 验证安装
nginx -v

# 确认 Nginx 正在运行
sudo systemctl status nginx
```

### 4. 安装 Docker 和 Docker Compose

```bash
# 更新系统软件包
sudo apt update && sudo apt upgrade -y

# 安装 Docker（使用官方一键安装脚本）
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 将当前用户添加到 docker 组（避免每次都需要 sudo）
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo apt install docker-compose -y

# 验证安装
docker --version
docker-compose --version
```

**注意**: 执行 `usermod` 命令后需要退出并重新登录，或者执行 `newgrp docker` 使组权限生效。

## 🚀 部署步骤

### 第一步：获取项目代码

将项目代码上传到服务器。你可以使用以下任一方式：

#### 方式 A：使用 Git（推荐）

```bash
# 安装 git（如果未安装）
sudo apt install git -y

# 克隆项目到服务器
git clone https://github.com/NinePoundsAndFifteenPennies/blog-project.git
cd blog-project
```

#### 方式 B：直接上传

使用 SCP、SFTP 或其他工具将项目文件夹上传到服务器。

### 第二步：配置环境变量

项目需要一些敏感配置信息，这些信息不应该提交到版本控制系统中。

```bash
# 进入项目目录
cd blog-project

# 复制环境变量模板文件
cp .env.docker.example .env

# 编辑环境变量文件
nano .env
```

在打开的编辑器中，你需要修改以下内容：

```bash
# === MySQL 数据库配置 ===
MYSQL_ROOT_PASSWORD=你的MySQL根密码       # 修改为强密码
DATABASE_USERNAME=bloguser                # 可以保持不变
DATABASE_PASSWORD=你的数据库密码          # 修改为强密码

# === JWT 密钥配置 ===
JWT_SECRET=你生成的JWT密钥               # 见下方生成方法
JWT_EXPIRATION_MS=3600000
JWT_REMEMBER_ME_EXPIRATION_MS=2592000000

# === 数据库表管理配置 ===
DDL_AUTO=update                          # 首次部署保持 update，后续改为 validate
```

#### 生成 JWT 密钥

JWT 密钥必须是一个安全的随机字符串。在服务器上运行以下命令生成：

```bash
openssl rand -base64 64
```

将输出的字符串复制到 `.env` 文件的 `JWT_SECRET` 配置项中。

**示例输出**:
```
DFoin7uuuVzunje2OpBpZBWdTFRKrU0ran1U6zu2fuk3mfRyqzzLUTXvGDZkK1xGg3Xwyz9Eo398H1tpXkjtWA==
```

**安全提示**：
- 请务必使用你自己生成的密钥，不要使用示例中的密钥
- 不要将 `.env` 文件提交到 Git 仓库
- 妥善保管你的密码和密钥

### 第三步：构建后端应用

Docker 部署需要先构建后端的 JAR 文件：

```bash
# 进入后端目录
cd backend/blog

# 使用 Maven 构建项目（跳过测试以加快速度）
./mvnw clean package -DskipTests

# 返回项目根目录
cd ../..
```

**注意**：如果服务器上没有安装 Java 和 Maven，不用担心，`mvnw` 脚本会自动下载所需的 Maven 版本。

构建成功后，你会在 `backend/blog/target/` 目录下看到 `blog-0.0.1-SNAPSHOT.jar` 文件。

**如果构建失败**：

如果遇到依赖下载问题（如 Spring snapshot 版本无法访问），可以尝试：

1. 检查服务器网络连接
2. 配置 Maven 使用国内镜像（阿里云镜像）

创建或编辑 `~/.m2/settings.xml` 文件：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun-maven</id>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

然后重新运行构建命令。

### 第四步：创建上传目录

为头像和其他上传文件创建持久化存储目录：

```bash
# 在项目根目录创建 data/uploads 目录
mkdir -p data/uploads

# 确保目录权限正确
chmod 755 data/uploads
```

这个目录将被挂载到后端容器的 `/app/uploads`，确保上传的文件持久保存在宿主机上。

### 第五步：启动 Docker 容器

现在可以一键启动所有服务（MySQL 数据库、后端应用、前端应用）：

```bash
# 确保在项目根目录
cd /path/to/blog-project

# 使用 Docker Compose 启动所有服务（后台运行）
docker-compose up -d
```

这个命令会：
1. 自动构建前端和后端的 Docker 镜像
2. 启动 MySQL 数据库容器
3. 启动后端应用容器（端口 127.0.0.1:8080）
4. 启动前端应用容器（端口 127.0.0.1:8081）
5. 自动创建所需的网络和数据卷

**首次启动说明**：
- 首次启动时，MySQL 需要初始化，后端应用会等待 MySQL 就绪后才启动
- 后端应用会自动创建数据库表（因为 `DDL_AUTO=update`）
- 整个过程可能需要 1-3 分钟，请耐心等待

### 第六步：查看服务状态

```bash
# 查看所有容器的运行状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 只查看后端日志
docker-compose logs -f backend

# 只查看前端日志
docker-compose logs -f frontend

# 只查看数据库日志
docker-compose logs -f mysql
```

如果所有服务都正常启动，你会看到类似这样的输出：

```
NAME                IMAGE                       STATUS              PORTS
blog-backend        blog-project-backend        Up 2 minutes        127.0.0.1:8080->8080/tcp
blog-frontend       blog-project-frontend       Up 2 minutes        127.0.0.1:8081->80/tcp
blog-mysql          mysql:8.0                   Up 2 minutes        127.0.0.1:3306->3306/tcp
```

注意：所有端口都绑定在 127.0.0.1，不直接暴露到公网，这是正常的。

### 第七步：配置系统 Nginx

系统 Nginx 作为公网入口，需要创建站点配置文件。

#### Nginx 目录规范

- **sites-available**: 存放所有站点配置文件（可编辑）
- **sites-enabled**: 存放启用站点的软链接（不要直接拷贝文件）
- 避免创建拼错的目录如 `sites-enable`（少了 d）

#### 创建 Nginx 配置文件

```bash
# 创建站点配置文件
sudo nano /etc/nginx/sites-available/blog
```

添加以下内容（将 `myblogsystem.icu` 替换为你的域名）：

```nginx
# HTTP 服务器 - 重定向到 HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name myblogsystem.icu www.myblogsystem.icu;

    # 重定向所有 HTTP 请求到 HTTPS
    return 301 https://$server_name$request_uri;
}

# HTTPS 服务器 - 主域名
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name myblogsystem.icu;

    # SSL 证书配置（证书获取后会自动配置）
    ssl_certificate /etc/letsencrypt/live/myblogsystem.icu/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/myblogsystem.icu/privkey.pem;

    # SSL 优化配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # 前端代理到容器
    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端 API 代理到容器
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 上传文件代理到后端（避免头像 404）
    location ^~ /uploads/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 可选：添加缓存头
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}

# HTTPS 服务器 - www 子域名重定向到主域名
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name www.myblogsystem.icu;

    ssl_certificate /etc/letsencrypt/live/myblogsystem.icu/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/myblogsystem.icu/privkey.pem;

    # 301 重定向到主域名
    return 301 https://myblogsystem.icu$request_uri;
}
```

#### 启用站点配置

```bash
# 创建软链接到 sites-enabled（不要直接拷贝文件）
sudo ln -s /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/

# 删除默认站点配置（如果存在）
sudo rm -f /etc/nginx/sites-enabled/default

# 测试 Nginx 配置语法
sudo nginx -t

# 如果提示证书文件不存在，暂时不用担心，获取证书后会自动生效
```

#### 重要提示

- **禁止用 IP 访问 HTTPS**：HTTPS 证书颁发给域名，用 IP 访问会出现 `ERR_CERT_COMMON_NAME_INVALID` 错误
- **域名一致性**：确保 DNS A 记录、Nginx `server_name`、证书 CN/SAN 完全一致
- **www 子域名**：如需支持 www，必须在证书申请时包含，或使用 301 重定向统一
- **使用软链接**：sites-enabled 中只放软链接，不要直接拷贝配置文件

### 第八步：配置防火墙

为了让外网能访问你的博客，需要开放 80 和 443 端口：

```bash
# 如果使用 UFW 防火墙（Ubuntu 默认）
sudo ufw allow 80/tcp    # HTTP 端口
sudo ufw allow 443/tcp   # HTTPS 端口

# 启用防火墙
sudo ufw enable

# 查看防火墙状态
sudo ufw status
```

**注意**：
- 不要开放 8080、8081、3306 端口，这些端口仅供本机访问
- 云服务器还需在控制台的安全组中开放 80 and 443 端口

## 🔒 配置 HTTPS 证书

使用 HTTPS 可以加密传输，保护用户数据安全。本项目采用 **系统 certbot + nginx 插件 + systemd timer + deploy hook** 的方案。

### 安装 Certbot 和 Nginx 插件

```bash
# 安装 certbot 和 nginx 插件
sudo apt install certbot python3-certbot-nginx -y

# 验证安装
certbot --version
```

### 获取 SSL 证书

使用 certbot 的 nginx 插件获取证书，它会自动验证域名所有权：

```bash
# 获取证书（替换为你的域名）
sudo certbot --nginx -d myblogsystem.icu -d www.myblogsystem.icu

# 按照提示操作：
# 1. 输入邮箱地址（用于证书到期提醒）
# 2. 同意服务条款（输入 Y）
# 3. 选择是否分享邮箱给 EFF（可选）
# 4. 如果已有证书，选择 "2: Renew & replace" 替换现有证书
```

**重要说明**：
- 必须确保域名已经解析到服务器 IP，否则验证会失败
- 如果需要支持 www 子域，必须在命令中同时包含（使用多个 `-d` 参数）
- certbot 会自动修改 Nginx 配置文件，添加证书路径

### 验证证书安装

```bash
# 测试 Nginx 配置
sudo nginx -t

# 重新加载 Nginx
sudo systemctl reload nginx

# 测试 HTTPS 访问
curl -I https://myblogsystem.icu

# 查看证书信息
sudo certbot certificates
```

访问 `https://你的域名` 确认证书已生效。

### 配置自动续期

Let's Encrypt 证书有效期为 90 天，需要配置自动续期。

#### 启用 systemd timer

Ubuntu 18.04+ 默认使用 systemd timer 管理 certbot 自动续期：

```bash
# 查看 certbot timer 状态
sudo systemctl status certbot.timer

# 如果未启用，启用它
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# 查看下次续期时间
sudo systemctl list-timers | grep certbot
```

#### 配置 deploy hook 自动重载 Nginx

创建 deploy hook 脚本，在证书续期后自动重载 Nginx：

```bash
# 创建 deploy hook 脚本
sudo nano /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
```

添加以下内容：

```bash
#!/bin/bash
# 证书续期后自动重载 Nginx

echo "证书已更新，重载 Nginx 配置..."
systemctl reload nginx
echo "Nginx 重载完成: $(date)"
```

添加执行权限：

```bash
sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
```

#### 验证续期配置

```bash
# 测试续期流程（dry-run 模式，不会实际更新证书）
sudo certbot renew --dry-run

# 如果测试成功，你会看到类似输出：
# Congratulations, all simulated renewals succeeded
```

**如果 dry-run 失败**，检查以下几点：
1. 确认 80 端口未被占用（系统 Nginx 应该正常运行）
2. 查看 `/etc/letsencrypt/renewal/*.conf` 文件，确认 `authenticator = nginx`
3. 确认域名解析正常

#### 检查续期配置文件

```bash
# 查看续期配置文件
sudo cat /etc/letsencrypt/renewal/myblogsystem.icu.conf

# 确认 authenticator 为 nginx
# 应该看到类似：
# authenticator = nginx
```

#### 查看自动续期日志

```bash
# 查看 certbot 服务日志
sudo journalctl -u certbot.service

# 查看 certbot timer 日志
sudo journalctl -u certbot.timer

# 实时查看日志
sudo journalctl -u certbot.service -f
```

#### 移除旧的 crontab 续期任务（如果有）

```bash
# 查看当前 crontab
sudo crontab -l

# 如果有 certbot renew 相关的任务，删除它们
sudo crontab -e
# 删除所有 certbot renew 相关行（保留数据库备份等其他任务）
```

**重要提示**：
- ❌ 不要使用 webroot 或 standalone 模式，它们与容器架构不兼容
- ❌ 不要将 `/etc/letsencrypt` 挂载到容器内
- ❌ 不要在续期时停止容器
- ✅ systemd timer 会在每天随机时间自动检查并续期证书
- ✅ 证书会在到期前 30 天内自动续期
- ✅ 续期成功后会自动执行 deploy hook 重载 Nginx

## 🔒 部署后的安全设置

### 1. 修改数据库表管理模式

首次部署成功并验证数据库表已创建后，**强烈建议**将 `DDL_AUTO` 改为 `validate`，防止误操作导致数据丢失：

```bash
# 编辑 .env 文件
nano .env

# 将 DDL_AUTO=update 改为
DDL_AUTO=validate

# 重新构建并启动后端服务使配置生效
# 注意：必须使用 --build 参数重新构建容器，仅 restart 不会更新配置
docker-compose up -d --build backend
```

### 2. 定期备份数据库

设置定期备份可以防止数据丢失：

```bash
# 创建备份脚本
sudo nano /usr/local/bin/backup-blog-db.sh
```

添加以下内容：

```bash
#!/bin/bash
BACKUP_DIR="/backup/blog"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 导出数据库（从容器中）
docker exec blog-mysql mysqldump -u root -p你的MySQL根密码 blog > $BACKUP_DIR/blog_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/blog_$DATE.sql

# 保留最近 30 天的备份
find $BACKUP_DIR -name "blog_*.sql.gz" -mtime +30 -delete

echo "数据库备份完成: blog_$DATE.sql.gz"
```

保存后，添加执行权限和定时任务：

```bash
# 添加执行权限
sudo chmod +x /usr/local/bin/backup-blog-db.sh

# 添加定时任务（每天凌晨 3 点备份）
sudo crontab -e

# 添加以下行
0 3 * * * /usr/local/bin/backup-blog-db.sh
```

## 🛠️ 日常维护

部署完成后，你需要进行一些日常维护操作，如查看日志、更新代码、重启服务等。

**详细的日常维护操作请参考：[日常维护指南](./MAINTENANCE.md)**

该文档包含：
- 查看服务状态和日志
- 更新应用代码
- 重启和停止服务
- 进入容器调试
- 修改配置文件
- 数据库操作（备份、恢复等）
- 性能监控
- 清理 Docker 资源

### 快速参考

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 更新代码并重新部署
git pull
cd backend/blog && ./mvnw clean package -DskipTests && cd ../..
docker-compose up -d --build

# 重启服务
docker-compose restart backend
```

## 🐛 常见问题排查

### 问题 1：无法访问前端页面

**检查步骤**：

```bash
# 1. 确认容器正在运行
docker-compose ps

# 2. 查看前端日志
docker-compose logs frontend

# 3. 检查防火墙是否开放 80 端口
sudo ufw status

# 4. 测试端口是否可访问
curl http://localhost

# 5. 检查服务器安全组（云服务器）
# 在云服务商控制台检查是否开放了 80 端口
```

### 问题 2：后端无法连接数据库

**检查步骤**：

```bash
# 1. 确认 MySQL 容器健康状态
docker-compose ps

# 2. 查看 MySQL 日志
docker-compose logs mysql

# 3. 查看后端日志中的错误信息
docker-compose logs backend | grep -i error

# 4. 手动测试数据库连接
docker exec -it blog-mysql mysql -u bloguser -p
# 输入 .env 中配置的 DATABASE_PASSWORD
```

### 问题 3：前端无法调用后端 API

**检查步骤**：

```bash
# 1. 确认后端正在运行
docker-compose ps

# 2. 测试后端 API 是否可访问
curl http://localhost:8080/api/health

# 3. 检查防火墙
sudo ufw status

# 4. 查看 Nginx 配置
docker exec -it blog-frontend cat /etc/nginx/conf.d/default.conf

# 5. 查看前端日志
docker-compose logs frontend
```

### 问题 4：容器频繁重启

**检查步骤**：

```bash
# 1. 查看容器日志找出错误原因
docker-compose logs -f backend

# 2. 检查环境变量配置
cat .env

# 3. 确认 JWT_SECRET 等配置项已正确设置
```

### 问题 5：数据库表未创建

**解决方法**：

```bash
# 1. 确认 .env 文件中 DDL_AUTO=update
cat .env | grep DDL_AUTO

# 2. 如果不是 update，修改为 update
nano .env
# 改为：DDL_AUTO=update

# 3. 重新构建并启动后端服务（必须使用 --build 参数）
docker-compose up -d --build backend

# 4. 查看日志确认表已创建
docker-compose logs backend | grep -i "table"

# 5. 进入数据库检查
docker exec -it blog-mysql mysql -u bloguser -p
# 在 MySQL 中执行：
USE blog;
SHOW TABLES;
```

### 问题 6：头像显示 404 (GET /uploads/...)

**症状**：
- 上传头像成功，数据库中有记录
- 但浏览器访问 `/uploads/...` 返回 404

**根本原因**：
- 后端通过 WebMvc 映射 `/uploads/**` 为静态资源
- 系统 Nginx 未将 `/uploads/**` 反代到后端，导致 Nginx 找不到文件

**解决方法**：

1. 检查系统 Nginx 配置中是否有 `/uploads/` 反代配置：

```bash
sudo cat /etc/nginx/sites-available/blog | grep -A 10 "uploads"
```

应该看到类似：

```nginx
location ^~ /uploads/ {
    proxy_pass http://127.0.0.1:8080;
    # ...
}
```

2. 如果没有，编辑配置文件添加：

```bash
sudo nano /etc/nginx/sites-available/blog
```

在 HTTPS server 块中添加：

```nginx
    # 上传文件代理到后端（避免头像 404）
    location ^~ /uploads/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 可选：添加缓存头
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
```

3. 测试并重载 Nginx：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

4. 验证上传目录挂载：

```bash
# 检查宿主机上传目录
ls -la data/uploads

# 检查容器内上传目录
docker exec blog-backend ls -la /app/uploads
```

### 问题 7：CORS 错误 (403 Invalid CORS request)

**症状**：
- 线上域名发起的头像上传 POST/PUT 报 403 Invalid CORS request
- 浏览器控制台显示 CORS 策略阻止

**根本原因**：
- WebConfig 仅放行 `http://localhost:3000`（本地开发）
- 生产环境域名未加入 `allowedOrigins`

**解决方法**：

**最佳方案：生产环境使用同源（推荐）**

前端生产环境应该使用相对路径 `/api`，这样请求与页面同源，不会触发 CORS：

```javascript
// 前端代码中
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? '/api'  // 生产环境：同源，不触发 CORS
  : 'http://localhost:8080/api';  // 开发环境：跨域，需要 CORS
```

**临时方案：后端放行生产域名**

如果必须跨域访问，检查后端配置：

1. 检查 SecurityConfig.java 是否启用了 CORS：

```bash
cat backend/blog/src/main/java/com/lost/blog/config/SecurityConfig.java | grep -A 5 "cors"
```

应该看到：

```java
.cors(Customizer.withDefaults())
```

2. 检查 WebConfig.java 是否包含生产域名：

```bash
cat backend/blog/src/main/java/com/lost/blog/config/WebConfig.java | grep -A 10 "allowedOrigins"
```

应该看到：

```java
.allowedOrigins(
    "https://myblogsystem.icu",
    "https://www.myblogsystem.icu",
    "http://localhost:3000"
)
```

3. 如果配置正确但仍有问题，重新构建后端：

```bash
cd backend/blog
./mvnw clean package -DskipTests
cd ../..
docker-compose up -d --build backend
```

**本地开发 CORS 配置**：

在本地开发时，前端通过代理或直接跨域访问后端。确保：

1. WebConfig 中已包含 `http://localhost:3000`
2. SecurityConfig 中启用了 CORS
3. 前端配置了正确的 API 地址

### 问题 8：内存不足

如果服务器内存较小（2GB），可能会出现内存不足的问题。

**解决方法**：

1. 限制 Java 堆内存大小

编辑 `backend/blog/Dockerfile`：

```dockerfile
# 启动应用
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
```

2. 重新构建并启动：

```bash
docker-compose up -d --build backend
```

### 问题 9：端口被占用

**解决方法**：

```bash
# 查看端口占用情况
sudo netstat -tlnp | grep -E '80|443|8080|8081|3306'

# 如果 80 或 443 被占用，检查是什么进程
sudo lsof -i :80
sudo lsof -i :443

# 如果是其他服务占用，停止它或修改 Nginx 配置使用其他端口
```

**注意**：
- 80 和 443 端口应该由系统 Nginx 占用，这是正常的
- 8080、8081、3306 应该只在 127.0.0.1 上监听，不在 0.0.0.0 上监听

## 📊 性能监控与优化

关于性能监控、资源清理和优化建议，请参考 [日常维护指南](./MAINTENANCE.md) 中的相关章节。

## 💡 生产环境优化建议

### 1. 使用域名访问

- ✅ 必须使用域名（HTTPS 证书需要域名）
- ✅ 配置 DNS A 记录指向服务器 IP
- ✅ 域名、DNS、Nginx server_name、证书 CN/SAN 保持一致
- ❌ 禁止用 IP 访问 HTTPS（会出现 ERR_CERT_COMMON_NAME_INVALID 错误）

### 2. 配置 CDN

如果你的博客有大量静态资源，可以使用 CDN 加速访问。

### 3. 定期更新系统和 Docker

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 更新 Docker
sudo apt install --only-upgrade docker-ce docker-ce-cli containerd.io

# 更新镜像
docker-compose pull
docker-compose up -d
```

## 🎉 总结

按照本指南，你已经成功使用 Docker 将博客系统部署到服务器上了！

**完成的工作**：
- ✅ 配置了统一的部署架构（系统 Nginx 作为唯一公网入口）
- ✅ 安装了 Docker 和 Docker Compose
- ✅ 配置了环境变量和密钥
- ✅ 构建并启动了所有容器服务
- ✅ 配置了系统 Nginx（前端、后端 API、上传文件反代）
- ✅ 配置了防火墙允许外部访问
- ✅ 获取并配置了 HTTPS 证书（certbot + nginx 插件）
- ✅ 配置了证书自动续期（systemd timer + deploy hook）
- ✅ 设置了数据库定期备份

**部署架构要点**：
- 系统 Nginx（80/443）→ 前端容器（127.0.0.1:8081）
- 系统 Nginx（/api）→ 后端容器（127.0.0.1:8080）
- 系统 Nginx（/uploads）→ 后端容器（127.0.0.1:8080）
- MySQL 容器（127.0.0.1:3306）仅本机访问
- 上传文件持久化到宿主机 ./data/uploads

**后续维护**：
- 查看 [日常维护指南](./MAINTENANCE.md) 了解如何进行日常运维操作
- 定期查看日志，关注系统运行状态
- 定期备份数据库
- 定期更新系统和应用
- 关注服务器资源使用情况
- 监控证书续期日志

**重要提醒**：
- 修改环境变量或配置文件后，需要使用 `docker-compose up -d --build` 重新构建容器
- SSL 证书由 systemd timer 自动续期，不需要手动干预
- 生产环境应将数据库表管理模式设置为 `validate`
- 所有容器端口仅绑定 127.0.0.1，不直接暴露到公网
- 必须使用域名访问 HTTPS，禁止用 IP 访问

如果遇到问题，请参考"常见问题排查"章节，或查看容器日志获取详细错误信息。

祝你的博客运行顺利！🚀