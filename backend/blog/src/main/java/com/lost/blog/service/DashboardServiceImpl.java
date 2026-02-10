package com.lost.blog.service;

import com.lost.blog.dto.DashboardResponse;
import com.lost.blog.dto.DashboardResponse.ContentRadarData;
import com.lost.blog.dto.DashboardResponse.HotPostItem;
import com.lost.blog.dto.DashboardResponse.RecentActivityItem;
import com.lost.blog.dto.DashboardResponse.TagCategoryItem;
import com.lost.blog.dto.DashboardResponse.TrendItem;
import com.lost.blog.model.Category;
import com.lost.blog.model.PostStatus;
import com.lost.blog.model.Tag;
import com.lost.blog.repository.CategoryRepository;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.LikeRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.PostViewLogRepository;
import com.lost.blog.repository.TagRepository;
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final int TREND_DAYS = 30;

    // 雷达图评分归一化因子
    private static final double VIEWS_SCORE_DIVISOR = 10.0;       // 1000浏览量 = 满分100
    private static final double COMMENTS_SCORE_MULTIPLIER = 2.0;  // 50条评论 = 满分100
    private static final double ENGAGEMENT_MULTIPLIER = 10.0;     // 用户参与度缩放因子
    private static final double FRESHNESS_BOOST_FACTOR = 4.0;     // 内容新鲜度增幅（25%为基准线）

    // 热力图颜色方案
    private static final String[] TAG_COLORS = {
        "#1890ff", "#52c41a", "#faad14", "#ff4d4f", "#722ed1",
        "#13c2c2", "#eb2f96", "#2f54eb", "#fa8c16", "#a0d911"
    };
    private static final String[] CATEGORY_COLORS = {
        "#2f54eb", "#1890ff", "#13c2c2", "#52c41a", "#a0d911",
        "#faad14", "#fa8c16", "#ff4d4f", "#eb2f96", "#722ed1"
    };

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostViewLogRepository postViewLogRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final LikeRepository likeRepository;

    @Autowired
    public DashboardServiceImpl(UserRepository userRepository,
                                PostRepository postRepository,
                                CommentRepository commentRepository,
                                PostViewLogRepository postViewLogRepository,
                                TagRepository tagRepository,
                                CategoryRepository categoryRepository,
                                LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postViewLogRepository = postViewLogRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.likeRepository = likeRepository;
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

        // 标签/分类热力图
        populateTagCategoryStats(response);

        // 系统概览
        populateSystemOverview(response);

        // 最近活动
        populateRecentActivities(response, todayStart);

        // 内容质量雷达图
        populateContentRadar(response);

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
     * 填充热门文章 TOP10（按热度公式排序）
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
            item.setHeatScore(((Number) row[6]).doubleValue());
            hotPosts.add(item);
        }

        response.setHotPosts(hotPosts);
    }

    /**
     * 填充标签和分类统计数据（用于热力图）
     */
    private void populateTagCategoryStats(DashboardResponse response) {
        // 标签统计
        List<Tag> popularTags = tagRepository.findPopularTags();
        List<TagCategoryItem> tagStats = new ArrayList<>();

        for (int i = 0; i < popularTags.size(); i++) {
            Tag tag = popularTags.get(i);
            long postCount = tag.getPosts() != null ? tag.getPosts().size() : 0;
            tagStats.add(new TagCategoryItem(
                tag.getId(),
                tag.getName(),
                postCount,
                tag.getColor() != null ? tag.getColor() : TAG_COLORS[i % TAG_COLORS.length]
            ));
        }
        response.setTagStats(tagStats);

        // 分类统计
        List<Category> popularCategories = categoryRepository.findPopularCategories();
        List<TagCategoryItem> categoryStats = new ArrayList<>();

        for (int i = 0; i < popularCategories.size(); i++) {
            Category cat = popularCategories.get(i);
            // For categories, count posts using the repository query
            List<Object[]> catPosts = categoryRepository.findPostsByCategoryId(cat.getId());
            long postCount = catPosts != null ? catPosts.size() : 0;
            categoryStats.add(new TagCategoryItem(
                cat.getId(),
                cat.getName(),
                postCount,
                cat.getColor() != null ? cat.getColor() : CATEGORY_COLORS[i % CATEGORY_COLORS.length]
            ));
        }
        response.setCategoryStats(categoryStats);
    }

    /**
     * 填充系统概览数据
     */
    private void populateSystemOverview(DashboardResponse response) {
        response.setTotalTags(tagRepository.count());
        response.setTotalCategories(categoryRepository.count());
        response.setEnabledUsers(userRepository.countByEnabled(true));
        response.setDisabledUsers(userRepository.countByEnabled(false));
    }

    /**
     * 填充最近活动
     */
    private void populateRecentActivities(DashboardResponse response, LocalDateTime todayStart) {
        List<RecentActivityItem> activities = new ArrayList<>();

        // 获取今日新用户数
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long newUsersToday = userRepository.countByCreatedAtBetween(todayStart, todayEnd);
        if (newUsersToday > 0) {
            activities.add(new RecentActivityItem("user", newUsersToday + " 位新用户注册", LocalDate.now().format(DATE_FORMATTER), "👤"));
        }

        // 获取今日新文章数
        long newPostsToday = postRepository.countByCreatedAtBetween(todayStart, todayEnd);
        if (newPostsToday > 0) {
            activities.add(new RecentActivityItem("post", newPostsToday + " 篇新文章发布", LocalDate.now().format(DATE_FORMATTER), "📝"));
        }

        // 获取今日新评论数
        long newCommentsToday = commentRepository.countByCreatedAtBetween(todayStart, todayEnd);
        if (newCommentsToday > 0) {
            activities.add(new RecentActivityItem("comment", newCommentsToday + " 条新评论", LocalDate.now().format(DATE_FORMATTER), "💬"));
        }

        // 获取待审核文章数
        long pendingPosts = postRepository.countByStatus(PostStatus.PENDING_REVIEW);
        if (pendingPosts > 0) {
            activities.add(new RecentActivityItem("pending", pendingPosts + " 篇文章待审核", "待处理", "⏳"));
        }

        // 获取昨日数据
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);
        long yesterdayViews = postViewLogRepository.countByCreateTimeBetween(yesterdayStart, yesterdayEnd);
        if (yesterdayViews > 0) {
            activities.add(new RecentActivityItem("view", "昨日 " + yesterdayViews + " 次浏览", LocalDate.now().minusDays(1).format(DATE_FORMATTER), "👁"));
        }

        response.setRecentActivities(activities);
    }

    /**
     * 填充内容质量雷达图数据（0-100分制）
     */
    private void populateContentRadar(DashboardResponse response) {
        ContentRadarData radar = new ContentRadarData();

        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();
        long totalViews = postRepository.sumAllViewCount();
        long totalLikes = likeRepository.count();
        long publishedPosts = postRepository.countByStatus(PostStatus.PUBLISHED);

        if (totalPosts > 0) {
            // 平均浏览量（归一化到0-100）
            double avgViews = (double) totalViews / totalPosts;
            radar.setAvgViewsPerPost(Math.min(avgViews / VIEWS_SCORE_DIVISOR, 100));

            // 平均评论数（归一化到0-100）
            double avgComments = (double) totalComments / totalPosts;
            radar.setAvgCommentsPerPost(Math.min(avgComments * COMMENTS_SCORE_MULTIPLIER, 100));

            // 平均点赞数（归一化，假设100为满分）
            double avgLikes = (double) totalLikes / totalPosts;
            radar.setAvgLikesPerPost(Math.min(avgLikes, 100));

            // 发布率
            radar.setPublishRate((double) publishedPosts / totalPosts * 100);
        }

        // 用户参与度（有评论或点赞的比率）
        long totalUsers = userRepository.count();
        if (totalUsers > 0) {
            radar.setUserEngagement(Math.min((double)(totalComments + totalLikes) / totalUsers * ENGAGEMENT_MULTIPLIER, 100));
        }

        // 内容新鲜度（最近7天发布的文章占比）
        LocalDateTime weekAgo = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime now = LocalDate.now().atTime(LocalTime.MAX);
        long recentPosts = postRepository.countByCreatedAtBetween(weekAgo, now);
        if (totalPosts > 0) {
            radar.setContentFreshness(Math.min((double) recentPosts / totalPosts * 100 * FRESHNESS_BOOST_FACTOR, 100));
        }

        response.setContentRadar(radar);
    }
}
