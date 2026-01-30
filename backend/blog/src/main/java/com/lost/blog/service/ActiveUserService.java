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
