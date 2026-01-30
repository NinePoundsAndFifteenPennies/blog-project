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
