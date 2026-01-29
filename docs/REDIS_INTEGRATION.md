# Redis Integration Guide - Active User Tracking

本文档详细说明如何在博客系统中集成Redis来实现实时活跃用户追踪。

## 为什么需要Redis?

当前实现的局限性：
- 只能统计最近15分钟内更新过个人资料的用户
- 无法追踪用户的浏览、搜索、阅读等行为
- 不适合分布式部署（多台服务器各自统计）

Redis的优势：
- **实时性**：用户任何操作都可以即时记录
- **高性能**：内存操作，响应极快
- **自动过期**：使用TTL自动清理过期用户
- **分布式支持**：多台服务器共享同一个Redis实例

---

## 实施步骤

### 第1步：添加Redis依赖

编辑 `backend/blog/pom.xml`，添加：

```xml
<dependencies>
    <!-- 现有依赖... -->
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
</dependencies>
```

### 第2步：配置Redis连接

编辑 `backend/blog/src/main/resources/application.properties`：

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=2000ms

# Redis连接池配置
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
spring.data.redis.lettuce.pool.max-wait=-1ms

# Cache Configuration (可选，用于其他缓存场景)
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
```

**开发环境**：如果你的开发机器没有Redis，设置：
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**生产环境**：根据你的Redis服务器配置修改。

### 第3步：创建Redis配置类

创建 `backend/blog/src/main/java/com/lost/blog/config/RedisConfig.java`：

```java
package com.lost.blog.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置RedisTemplate用于操作Redis数据
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 使用String作为key的序列化器，JSON作为value的序列化器
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用String序列化器序列化key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // 使用JSON序列化器序列化value
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

### 第4步：创建活跃用户追踪Service

创建 `backend/blog/src/main/java/com/lost/blog/service/ActiveUserService.java`：

```java
package com.lost.blog.service;

/**
 * 活跃用户追踪服务接口
 */
public interface ActiveUserService {
    
    /**
     * 记录用户活跃（用户进行了任何操作）
     * @param userId 用户ID
     */
    void recordUserActivity(Long userId);
    
    /**
     * 获取当前活跃用户数量
     * @return 活跃用户数量
     */
    Long getActiveUserCount();
}
```

创建 `backend/blog/src/main/java/com/lost/blog/service/ActiveUserServiceImpl.java`：

```java
package com.lost.blog.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 活跃用户追踪服务实现（基于Redis）
 * 
 * 实现逻辑：
 * 1. 每当用户进行操作时，在Redis中记录该用户
 * 2. 使用SET数据结构存储活跃用户ID
 * 3. 为每个用户设置15分钟的过期时间
 * 4. 统计时直接返回SET的大小
 */
@Service
public class ActiveUserServiceImpl implements ActiveUserService {
    
    private static final String ACTIVE_USER_KEY_PREFIX = "active:user:";
    private static final long ACTIVE_THRESHOLD_SECONDS = 15 * 60; // 15分钟
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public ActiveUserServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 记录用户活跃
     * 在Redis中为该用户设置一个键，15分钟后自动过期
     */
    @Override
    public void recordUserActivity(Long userId) {
        if (userId == null) {
            return;
        }
        
        String key = ACTIVE_USER_KEY_PREFIX + userId;
        // 设置键值，15分钟后自动过期
        redisTemplate.opsForValue().set(key, "1", ACTIVE_THRESHOLD_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * 获取当前活跃用户数量
     * 通过Redis KEYS命令查找所有active:user:*的键
     */
    @Override
    public Long getActiveUserCount() {
        try {
            // 使用KEYS命令查找所有活跃用户键
            // 注意：在生产环境如果用户量大，考虑使用SCAN命令替代KEYS
            var keys = redisTemplate.keys(ACTIVE_USER_KEY_PREFIX + "*");
            return keys != null ? (long) keys.size() : 0L;
        } catch (Exception e) {
            // Redis异常时返回0，不影响主业务
            return 0L;
        }
    }
}
```

**性能优化建议**：
如果用户量非常大（>10万），使用SCAN命令替代KEYS：
```java
@Override
public Long getActiveUserCount() {
    try {
        AtomicLong count = new AtomicLong(0);
        ScanOptions options = ScanOptions.scanOptions()
            .match(ACTIVE_USER_KEY_PREFIX + "*")
            .count(1000)
            .build();
        
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                cursor.next();
                count.incrementAndGet();
            }
            cursor.close();
            return null;
        });
        
        return count.get();
    } catch (Exception e) {
        return 0L;
    }
}
```

