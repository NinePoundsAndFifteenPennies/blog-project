package com.lost.blog.service;

import com.lost.blog.dto.StatisticsResponse;

/**
 * 统计服务接口
 * 提供社区统计数据
 */
public interface StatisticsService {

    /**
     * 获取社区统计信息
     * @return 统计信息响应DTO
     */
    StatisticsResponse getStatistics();
}
