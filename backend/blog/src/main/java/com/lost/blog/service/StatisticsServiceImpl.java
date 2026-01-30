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