### 第5步：更新StatisticsServiceImpl

修改 `backend/blog/src/main/java/com/lost/blog/service/StatisticsServiceImpl.java`：

```java
package com.lost.blog.service;

import com.lost.blog.dto.StatisticsResponse;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.PostViewLogRepository;
import com.lost.blog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostViewLogRepository postViewLogRepository;
    private final ActiveUserService activeUserService;  // 注入ActiveUserService
    
    public StatisticsServiceImpl(
            UserRepository userRepository,
            PostRepository postRepository,
            PostViewLogRepository postViewLogRepository,
            ActiveUserService activeUserService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postViewLogRepository = postViewLogRepository;
        this.activeUserService = activeUserService;
    }
    
    @Override
    public StatisticsResponse getStatistics() {
        // 用户总数
        Long totalUsers = userRepository.count();
        
        // 文章总数（仅统计已发布）
        Long totalPosts = postRepository.countByDraftFalse();
        
        // 活跃用户数（从Redis获取）
        Long onlineUsers = activeUserService.getActiveUserCount();
        
        // 今日访问数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayVisits = postViewLogRepository.countByCreateTimeBetween(todayStart, todayEnd);
        
        return new StatisticsResponse(totalUsers, totalPosts, onlineUsers, todayVisits);
    }
}
```

### 第6步：创建用户活跃追踪拦截器

创建 `backend/blog/src/main/java/com/lost/blog/interceptor/UserActivityInterceptor.java`：

```java
package com.lost.blog.interceptor;

import com.lost.blog.service.ActiveUserService;
import com.lost.blog.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户活跃追踪拦截器
 * 拦截所有已认证用户的请求，记录其活跃状态
 */
@Component
public class UserActivityInterceptor implements HandlerInterceptor {
    
    private final ActiveUserService activeUserService;
    private final JwtUtil jwtUtil;
    
    public UserActivityInterceptor(ActiveUserService activeUserService, JwtUtil jwtUtil) {
        this.activeUserService = activeUserService;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取JWT token
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                // 解析token获取用户ID
                Long userId = jwtUtil.getUserIdFromToken(token);
                
                if (userId != null) {
                    // 记录用户活跃
                    activeUserService.recordUserActivity(userId);
                }
            } catch (Exception e) {
                // Token解析失败，忽略（用户未登录或token无效）
            }
        }
        
        return true;
    }
}
```

### 第7步：注册拦截器

修改 `backend/blog/src/main/java/com/lost/blog/config/WebMvcConfig.java`（如果不存在则创建）：

```java
package com.lost.blog.config;

import com.lost.blog.interceptor.UserActivityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final UserActivityInterceptor userActivityInterceptor;
    
    public WebMvcConfig(UserActivityInterceptor userActivityInterceptor) {
        this.userActivityInterceptor = userActivityInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/api/auth/**",       // 排除认证相关接口
                        "/api/statistics",    // 排除统计接口（公开访问）
                        "/error"              // 排除错误页面
                );
    }
}
```

---

## 安装和运行Redis

### Windows

**方法1：使用Windows Subsystem for Linux (WSL)**
```bash
# 安装WSL中的Redis
sudo apt update
sudo apt install redis-server

# 启动Redis
sudo service redis-server start

# 测试连接
redis-cli ping
# 应该返回：PONG
```

**方法2：使用Redis for Windows（非官方）**
1. 下载：https://github.com/microsoftarchive/redis/releases
2. 解压到目录，如 `C:\Redis`
3. 运行 `redis-server.exe`

### macOS

```bash
# 使用Homebrew安装
brew install redis

# 启动Redis
brew services start redis

# 测试连接
redis-cli ping
# 应该返回：PONG
```

### Linux (Ubuntu/Debian)

```bash
# 安装Redis
sudo apt update
sudo apt install redis-server

# 启动Redis
sudo systemctl start redis-server

# 设置开机自启
sudo systemctl enable redis-server

# 测试连接
redis-cli ping
# 应该返回：PONG
```

### Docker

```bash
# 拉取Redis镜像
docker pull redis:latest

# 运行Redis容器
docker run -d \
  --name blog-redis \
  -p 6379:6379 \
  redis:latest

# 测试连接
docker exec -it blog-redis redis-cli ping
# 应该返回：PONG
```

