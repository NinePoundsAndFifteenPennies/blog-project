# 生产环境部署指南

本文档介绍如何将博客系统部署到生产服务器。

## 📋 部署前准备

### 1. 服务器要求

- **操作系统**: Linux (推荐 Ubuntu 20.04+ 或 CentOS 7+)
- **Java**: JDK 17 或更高版本
- **MySQL**: 5.7 或 8.0+
- **Node.js**: 14.x 或更高版本 (仅构建前端时需要)
- **内存**: 至少 2GB RAM
- **磁盘空间**: 至少 10GB

### 2. 必需软件安装

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java (Ubuntu/Debian)
sudo apt install openjdk-17-jdk -y

# 或者 (CentOS/RHEL)
sudo yum install java-17-openjdk -y

# 安装 MySQL
sudo apt install mysql-server -y

# 启动并启用 MySQL
sudo systemctl start mysql
sudo systemctl enable mysql
```

## 🗄️ 数据库配置

### 1. 创建数据库和用户

```bash
# 登录 MySQL
sudo mysql -u root -p

# 在 MySQL 中执行以下命令
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bloguser'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON blog.* TO 'bloguser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. 初始化数据库表

项目使用 JPA 自动创建表。首次启动时，需要临时使用 `update` 模式创建表结构：

```bash
# 首次启动时使用 update 模式
java -jar blog.jar --spring.jpa.hibernate.ddl-auto=update

# 等待应用启动完成并创建所有表后，停止应用
# 后续启动使用 prod 配置文件
```

**重要**: 生产环境长期运行应使用 `validate` 模式，避免意外修改数据库结构。

## 🔧 后端部署

### 1. 构建后端应用

在本地或服务器上构建应用：

```bash
cd backend/blog

# 使用 Maven 构建
./mvnw clean package -DskipTests

# 构建产物位于 target/blog-0.0.1-SNAPSHOT.jar
```

### 2. 配置环境变量

创建环境变量配置文件 `/etc/blog/blog.env`：

```bash
# 创建配置目录
sudo mkdir -p /etc/blog

# 创建环境变量文件
sudo nano /etc/blog/blog.env
```

添加以下内容（根据实际情况修改）：

```bash
# 数据库配置
DATABASE_URL=jdbc:mysql://localhost:3306/blog?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
DATABASE_USERNAME=bloguser
DATABASE_PASSWORD=your_secure_password

# JWT配置
# 生成新密钥: openssl rand -base64 64
JWT_SECRET=your_generated_jwt_secret_key_at_least_64_characters_long
JWT_EXPIRATION_MS=3600000
JWT_REMEMBER_ME_EXPIRATION_MS=2592000000

# 服务器配置
SERVER_PORT=8080
```

### 3. 生成安全的 JWT 密钥

```bash
# 生成一个安全的随机密钥
openssl rand -base64 64

# 将生成的密钥复制到 blog.env 中的 JWT_SECRET
```

### 4. 创建 Systemd 服务

创建服务文件 `/etc/systemd/system/blog.service`：

```bash
sudo nano /etc/systemd/system/blog.service
```

添加以下内容：

```ini
[Unit]
Description=Blog Application
After=mysql.service
Wants=mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/blog
EnvironmentFile=/etc/blog/blog.env
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/blog/blog.jar
Restart=always
RestartSec=10

# 日志配置
StandardOutput=journal
StandardError=journal
SyslogIdentifier=blog

[Install]
WantedBy=multi-user.target
```

### 5. 部署和启动服务

```bash
# 创建应用目录
sudo mkdir -p /opt/blog

# 复制 JAR 文件到服务器
sudo cp target/blog-0.0.1-SNAPSHOT.jar /opt/blog/blog.jar

# 设置权限
sudo chown -R www-data:www-data /opt/blog
sudo chmod 600 /etc/blog/blog.env

# 重新加载 systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start blog

# 设置开机自启
sudo systemctl enable blog

# 检查服务状态
sudo systemctl status blog

# 查看日志
sudo journalctl -u blog -f
```

## 🎨 前端部署

### 1. 构建前端应用

```bash
cd frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 构建产物位于 dist/ 目录
```

### 2. 部署选项

#### 选项 A: 使用 Nginx 作为反向代理（推荐）

安装 Nginx：

```bash
sudo apt install nginx -y
```

创建 Nginx 配置文件 `/etc/nginx/sites-available/blog`：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为您的域名

    # 前端静态文件
    root /var/www/blog;
    index index.html;

    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
}
```

部署前端文件：

```bash
# 创建网站目录
sudo mkdir -p /var/www/blog

