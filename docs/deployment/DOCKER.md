# Docker 部署指南

本指南专门针对使用 Docker 将博客系统部署到租用服务器的场景。按照本指南操作，你可以让博客系统在服务器上持续运行，并对外提供服务。

> **📢 重要说明**：
> - ✅ **默认使用8000端口**：避免与服务器上已有的nginx服务冲突
> - ✅ **简化的配置**：移除了容器内的SSL配置，推荐使用服务器nginx做反向代理
> - 📖 **日常运维操作**：查看日志、更新代码等日常操作请参考 [日常维护指南](./MAINTENANCE.md)

## 📋 前置准备

### 1. 服务器要求

- **操作系统**: Linux 系统（推荐 Ubuntu 20.04+ 或 CentOS 7+）
- **内存**: 至少 2GB RAM
- **磁盘空间**: 至少 10GB 可用空间
- **网络**: 确保服务器有公网 IP 或域名

### 2. 安装 Docker 和 Docker Compose

在你的服务器上执行以下命令安装 Docker：

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

### 第四步：启动所有服务

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
3. 启动后端应用容器
4. 启动前端应用容器
5. 自动创建所需的网络和数据卷

**首次启动说明**：
- 首次启动时，MySQL 需要初始化，后端应用会等待 MySQL 就绪后才启动
- 后端应用会自动创建数据库表（因为 `DDL_AUTO=update`）
- 整个过程可能需要 1-3 分钟，请耐心等待

### 第五步：查看服务状态

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
blog-backend        blog-project-backend        Up 2 minutes        0.0.0.0:8080->8080/tcp
blog-frontend       blog-project-frontend       Up 2 minutes        0.0.0.0:8000->80/tcp
blog-mysql          mysql:8.0                   Up 2 minutes        0.0.0.0:3306->3306/tcp
```

### 第六步：配置防火墙（重要）

为了让外网能访问你的博客，需要开放相应的端口：

```bash
# 如果使用 UFW 防火墙（Ubuntu 默认）
sudo ufw allow 8000/tcp  # 前端端口
sudo ufw allow 8080/tcp  # 后端 API 端口（可选，如果需要直接访问）

# 启用防火墙
sudo ufw enable

# 查看防火墙状态
sudo ufw status
```

**注意**：不要开放 3306 端口（MySQL），数据库只应在容器内部访问。

### 第七步：访问你的博客

部署完成！现在可以通过以下地址访问你的博客：

- **前端页面**: `http://你的服务器IP地址:8000`
- **后端 API**: `http://你的服务器IP地址:8080/api`

例如，如果你的服务器 IP 是 `192.168.1.100`：
- 前端: http://192.168.1.100:8000
- 后端: http://192.168.1.100:8080/api

## 🌐 配置域名和HTTPS（推荐）

如果你有域名并且希望使用标准的80/443端口以及HTTPS，建议在服务器上配置nginx反向代理：

### 方法：使用服务器nginx反向代理（推荐）

1. **安装nginx**（如果还没安装）：

```bash
sudo apt install nginx -y
```

2. **创建nginx配置文件**：

```bash
sudo nano /etc/nginx/sites-available/blog
```

3. **添加以下配置**：

```nginx
server {
    listen 80;
    server_name yourdomain.com;  # 改为你的域名

    location / {
        proxy_pass http://localhost:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

4. **启用配置**：

```bash
sudo ln -s /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

5. **配置HTTPS（使用Let's Encrypt）**：

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取并配置SSL证书
sudo certbot --nginx -d yourdomain.com
```

certbot会自动修改nginx配置，添加HTTPS支持和HTTP到HTTPS的重定向。

现在你可以通过 `https://yourdomain.com` 访问你的博客了！

## 🔒 重要：部署后的安全设置

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

### 2. 定期备份数据

```yaml
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: blog-frontend
    restart: always
    depends_on:
      - backend
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /etc/letsencrypt:/etc/letsencrypt:ro
      - /var/www/certbot:/var/www/certbot:ro
    networks:
      - blog-network
```

重新构建前端容器：

```bash
docker-compose up -d --build frontend
```

创建续期脚本：

```bash
sudo nano /usr/local/bin/renew-cert.sh
```

添加以下内容：

```bash
#!/bin/bash
# SSL 证书续期脚本

# 创建 webroot 目录
mkdir -p /var/www/certbot

# 使用 webroot 模式续期证书
certbot renew --webroot -w /var/www/certbot --quiet

# 重新加载前端 Nginx 配置
docker-compose restart frontend

echo "证书续期完成: $(date)"
```