---

## 测试集成

### 1. 启动Redis
确保Redis服务正在运行（见上面的安装说明）。

### 2. 启动后端服务
```bash
cd backend/blog
mvn spring-boot:run
```

如果看到类似日志，说明Redis连接成功：
```
Lettuce ConnectionFactory initialized
```

### 3. 测试活跃用户追踪

**测试步骤**：
1. 登录系统（获取JWT token）
2. 浏览几个页面（阅读文章、搜索等）
3. 访问统计API：`GET http://localhost:8080/api/statistics`
4. 应该看到 `onlineUsers` 数字增加

**使用Redis CLI验证**：
```bash
# 连接到Redis
redis-cli

# 查看所有活跃用户键
KEYS active:user:*

# 查看某个用户的过期时间
TTL active:user:1

# 应该返回剩余秒数，如：899（约15分钟）
```

### 4. 验证自动过期

等待15分钟后，再次检查：
```bash
redis-cli
KEYS active:user:*
# 应该返回：(empty list)
```

---

## 性能优化建议

### 1. 使用Redis连接池

配置已经包含连接池设置：
```properties
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
```

### 2. 避免KEYS命令（生产环境）

在用户量非常大的情况下，使用SCAN命令替代KEYS（见第4步的性能优化建议）。

### 3. 考虑使用Bitmap

如果用户ID是连续的整数，可以使用Redis Bitmap来节省内存：

```java
@Override
public void recordUserActivity(Long userId) {
    String key = "active:users:bitmap";
    redisTemplate.opsForValue().setBit(key, userId, true);
    redisTemplate.expire(key, ACTIVE_THRESHOLD_SECONDS, TimeUnit.SECONDS);
}

@Override
public Long getActiveUserCount() {
    String key = "active:users:bitmap";
    return redisTemplate.execute((RedisCallback<Long>) connection -> {
        return connection.bitCount(key.getBytes());
    });
}
```

### 4. 分片策略（超大规模）

如果活跃用户数超过100万，可以按时间分片：

```java
private String getCurrentShardKey() {
    // 每小时一个分片
    long hour = System.currentTimeMillis() / (60 * 60 * 1000);
    return ACTIVE_USER_KEY_PREFIX + "shard:" + hour + ":";
}
```

---

## 故障处理

### Redis连接失败

**错误信息**：
```
Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException
```

**解决方法**：
1. 确认Redis正在运行：`redis-cli ping`
2. 检查防火墙设置
3. 验证`application.properties`中的Redis配置

### Redis性能问题

**问题**：统计接口响应慢

**解决方法**：
1. 使用SCAN替代KEYS命令
2. 增加Redis内存
3. 启用Redis持久化（可选）
4. 考虑使用Redis Cluster（分布式）

---

## 迁移方案

如果你已经有现有用户在使用系统，迁移到Redis活跃追踪：

### 平滑迁移

1. **部署Redis但保留现有逻辑**（第1-4步）
2. **添加拦截器开始追踪**（第5-7步），但不修改StatisticsServiceImpl
3. **观察Redis数据**（运行1-2天）
4. **切换到Redis统计**（第8步）

这样可以确保Redis正常工作后再切换，避免影响用户体验。

---

## 总结

**实施后的效果**：
- ✅ 任何已登录用户的操作都会被追踪（浏览、搜索、评论等）
- ✅ 15分钟无活动自动标记为离线
- ✅ 支持分布式部署
- ✅ 高性能，低延迟
- ✅ 自动清理过期数据

**代码变更清单**：
1. 添加Redis依赖（pom.xml）
2. 配置Redis连接（application.properties）
3. 创建RedisConfig配置类
4. 创建ActiveUserService接口和实现
5. 修改StatisticsServiceImpl使用ActiveUserService
6. 创建UserActivityInterceptor拦截器
7. 注册拦截器（WebMvcConfig）

**运维要求**：
- 需要运行Redis服务器（本地开发或生产环境）
- Redis数据可以不持久化（重启后活跃用户清零，无影响）
- 建议配置Redis监控（如RedisInsight）

有任何问题，请参考：
- [Spring Data Redis官方文档](https://spring.io/projects/spring-data-redis)
- [Redis官方文档](https://redis.io/documentation)