# 复制构建文件
sudo cp -r dist/* /var/www/blog/

# 设置权限
sudo chown -R www-data:www-data /var/www/blog

# 启用站点
sudo ln -s /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

#### 选项 B: 与后端打包在一起

如果不想单独部署前端，可以将前端构建文件放入后端的 `static` 目录：

```bash
# 在后端项目中创建 static 目录
mkdir -p backend/blog/src/main/resources/static

# 复制前端构建文件
cp -r frontend/dist/* backend/blog/src/main/resources/static/

# 重新构建后端
cd backend/blog
./mvnw clean package -DskipTests
```

这样访问 `http://your-domain.com:8080/` 就可以直接访问前端页面。

## 🔒 配置 HTTPS（可选但推荐）

使用 Let's Encrypt 免费 SSL 证书：

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取证书并自动配置 Nginx
sudo certbot --nginx -d your-domain.com

# 证书会自动续期
```

## 📊 日志管理

### 查看应用日志

```bash
# 实时查看日志
sudo journalctl -u blog -f

# 查看最近的日志
sudo journalctl -u blog -n 100

# 查看特定时间的日志
sudo journalctl -u blog --since "2024-01-01" --until "2024-01-02"
```

### 日志轮转配置

创建日志轮转配置 `/etc/logrotate.d/blog`：

```
/var/log/blog/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0644 www-data www-data
}
```

## 🔄 更新部署

### 更新后端

```bash
# 停止服务
sudo systemctl stop blog

# 备份旧版本
sudo cp /opt/blog/blog.jar /opt/blog/blog.jar.backup

# 上传新的 JAR 文件
sudo cp new-blog.jar /opt/blog/blog.jar

# 启动服务
sudo systemctl start blog

# 检查状态
sudo systemctl status blog
```

### 更新前端

```bash
# 备份旧版本
sudo mv /var/www/blog /var/www/blog.backup

# 部署新版本
sudo mkdir -p /var/www/blog
sudo cp -r dist/* /var/www/blog/
sudo chown -R www-data:www-data /var/www/blog

# 重启 Nginx
sudo systemctl restart nginx
```

## 🔍 故障排查

### 应用无法启动

1. 检查日志：`sudo journalctl -u blog -n 50`
2. 检查数据库连接：确保 MySQL 运行正常
3. 检查端口占用：`sudo netstat -tlnp | grep 8080`
4. 检查环境变量：`sudo systemctl show blog | grep Environment`

### 前端无法访问后端 API

1. 检查 Nginx 配置：`sudo nginx -t`
2. 检查后端是否运行：`curl http://localhost:8080/api/health`
3. 检查防火墙：`sudo ufw status`

### 数据库连接失败

1. 检查 MySQL 状态：`sudo systemctl status mysql`
2. 测试数据库连接：`mysql -u bloguser -p -h localhost blog`
3. 检查数据库权限

## 🛡️ 安全建议

1. **定期更新**：保持系统和依赖项更新
2. **防火墙配置**：
   ```bash
   # 开放必要的端口
   sudo ufw allow 80/tcp    # HTTP
   sudo ufw allow 443/tcp   # HTTPS
   sudo ufw allow 8080/tcp  # 后端 API（如果需要直接访问）
   sudo ufw enable
   
   # 限制 MySQL 端口只能从本地访问
   sudo ufw deny 3306/tcp
   ```
3. **网络绑定**：应用已配置为绑定到 `0.0.0.0`，可从不同网络环境访问
4. **限制访问**：使用防火墙规则限制数据库访问，MySQL 只应从本地或容器网络访问
5. **备份**：定期备份数据库和应用数据
6. **监控**：设置应用和服务器监控
7. **密钥管理**：定期轮换 JWT 密钥

## 📦 备份策略

### 数据库备份

创建备份脚本 `/opt/scripts/backup-blog-db.sh`：

```bash
#!/bin/bash
BACKUP_DIR="/backup/blog"
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_USER="bloguser"
MYSQL_PASSWORD="your_secure_password"
DATABASE="blog"

mkdir -p $BACKUP_DIR
mysqldump -u $MYSQL_USER -p$MYSQL_PASSWORD $DATABASE > $BACKUP_DIR/blog_$DATE.sql
gzip $BACKUP_DIR/blog_$DATE.sql

# 保留最近 7 天的备份
find $BACKUP_DIR -name "blog_*.sql.gz" -mtime +7 -delete
```

添加定时任务：

```bash
# 编辑 crontab
sudo crontab -e

# 每天凌晨 2 点备份
0 2 * * * /opt/scripts/backup-blog-db.sh
```

## 🎯 性能优化

1. **数据库索引**：确保常用查询字段有索引
2. **连接池**：调整数据库连接池大小
3. **JVM 参数**：根据服务器内存调整 JVM 参数
   ```bash
   ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar ...
   ```
4. **静态资源缓存**：在 Nginx 中配置静态资源缓存
5. **Gzip 压缩**：启用 Gzip 压缩减少传输大小

## 📞 需要帮助？

如果在部署过程中遇到问题，请检查：
- 应用日志：`sudo journalctl -u blog -f`
- Nginx 日志：`sudo tail -f /var/log/nginx/error.log`
- MySQL 日志：`sudo tail -f /var/log/mysql/error.log`
