package com.lost.blog.service;

import com.lost.blog.dto.DashboardResponse;
import com.lost.blog.dto.DashboardResponse.HotPostItem;
import com.lost.blog.dto.DashboardResponse.TrendItem;
import com.lost.blog.model.PostStatus;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.PostViewLogRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 仪表盘服务实现
 * 聚合多个数据源，生成仪表盘所需的统计数据
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final int TREND_DAYS = 30;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostViewLogRepository postViewLogRepository;

    @Autowired
    public DashboardServiceImpl(UserRepository userRepository,
                                PostRepository postRepository,
                                CommentRepository commentRepository,
                                PostViewLogRepository postViewLogRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postViewLogRepository = postViewLogRepository;
    }

    @Override
    public DashboardResponse getDashboardData(String adminUsername) {
        DashboardResponse response = new DashboardResponse();
        response.setAdmin(adminUsername);
        response.setMessage("欢迎进入管理后台");

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 核心统计数据
        populateCoreStats(response, todayStart, todayEnd);

        // 文章状态分布
        populatePostStatusDistribution(response);

        // 趋势数据（最近30天）
        populateTrends(response);

        // 热门文章 TOP10
        populateHotPosts(response);

        return response;
    }

    /**
     * 填充核心统计数据
     */
    private void populateCoreStats(DashboardResponse response, LocalDateTime todayStart, LocalDateTime todayEnd) {
        // 用户统计
        response.setTotalUsers(userRepository.count());
        response.setTodayNewUsers(userRepository.countByCreatedAtBetween(todayStart, todayEnd));

        // 文章统计
        response.setTotalPosts(postRepository.count());
        response.setTodayNewPosts(postRepository.countByCreatedAtBetween(todayStart, todayEnd));

        // 评论统计
        response.setTotalComments(commentRepository.count());
        response.setTodayNewComments(commentRepository.countByCreatedAtBetween(todayStart, todayEnd));

        // 浏览量统计
        response.setTotalViews(postRepository.sumAllViewCount());
        response.setTodayViews(postViewLogRepository.countByCreateTimeBetween(todayStart, todayEnd));
    }

    /**
     * 填充文章状态分布
     */
    private void populatePostStatusDistribution(DashboardResponse response) {
        response.setPublishedPosts(postRepository.countByStatus(PostStatus.PUBLISHED));
        response.setDraftPosts(postRepository.countByStatus(PostStatus.DRAFT));
        response.setPendingPosts(postRepository.countByStatus(PostStatus.PENDING_REVIEW));
        response.setRejectedPosts(postRepository.countByStatus(PostStatus.REJECTED));
    }

    /**
     * 填充最近30天趋势数据
     */
    private void populateTrends(DashboardResponse response) {
        List<TrendItem> userTrend = new ArrayList<>();
        List<TrendItem> postTrend = new ArrayList<>();
        List<TrendItem> commentTrend = new ArrayList<>();
        List<TrendItem> viewTrend = new ArrayList<>();

        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            String dateStr = date.format(DATE_FORMATTER);

            userTrend.add(new TrendItem(dateStr, userRepository.countByCreatedAtBetween(dayStart, dayEnd)));
            postTrend.add(new TrendItem(dateStr, postRepository.countByCreatedAtBetween(dayStart, dayEnd)));
            commentTrend.add(new TrendItem(dateStr, commentRepository.countByCreatedAtBetween(dayStart, dayEnd)));
            viewTrend.add(new TrendItem(dateStr, postViewLogRepository.countByCreateTimeBetween(dayStart, dayEnd)));
        }

        response.setUserTrend(userTrend);
        response.setPostTrend(postTrend);
        response.setCommentTrend(commentTrend);
        response.setViewTrend(viewTrend);
    }

    /**
     * 填充热门文章 TOP10
     */
    private void populateHotPosts(DashboardResponse response) {
        List<Object[]> results = postRepository.findTopHotPosts(10);
        List<HotPostItem> hotPosts = new ArrayList<>();

        for (Object[] row : results) {
            HotPostItem item = new HotPostItem();
            item.setId(((Number) row[0]).longValue());
            item.setTitle((String) row[1]);
            item.setAuthor(row[2] != null ? (String) row[2] : "未知");
            item.setViewCount(((Number) row[3]).longValue());
            item.setLikeCount(((Number) row[4]).longValue());
            item.setCommentCount(((Number) row[5]).longValue());
            hotPosts.add(item);
        }

        response.setHotPosts(hotPosts);
    }
}
