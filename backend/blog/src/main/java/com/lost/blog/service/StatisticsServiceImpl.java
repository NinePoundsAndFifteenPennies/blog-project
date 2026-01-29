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
    // 注意：当前基于用户updatedAt字段,只在用户信息更新时触发
    // 建议：添加lastActivityAt字段或使用Session/Redis跟踪真实在线状态
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
     * 不使用缓存,确保每次都能获取到最新数据
     * 如果高并发场景需要缓存,建议配置较短的过期时间(如5秒)或使用Redis缓存
     */
    @Override
    public StatisticsResponse getStatistics() {
        // 1. 获取用户总数
        Long totalUsers = userRepository.count();

        // 2. 获取文章总数（仅已发布的文章）
        Long totalPosts = postRepository.countByDraftFalse();

        // 3. 获取在线用户数
        // 当前方案：统计最近15分钟内注册或更新过资料的用户
        // 局限性：只有用户更新个人信息时才会更新updatedAt，无法反映真实浏览活动
        // 改进建议：
        //   - 方案1：添加lastActivityAt字段，在用户任何操作时更新（发帖、评论、点赞、浏览等）
        //   - 方案2：使用HttpSession或JWT token过期时间跟踪在线状态
        //   - 方案3：使用Redis存储活跃用户集合，设置过期时间
        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        Long onlineUsers = userRepository.countByUpdatedAtAfterOrUpdatedAtIsNullAndCreatedAtAfter(
            onlineThreshold, onlineThreshold);

        // 4. 获取今日访问数（基于PostViewLog表）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayVisits = postViewLogRepository.countByCreateTimeBetween(todayStart, todayEnd);

        return new StatisticsResponse(totalUsers, totalPosts, onlineUsers, todayVisits);
    }
}
