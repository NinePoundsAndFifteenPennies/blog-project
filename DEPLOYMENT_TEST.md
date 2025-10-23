# 部署测试指南

部署完成后，请按以下步骤验证系统是否正常工作。

## 1. 检查容器状态

```bash
docker-compose ps
```

应该看到三个容器都在运行（Up）：
- blog-mysql
- blog-backend
- blog-frontend

## 2. 检查日志

```bash
# 查看所有容器日志
docker-compose logs

# 或分别查看
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql
```

确认没有错误信息。

## 3. 检查端口

```bash
# 检查端口占用情况
sudo netstat -tlnp | grep -E '80|8080|8081|3306'
```

应该看到：
- `0.0.0.0:80` - 系统 Nginx（或 nginx）
- `0.0.0.0:443` - 系统 Nginx（如果配置了 HTTPS）
- `0.0.0.0:8080` - blog-backend（Docker）
- `0.0.0.0:8081` - blog-frontend（Docker）
- `0.0.0.0:3306` - blog-mysql（Docker）

## 4. 测试后端 API

```bash
# 测试后端健康检查（如果有）
curl http://localhost:8080/api/

# 测试通过前端容器访问
curl http://localhost:8081/
```

## 5. 测试系统 Nginx 代理

```bash
# 测试 HTTP 访问
curl -I http://localhost/

# 测试 HTTPS 访问（如果配置了）
curl -I https://myblogsystem.icu/

# 测试 API 代理
curl http://localhost/api/
```

## 6. 浏览器测试

打开浏览器，访问：

1. **前端页面**：`https://myblogsystem.icu` 或 `http://你的服务器IP`
   - 应该能看到博客首页
   
2. **注册功能**：
   - 点击注册按钮
   - 填写用户名、邮箱、密码
   - 提交注册
   
3. **登录功能**：
   - 使用刚注册的账号登录
   
4. **创建文章**：
   - 登录后点击"创建文章"
   - 填写标题和内容
   - 保存文章
   
5. **查看文章**：
   - 返回首页
   - 应该能看到刚创建的文章

## 7. 检查数据库

```bash
# 进入数据库容器
docker exec -it blog-mysql mysql -u root -p

# 输入密码后执行
USE blog;
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM posts;
```

应该能看到刚注册的用户和创建的文章。

## 8. 性能测试（可选）

```bash
# 使用 Apache Bench 测试
ab -n 100 -c 10 http://localhost/

# 或使用 curl 测试响应时间
curl -o /dev/null -s -w "Time: %{time_total}s\n" https://myblogsystem.icu/
```

## 常见问题排查

### 问题 1：容器启动失败

```bash
# 查看详细日志
docker-compose logs -f backend

# 检查环境变量
cat .env

# 重新构建并启动
docker-compose down
docker-compose up -d --build
```

### 问题 2：前端无法访问

```bash
# 检查 Nginx 配置
sudo nginx -t

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/error.log

# 检查防火墙
sudo ufw status
```

### 问题 3：后端 API 报错

```bash
# 查看后端日志
docker-compose logs backend | grep -i error

# 检查数据库连接
docker exec -it blog-mysql mysql -u bloguser -p
```

### 问题 4：系统 Nginx 无法启动

```bash
# 检查端口占用
sudo lsof -i :80
sudo lsof -i :443

# 检查配置文件
sudo nginx -t

# 查看详细错误
sudo journalctl -u nginx -n 50
```

## 验证清单

部署完成后，请确认以下所有项：

- [ ] 所有 Docker 容器正常运行
- [ ] 可以访问前端页面
- [ ] 可以注册新用户
- [ ] 可以登录
- [ ] 可以创建文章
- [ ] 可以查看文章列表
- [ ] 可以编辑自己的文章
- [ ] 可以删除自己的文章
- [ ] HTTPS 正常工作（如果配置了）
- [ ] 证书自动续期配置完成（如果使用 Let's Encrypt）

## 下一步

验证通过后，建议：

1. 设置数据库定期备份
2. 配置日志轮转
3. 设置监控告警
4. 优化 Nginx 缓存配置

详见：`docs/deployment/MAINTENANCE.md`
