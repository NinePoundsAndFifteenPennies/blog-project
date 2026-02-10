package com.lost.blog.service;

import com.lost.blog.dto.DashboardResponse;

/**
 * 仪表盘服务接口
 * 提供管理后台仪表盘所需的统计数据
 */
public interface DashboardService {

    /**
     * 获取仪表盘完整数据
     * 包含核心统计、趋势数据、热门文章等
     *
     * @param adminUsername 当前管理员用户名
     * @return 仪表盘数据
     */
    DashboardResponse getDashboardData(String adminUsername);
}