添加执行权限和定时任务：

```bash
# 添加执行权限
sudo chmod +x /usr/local/bin/renew-cert.sh

# 添加定时任务（每天凌晨 2 点检查并续期）
sudo crontab -e

# 添加以下行
0 2 * * * /usr/local/bin/renew-cert.sh >> /var/log/certbot-renew.log 2>&1
```

**推荐方法 2：临时停止容器续期**

创建续期脚本：

```bash
sudo nano /usr/local/bin/renew-cert-standalone.sh
```

添加以下内容：

```bash
#!/bin/bash
# SSL 证书续期脚本（standalone 模式）

cd /path/to/blog-project  # 修改为你的项目路径

# 停止前端容器释放 80 端口
docker-compose stop frontend

# 使用 standalone 模式续期证书
### 2. 定期备份数据

设置定期备份可以防止数据丢失。详细的备份操作请参考 [日常维护指南](./MAINTENANCE.md)。

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

### 问题 1：端口被占用

**错误信息**：
```
ERROR: for blog-frontend  Cannot start service frontend: failed to bind host port for 0.0.0.0:8000
```

**解决方法**：

```bash
# 1. 查看端口占用情况
sudo lsof -i :8000

# 2. 如果端口被占用，可以选择：
# 选项A：停止占用端口的服务
sudo systemctl stop <服务名>

# 选项B：修改docker-compose.yml使用其他端口
# 将 "8000:80" 改为 "8001:80" 或其他可用端口
```

### 问题 2：无法访问前端页面

**检查步骤**：

```bash
# 1. 确认容器正在运行
docker-compose ps

# 2. 查看前端日志
docker-compose logs frontend

# 3. 检查防火墙是否开放端口
sudo ufw status

# 4. 测试端口是否可访问
curl http://localhost:8000

# 5. 检查服务器安全组（云服务器）
# 在云服务商控制台检查是否开放了 8000 端口
```

### 问题 3：后端无法连接数据库

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

### 问题 4：前端无法调用后端 API

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

### 问题 5：容器频繁重启

**检查步骤**：

```bash
# 1. 查看容器日志找出错误原因
docker-compose logs -f backend

# 2. 检查环境变量配置
cat .env

# 3. 确认 JWT_SECRET 等配置项已正确设置
```

### 问题 6：数据库表未创建

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

### 问题 6：内存不足

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

### 问题 7：端口被占用

**解决方法**：

```bash
# 查看端口占用情况
sudo netstat -tlnp | grep -E '80|8080|3306'

# 如果端口被占用，可以修改 docker-compose.yml 中的端口映射
# 例如将前端从 80 改为 8000：
#   ports:
#     - "8000:80"
```

## 📊 性能监控与优化

关于性能监控、资源清理和优化建议，请参考 [日常维护指南](./MAINTENANCE.md) 中的相关章节。

docker-compose up -d --build backend

# 4. 查看日志确认表已创建
docker-compose logs backend | grep -i "table"
```

## 💡 生产环境优化建议

### 1. 使用域名和HTTPS

购买域名并配置 DNS 解析，然后使用服务器nginx配置HTTPS反向代理（参见上文"配置域名和HTTPS"章节），提供更好的用户体验和安全性。

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

### 4. 监控和日志

定期检查容器状态和日志，及时发现和解决问题。详见 [日常维护指南](./MAINTENANCE.md)。

## 🎉 总结

按照本指南，你已经成功使用 Docker 将博客系统部署到服务器上了！

**完成的工作**：
- ✅ 安装了 Docker 和 Docker Compose
- ✅ 配置了环境变量和密钥
- ✅ 构建并启动了所有服务
- ✅ 配置了防火墙允许外部访问
- ✅ 了解了如何配置HTTPS（使用服务器nginx）

**后续维护**：
- 查看 [日常维护指南](./MAINTENANCE.md) 了解如何进行日常运维操作
- 定期查看日志，关注系统运行状态
- 定期备份数据库
- 定期更新系统和应用
- 关注服务器资源使用情况

**重要提醒**：
- 前端默认运行在 8000 端口，避免与服务器nginx冲突
- 如需使用80/443端口，请配置服务器nginx反向代理
- 修改环境变量或配置文件后，需要使用 `docker-compose up -d --build` 重新构建容器
- 生产环境应将数据库表管理模式设置为 `validate`

如果遇到问题，请参考"常见问题排查"章节，或查看容器日志获取详细错误信息。

祝你的博客运行顺利！🚀