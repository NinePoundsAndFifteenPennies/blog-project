# 日常维护指南

本文档介绍博客系统部署后的日常维护操作，包括查看日志、更新代码、监控服务状态等。

**前提条件**：假设你已经按照 [Docker部署指南](./DOCKER.md) 完成了系统部署。

> **💡 提示**：
> - 如果你还没有完成部署，请先阅读 [Docker部署指南](./DOCKER.md)
> - 本文档的所有命令都假设你在项目根目录下执行
> - 建议将常用命令保存到笔记中，方便日后查阅

## 📋 目录

- [查看服务状态](#查看服务状态)
- [查看日志](#查看日志)
- [更新应用代码](#更新应用代码)
- [重启服务](#重启服务)
- [停止服务](#停止服务)
- [进入容器调试](#进入容器调试)
- [修改配置](#修改配置)
- [数据库操作](#数据库操作)
- [性能监控](#性能监控)
- [清理 Docker 资源](#清理-docker-资源)

---

## 🔍 查看服务状态

### 查看所有容器状态

```bash
# 查看所有容器的运行状态
docker-compose ps

# 查看容器资源占用（CPU、内存、网络等）
docker stats

# 查看特定容器的详细信息
docker inspect blog-backend
docker inspect blog-frontend
docker inspect blog-mysql
```

**预期输出示例**：

```
NAME                IMAGE                       STATUS              PORTS
blog-backend        blog-project-backend        Up 2 hours          0.0.0.0:8080->8080/tcp
blog-frontend       blog-project-frontend       Up 2 hours          0.0.0.0:80->80/tcp
blog-mysql          mysql:8.0                   Up 2 hours          0.0.0.0:3306->3306/tcp
```

### 检查容器健康状态

```bash
# 查看容器是否健康
docker-compose ps

# 如果容器状态显示 "Restarting" 或 "Exited"，说明有问题
# 此时需要查看日志找出原因
```

---

## 📝 查看日志

### 查看所有服务日志

```bash
# 查看所有服务的实时日志
docker-compose logs -f

# 查看最近 100 行日志
docker-compose logs --tail=100

# 查看最近 1 小时的日志
docker-compose logs --since 1h
```

### 查看特定服务日志

```bash
# 查看后端日志（实时）
docker-compose logs -f backend

# 查看前端日志（实时）
docker-compose logs -f frontend

# 查看数据库日志（实时）
docker-compose logs -f mysql

# 查看后端最近 50 行日志
docker-compose logs --tail=50 backend

# 查看后端日志并筛选错误信息
docker-compose logs backend | grep -i error
docker-compose logs backend | grep -i exception

# 将日志保存到文件
docker-compose logs backend > backend.log
```

### 常见日志检查场景

**场景 1：检查后端启动是否成功**

```bash
docker-compose logs backend | grep -i "started"
# 应该看到类似：Started BlogApplication in X.XXX seconds
```

**场景 2：检查数据库连接**

```bash
docker-compose logs backend | grep -i "database"
docker-compose logs backend | grep -i "mysql"
```

**场景 3：检查 API 请求日志**

```bash
docker-compose logs -f backend | grep "api"
```

---

## 🔄 更新应用代码

当你在本地修改了代码并推送到 Git 仓库后，需要在服务器上更新应用。

### 标准更新流程

```bash
# 1. 进入项目目录
cd /path/to/blog-project

# 2. 拉取最新代码
git pull origin main

# 3. 重新构建后端（如果修改了后端代码）
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 4. 重新构建并启动所有服务（使用 --no-cache 确保使用新的 JAR 文件）
docker-compose up -d --build --no-cache backend
docker-compose up -d --build frontend

# 5. 查看日志确认更新成功
docker-compose logs -f backend
```

**重要提示**：
- 使用 `--no-cache` 标志可以确保 Docker 不使用缓存的旧层，这对于后端代码更新非常重要，因为 Docker 可能无法检测到 target 目录中 JAR 文件的更改
- 对于后端更新，**必须使用** `--no-cache` 标志，否则可能会使用旧的 JAR 文件
- 对于前端更新，通常 `--build` 就足够了，因为前端使用多阶段构建，会在容器内重新构建
- 如果只修改了环境变量（.env 文件），不需要重新构建，只需要重启即可

### 只更新前端

```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动前端
docker-compose up -d --build frontend

# 查看日志
docker-compose logs -f frontend
```

### 只更新后端

```bash
# 拉取最新代码
git pull origin main

# 重新构建后端 JAR 文件
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 重新构建并启动后端容器（使用 --no-cache 确保使用新的 JAR 文件）
docker-compose up -d --build --no-cache backend

# 查看日志
docker-compose logs -f backend
```

### 验证更新是否成功

```bash
# 1. 检查容器状态
docker-compose ps

# 2. 测试前端访问
curl http://localhost

# 3. 测试后端 API
curl http://localhost:8080/api/health

# 4. 查看最新日志
docker-compose logs --tail=50 backend
docker-compose logs --tail=50 frontend
```

---

## 🔄 重启服务

### 重启所有服务

```bash
# 重启所有服务（保持数据不变）
docker-compose restart

# 查看日志确认重启成功
docker-compose logs --tail=50
```

### 重启特定服务

```bash
# 重启后端服务
docker-compose restart backend

# 重启前端服务
docker-compose restart frontend

# 重启数据库服务（谨慎操作）
docker-compose restart mysql
```

**注意**：
- `restart` 命令只是重启容器，不会重新构建镜像
- 如果修改了代码或配置文件，应该使用 `docker-compose up -d --build` 而不是 `restart`

---

## 🛑 停止服务

### 停止所有服务

```bash
# 停止所有服务（保留容器和数据）
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除容器和数据卷（⚠️ 会删除数据库数据！慎用！）
docker-compose down -v
```

### 停止特定服务

```bash
# 停止后端服务
docker-compose stop backend

# 停止前端服务
docker-compose stop frontend

# 停止数据库服务
docker-compose stop mysql
```

### 重新启动已停止的服务

```bash
# 启动所有已停止的服务
docker-compose start

# 启动特定服务
docker-compose start backend
docker-compose start frontend
```

---

## 🐛 进入容器调试

有时需要进入容器内部查看文件或执行命令。

### 进入后端容器

```bash
# 进入后端容器的 bash 终端
docker exec -it blog-backend bash

# 在容器内可以执行的操作：
# - 查看应用日志：cat /app/logs/spring.log
# - 查看配置文件：cat /app/application.properties
# - 查看环境变量：env | grep DATABASE
# - 退出容器：exit
```

### 进入前端容器

```bash
# 进入前端容器的 sh 终端
docker exec -it blog-frontend sh

# 在容器内可以执行的操作：
# - 查看 Nginx 配置：cat /etc/nginx/conf.d/default.conf
# - 查看访问日志：cat /var/log/nginx/access.log
# - 查看错误日志：cat /var/log/nginx/error.log
# - 测试 Nginx 配置：nginx -t
# - 退出容器：exit
```

### 进入数据库容器

```bash
# 进入 MySQL 容器
docker exec -it blog-mysql bash

# 连接到 MySQL 数据库
docker exec -it blog-mysql mysql -u root -p
# 输入密码后，可以执行 SQL 查询

# 连接到指定数据库
docker exec -it blog-mysql mysql -u bloguser -p blog
```

### 在容器中执行单个命令

```bash
# 在后端容器中查看 Java 版本
docker exec blog-backend java -version

# 在前端容器中查看 Nginx 版本
docker exec blog-frontend nginx -v

# 在数据库容器中执行 SQL 查询
docker exec blog-mysql mysql -u root -p你的密码 -e "SHOW DATABASES;"
```

---

## ⚙️ 修改配置

### 修改环境变量

环境变量存储在 `.env` 文件中，修改后需要重新构建容器。

```bash
# 1. 编辑环境变量文件
nano .env

# 2. 修改需要的配置项（如 JWT_SECRET、DDL_AUTO 等）

# 3. 重新构建并启动服务
docker-compose up -d --build

# 4. 查看日志确认配置生效
docker-compose logs -f backend
```

**常见配置修改场景**：

#### 修改数据库表管理模式

```bash
# 编辑 .env 文件
nano .env

# 将 DDL_AUTO=update 改为 DDL_AUTO=validate

# 重新构建并启动后端（注意：环境变量修改只需 --build，不需要 --no-cache）
docker-compose up -d --build backend
```

#### 修改 JWT 密钥或过期时间

```bash
# 编辑 .env 文件
nano .env

# 修改相关配置
JWT_SECRET=新的密钥
JWT_EXPIRATION_MS=7200000  # 2 小时

# 重新构建并启动后端
docker-compose up -d --build backend
```

### 修改 Nginx 配置

```bash
# 1. 编辑 Nginx 配置文件
nano frontend/nginx.conf

# 2. 修改配置（如添加缓存规则、修改代理设置等）

# 3. 重新构建并启动前端
docker-compose up -d --build frontend

# 4. 验证配置是否正确
docker exec blog-frontend nginx -t

# 5. 查看日志
docker-compose logs -f frontend
```

---

## 💾 数据库操作

### 连接数据库

```bash
# 方式 1：使用 docker exec
docker exec -it blog-mysql mysql -u bloguser -p blog

# 方式 2：使用本地 MySQL 客户端（如果已安装）
mysql -h localhost -P 3306 -u bloguser -p blog
```

### 常用数据库操作

```sql
-- 查看所有数据库
SHOW DATABASES;

-- 使用 blog 数据库
USE blog;

-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESCRIBE users;
DESCRIBE posts;

-- 查询数据
SELECT * FROM users;
SELECT * FROM posts ORDER BY created_at DESC LIMIT 10;

-- 统计数据
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM posts;

-- 退出 MySQL
EXIT;
```

### 备份数据库

```bash
# 手动备份数据库
docker exec blog-mysql mysqldump -u root -p你的密码 blog > blog_backup_$(date +%Y%m%d).sql

# 压缩备份文件
gzip blog_backup_*.sql

# 查看备份文件
ls -lh blog_backup_*.sql.gz
```

### 恢复数据库

```bash
# 解压备份文件（如果已压缩）
gunzip blog_backup_20231015.sql.gz

# 恢复数据库
docker exec -i blog-mysql mysql -u root -p你的密码 blog < blog_backup_20231015.sql

# 验证恢复是否成功
docker exec -it blog-mysql mysql -u bloguser -p blog -e "SELECT COUNT(*) FROM posts;"
```

---

## 📊 性能监控

### 查看容器资源使用情况

```bash
# 实时查看所有容器的资源使用
docker stats

# 查看特定容器的资源使用
docker stats blog-backend

# 只显示一次（不实时刷新）
docker stats --no-stream
```

### 查看磁盘使用情况

```bash
# 查看服务器磁盘使用情况
df -h

# 查看项目目录大小
du -sh /path/to/blog-project

# 查看 Docker 占用的空间
docker system df

# 查看详细信息
docker system df -v
```

### 查看网络连接

```bash
# 查看容器网络
docker network ls

# 查看特定网络的详细信息
docker network inspect blog-project_blog-network

# 查看端口占用情况
sudo netstat -tlnp | grep -E '80|8080|3306'
```

### 查看日志文件大小

```bash
# 查看所有容器的日志大小
docker ps -q | xargs docker inspect --format='{{.LogPath}}' | xargs ls -lh

# 清理日志（谨慎操作）
# 注意：这会删除所有日志
docker ps -q | xargs docker inspect --format='{{.LogPath}}' | xargs truncate -s 0
```

---

## 🧹 清理 Docker 资源

随着时间推移，Docker 会积累一些未使用的镜像、容器和数据卷。定期清理可以释放磁盘空间。

### 清理未使用的镜像

```bash
# 查看所有镜像
docker images

# 清理未使用的镜像
docker image prune

# 清理所有未使用的镜像（包括未被任何容器使用的）
docker image prune -a

# 强制清理，不提示确认
docker image prune -a -f
```

### 清理未使用的容器

```bash
# 查看所有容器（包括已停止的）
docker ps -a

# 清理已停止的容器
docker container prune

# 强制清理
docker container prune -f
```

### 清理所有未使用的资源

```bash
# 清理所有未使用的容器、网络、镜像
docker system prune

# 清理所有未使用的资源，包括数据卷（⚠️ 谨慎使用）
docker system prune -a --volumes

# 查看可以回收多少空间
docker system df
```

**⚠️ 警告**：
- 不要使用 `docker system prune -a --volumes`，除非你确定要删除所有未使用的数据卷
- 删除数据卷会导致数据库数据丢失
- 清理前建议先备份数据库

### 清理构建缓存

```bash
# 清理 Docker 构建缓存
docker builder prune

# 清理所有构建缓存
docker builder prune -a
```

---

## 💡 常见维护场景

### 场景 1：服务器重启后

服务器重启后，Docker 容器会自动启动（因为配置了 `restart: always`）。

```bash
# 检查所有服务是否正常启动
docker-compose ps

# 如果有服务未启动，手动启动
docker-compose up -d

# 查看日志
docker-compose logs --tail=100
```

### 场景 2：修改了后端代码

```bash
# 拉取最新代码
git pull

# 重新构建后端 JAR
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 重新构建并启动后端（使用 --no-cache 确保使用新的 JAR 文件）
docker-compose up -d --build --no-cache backend

# 查看日志
docker-compose logs -f backend
```

### 场景 3：修改了前端代码

```bash
# 拉取最新代码
git pull

# 重新构建并启动前端
docker-compose up -d --build frontend

# 查看日志
docker-compose logs -f frontend
```

### 场景 4：修改了环境变量

```bash
# 编辑 .env 文件
nano .env

# 重新构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f
```

### 场景 5：数据库出现问题

```bash
# 1. 查看数据库日志
docker-compose logs mysql

# 2. 尝试重启数据库
docker-compose restart mysql

# 3. 如果仍有问题，进入数据库检查
docker exec -it blog-mysql mysql -u root -p

# 4. 如果数据库损坏，从备份恢复
docker exec -i blog-mysql mysql -u root -p你的密码 blog < blog_backup.sql
```

---

## 📞 获取帮助

如果遇到问题：

1. **查看日志**：大多数问题可以通过查看日志找到原因
   ```bash
   docker-compose logs -f backend
   ```

2. **检查容器状态**：确认所有容器都在运行
   ```bash
   docker-compose ps
   ```

3. **查看常见问题排查**：参考 [Docker部署指南](./DOCKER.md) 中的"常见问题排查"章节

4. **重启服务**：有时简单的重启就能解决问题
   ```bash
   docker-compose restart
   ```

5. **重新构建**：如果问题持续，尝试重新构建
   ```bash
   docker-compose up -d --build
   ```

---

**文档版本**：1.0  
**最后更新**：2025-10-12

---

## 🔧 常见问题排查（补充）

### 问题：代码更新后必须删除容器和镜像才能部署

**症状**：
- 按照更新流程执行 `./mvnw clean package -DskipTests` 和 `docker-compose up -d --build` 后，代码没有更新
- 必须先执行 `docker-compose down` 并删除镜像，才能成功部署新代码

**原因**：
- Docker 的层缓存机制可能导致即使重新构建了 JAR 文件，Docker 也会使用旧的缓存层
- 这是因为 Docker 无法检测到 `target/` 目录中 JAR 文件的变化

**解决方法**：

在更新后端代码时，使用 `--no-cache` 标志：

```bash
# 重新构建后端 JAR
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 重新构建并启动后端（使用 --no-cache 确保使用新的 JAR 文件）
docker-compose up -d --build --no-cache backend

# 查看日志确认更新成功
docker-compose logs -f backend
```

**说明**：
- `--no-cache` 标志告诉 Docker 不使用缓存的层，从头开始构建镜像
- 这确保新的 JAR 文件被正确复制到镜像中
- 只有后端代码更新需要使用 `--no-cache`，环境变量修改不需要

### 问题：头像上传成功但显示 404

**症状**：
- 头像上传成功，数据库中有记录
- 但浏览器访问 `/uploads/头像文件` 返回 404

**排查步骤**：

1. **检查系统 Nginx 是否配置了 /uploads 反代**

```bash
# 查看系统 Nginx 配置
sudo cat /etc/nginx/sites-available/blog | grep -A 10 "uploads"
```

应该看到：

```nginx
location ^~ /uploads/ {
    proxy_pass http://127.0.0.1:8080;
    # ...
}
```

如果没有，参考 [Docker部署指南](./DOCKER.md) 中的"配置系统 Nginx"章节添加。

2. **检查上传目录挂载是否正确**

```bash
# 检查 docker-compose.yml 配置
cat docker-compose.yml | grep -A 5 "volumes:"

# 应该看到后端容器有类似配置：
# volumes:
#   - ./data/uploads:/app/uploads

# 检查宿主机目录
ls -la data/uploads

# 检查容器内目录
docker exec blog-backend ls -la /app/uploads
```

3. **检查文件权限**

```bash
# 确保上传目录有正确权限
chmod 755 data/uploads
```

4. **重新加载 Nginx 和重启后端**

```bash
sudo nginx -t
sudo systemctl reload nginx
docker-compose restart backend
```

### 问题：CORS 错误 (403 Invalid CORS request)

**症状**：
- 前端发起 API 请求时报 403 Invalid CORS request
- 浏览器控制台显示 CORS 策略阻止

**排查步骤**：

1. **确认前端是否使用了正确的 API 地址**

生产环境应该使用相对路径（同源），不触发 CORS：

```javascript
// 推荐配置
const API_BASE_URL = '/api';  // 生产环境：同源
```

2. **如果必须跨域，检查后端 CORS 配置**

```bash
# 检查 SecurityConfig.java
cat backend/blog/src/main/java/com/lost/blog/config/SecurityConfig.java | grep -A 5 "cors"

# 应该看到：
# .cors(Customizer.withDefaults())

# 检查 WebConfig.java
cat backend/blog/src/main/java/com/lost/blog/config/WebConfig.java | grep -A 10 "allowedOrigins"

# 应该看到包含你的生产域名：
# .allowedOrigins(
#     "https://myblogsystem.icu",
#     "https://www.myblogsystem.icu",
#     "http://localhost:3000"
# )
```

3. **如果配置有误，修改后重新构建**

```bash
# 修改 WebConfig.java，添加生产域名
nano backend/blog/src/main/java/com/lost/blog/config/WebConfig.java

# 重新构建后端
cd backend/blog
./mvnw clean package -DskipTests
cd ../..

# 重新部署后端容器
docker-compose up -d --build backend

# 查看日志确认启动成功
docker-compose logs -f backend
```

### 问题：证书续期失败

**症状**：
- certbot renew 失败
- 查看日志发现端口 80 被占用或验证失败

**排查步骤**：

1. **检查续期配置**

```bash
# 查看续期配置文件
sudo cat /etc/letsencrypt/renewal/myblogsystem.icu.conf

# 确认 authenticator 为 nginx
# 应该看到：
# authenticator = nginx
```

2. **测试续期流程**

```bash
# 测试续期（dry-run 模式）
sudo certbot renew --dry-run

# 查看详细日志
sudo journalctl -u certbot.service -n 50
```

3. **如果使用了错误的 authenticator，重新配置证书**

```bash
# 使用 nginx 插件重新获取证书
sudo certbot --nginx -d myblogsystem.icu -d www.myblogsystem.icu

# 选择 "2: Renew & replace" 替换现有证书
```

4. **确认 systemd timer 正在运行**

```bash
# 查看 timer 状态
sudo systemctl status certbot.timer

# 如果未运行，启用它
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# 查看下次续期时间
sudo systemctl list-timers | grep certbot
```

### 问题：Nginx 目录混乱（sites-enable vs sites-enabled）

**症状**：
- 发现有 `/etc/nginx/sites-enable` 目录（拼错）
- 不确定应该编辑哪个配置

**解决方法**：

1. **删除拼错的目录**

```bash
# 检查是否存在拼错的目录
ls -la /etc/nginx/ | grep sites

# 如果有 sites-enable（少了 d），删除它
sudo rm -rf /etc/nginx/sites-enable
```

2. **确认正确的目录结构**

```bash
# 正确的目录结构应该是：
# /etc/nginx/sites-available  - 存放配置文件（可编辑）
# /etc/nginx/sites-enabled     - 存放软链接（不要直接编辑）

# 查看启用的站点
ls -la /etc/nginx/sites-enabled/

# 应该看到软链接指向 sites-available
```

3. **编辑配置的正确方式**

```bash
# 1. 编辑 sites-available 中的文件
sudo nano /etc/nginx/sites-available/blog

# 2. 创建软链接到 sites-enabled（如果不存在）
sudo ln -s /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/

# 3. 测试配置
sudo nginx -t

# 4. 重载 Nginx
sudo systemctl reload nginx
```

---

**维护文档结束**

如需更多帮助，请参考：
- [Docker部署指南](./DOCKER.md) - 完整的部署流程
- 项目 GitHub Issues - 提交问题或查找已知问题
- 容器日志 - 使用 `docker-compose logs` 查看详细错误信息