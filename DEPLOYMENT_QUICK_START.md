# 生产环境部署快速指南

这是一个简化的部署指南，帮助您快速将博客系统部署到服务器。

## 📋 前置准备

1. **服务器要求**：
   - Linux 系统（推荐 Ubuntu 20.04+）
   - 至少 2GB 内存
   - 至少 10GB 磁盘空间

2. **需要安装的软件**：
   - Java 17+
   - MySQL 5.7+ 或 8.0+
   - Nginx（可选，用于前端部署）

## 🚀 方式一：Docker 部署（最简单，推荐）

### 1. 安装 Docker 和 Docker Compose

```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 安装 Docker Compose
sudo apt install docker-compose -y
```

### 2. 配置环境变量

```bash
# 复制环境变量示例
cp .env.docker.example .env

# 生成 JWT 密钥
openssl rand -base64 64

# 编辑 .env 文件，填入配置
nano .env
```

在 `.env` 文件中设置：
- `MYSQL_ROOT_PASSWORD`: MySQL root 密码
- `DATABASE_USERNAME`: 数据库用户名（如 bloguser）
- `DATABASE_PASSWORD`: 数据库密码
- `JWT_SECRET`: 刚刚生成的 JWT 密钥

### 3. 构建后端

```bash
cd backend/blog
./mvnw clean package -DskipTests
cd ../..
```

### 4. 启动所有服务

```bash
docker-compose up -d
```

### 5. 访问应用

- 前端：http://your-server-ip
- 后端 API：http://your-server-ip:8080/api

**重要**：应用已配置为绑定到所有网络接口（0.0.0.0），可以从不同网络环境访问。

### 6. 配置防火墙允许外部访问

如果需要从互联网访问，请确保防火墙开放以下端口：

```bash
# 开放 HTTP 端口
sudo ufw allow 80/tcp

# 开放后端 API 端口（如果需要直接访问）
sudo ufw allow 8080/tcp

# 如果配置了 HTTPS
sudo ufw allow 443/tcp

# 启用防火墙
sudo ufw enable
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 停止服务

```bash
docker-compose down
```

## 🔧 方式二：手动部署

### 第一步：准备数据库

```bash
# 安装 MySQL
sudo apt install mysql-server -y

# 启动 MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 登录 MySQL
sudo mysql -u root -p

# 创建数据库和用户
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bloguser'@'localhost' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON blog.* TO 'bloguser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 第二步：部署后端

```bash
cd backend/blog

# 复制环境变量示例
cp .env.example .env

# 生成 JWT 密钥
openssl rand -base64 64

# 编辑 .env 文件
nano .env
```

在 `.env` 文件中填入：
```bash
DATABASE_URL=jdbc:mysql://localhost:3306/blog?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
DATABASE_USERNAME=bloguser
DATABASE_PASSWORD=你的数据库密码
JWT_SECRET=刚刚生成的密钥
JWT_EXPIRATION_MS=3600000
JWT_REMEMBER_ME_EXPIRATION_MS=2592000000
SERVER_PORT=8080
```

构建和启动：
```bash
# 构建应用
./mvnw clean package -DskipTests

# 首次启动，初始化数据库表
java -jar target/blog-0.0.1-SNAPSHOT.jar --spring.jpa.hibernate.ddl-auto=update

# 等待应用完全启动并创建好所有表后，停止应用（Ctrl+C）

# 后续使用生产配置启动
./start-prod.sh
```

### 第三步：部署前端

```bash
cd frontend

# 安装依赖
npm install

# 构建生产版本
npm run build
```

#### 选项 A：使用 Nginx 部署

```bash
# 安装 Nginx
sudo apt install nginx -y

# 复制构建文件
sudo mkdir -p /var/www/blog
sudo cp -r dist/* /var/www/blog/

# 创建 Nginx 配置
sudo nano /etc/nginx/sites-available/blog
```

添加以下内容：
```nginx
server {
    listen 80;
    server_name your-domain.com;  # 改成你的域名或服务器IP

    root /var/www/blog;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

启用配置：
```bash
sudo ln -s /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### 选项 B：与后端一起部署

如果不想安装 Nginx，可以让 Spring Boot 直接提供前端文件：

```bash
# 复制前端构建文件到后端
mkdir -p backend/blog/src/main/resources/static
cp -r frontend/dist/* backend/blog/src/main/resources/static/

# 重新构建后端
cd backend/blog
./mvnw clean package -DskipTests

# 重启后端服务
```

访问 `http://your-server-ip:8080` 即可。

## 🔒 配置 HTTPS（推荐）

如果有域名，建议配置 HTTPS：

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取免费 SSL 证书
sudo certbot --nginx -d your-domain.com
```

## 🛠️ 常用命令

### 查看后端日志
```bash
# 如果使用 systemd 服务
sudo journalctl -u blog -f

# 如果直接运行
# 日志会输出到终端
```

### 重启服务
```bash
# Docker 方式
docker-compose restart

# 手动部署
sudo systemctl restart blog  # 如果配置了 systemd 服务
# 或者直接 kill 进程后重新运行
```

### 备份数据库
```bash
mysqldump -u bloguser -p blog > blog_backup_$(date +%Y%m%d).sql
```

## ⚠️ 重要提示

1. **保护敏感信息**：
   - 不要将 `.env` 文件提交到 Git
   - 使用强密码
   - JWT 密钥必须是随机生成的

2. **防火墙配置**：
```bash
# 开放必要端口
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

3. **数据库配置**：
   - 首次启动使用 `update` 模式创建表
   - 后续运行使用 `validate` 模式（生产配置）

4. **定期备份**：
   - 定期备份数据库
   - 备份上传的文件和配置

## 🆘 遇到问题？

### 后端无法启动
- 检查数据库是否运行：`sudo systemctl status mysql`
- 检查环境变量是否正确配置
- 查看日志了解具体错误

### 前端无法访问后端
- 检查后端是否正常运行：`curl http://localhost:8080/api/health`
- 检查 Nginx 配置是否正确
- 检查防火墙是否开放端口

### 数据库连接失败
- 确认数据库用户名和密码正确
- 确认数据库已创建
- 尝试手动连接：`mysql -u bloguser -p -h localhost blog`

## 📚 更多信息

- 详细部署文档：[DEPLOYMENT.md](DEPLOYMENT.md)
- 项目说明：[README.md](README.md)
- "记住我"功能说明：[REMEMBER_ME_IMPLEMENTATION.md](REMEMBER_ME_IMPLEMENTATION.md)
