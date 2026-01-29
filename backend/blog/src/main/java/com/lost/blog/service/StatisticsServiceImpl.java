package com.lost.blog.service;

import com.lost.blog.dto.StatisticsResponse;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.PostViewLogRepository;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 统计服务实现类
 * 提供社区统计数据的具体实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostViewLogRepository postViewLogRepository;

    // 在线用户定义：最近15分钟内有活动的用户
    private static final int ONLINE_THRESHOLD_MINUTES = 15;

    @Autowired
    public StatisticsServiceImpl(UserRepository userRepository, 
                                  PostRepository postRepository,
                                  PostViewLogRepository postViewLogRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postViewLogRepository = postViewLogRepository;
    }

    /**
     * 获取社区统计信息
     * 使用缓存减少数据库查询压力，缓存时间30秒
     */
    @Override
    @Cacheable(value = "statistics", key = "'communityStats'")
    public StatisticsResponse getStatistics() {
        // 1. 获取用户总数
        Long totalUsers = userRepository.count();

        // 2. 获取文章总数（仅已发布的文章）
        Long totalPosts = postRepository.countByDraftFalse();

        // 3. 获取在线用户数（基于最后更新时间或创建时间）
        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        // 查询updatedAt在阈值之后的用户，如果updatedAt为null则使用createdAt
        Long onlineUsers = userRepository.countByUpdatedAtAfterOrUpdatedAtIsNullAndCreatedAtAfter(
            onlineThreshold, onlineThreshold);

        // 4. 获取今日访问数（基于PostViewLog表）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayVisits = postViewLogRepository.countByCreateTimeBetween(todayStart, todayEnd);

        return new StatisticsResponse(totalUsers, totalPosts, onlineUsers, todayVisits);
    }
}
