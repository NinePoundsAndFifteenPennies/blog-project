package com.lost.blog.service;

import com.lost.blog.dto.HotAuthorResponse;
import com.lost.blog.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 热门作者服务实现类
 * 使用加权对数混合模型计算作者热度值
 * 
 * 公式: H = W_a * A + W_f * F + (W_l * L + W_c * C) + W_v * log10(V + 1)
 * 
 * 权重建议:
 * - W_a (文章数量): 5.0
 * - W_f (粉丝数): 15.0 (最高权重)
 * - W_l (点赞数): 2.5
 * - W_c (评论数): 6.0
 * - W_v (浏览量): 1.0 (对数处理后)
 */
@Service
public class HotAuthorServiceImpl implements HotAuthorService {

    // 权重常量
    private static final double WEIGHT_ARTICLES = 5.0;
    private static final double WEIGHT_FOLLOWERS = 15.0;
    private static final double WEIGHT_LIKES = 2.5;
    private static final double WEIGHT_COMMENTS = 6.0;
    private static final double WEIGHT_VIEWS = 1.0;
    
    // 最大返回作者数量
    public static final int MAX_HOT_AUTHORS_LIMIT = 30;

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;

    public HotAuthorServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<HotAuthorResponse> getHotAuthors(int limit) {
        // 限制最大返回数量
        int effectiveLimit = Math.min(Math.max(limit, 1), MAX_HOT_AUTHORS_LIMIT);

        // 使用原生SQL查询，计算每个作者的热度值
        // 只统计有发布文章的作者（非草稿）
        String sql = """
            SELECT 
                u.id,
                u.username,
                u.nickname,
                u.avatar_url,
                u.bio,
                COALESCE(f.follower_count, 0) AS follower_count,
                COALESCE(p.article_count, 0) AS article_count,
                COALESCE(lk.like_count, 0) AS like_count,
                COALESCE(c.comment_count, 0) AS comment_count,
                COALESCE(p.view_count, 0) AS view_count,
                COALESCE(u.updated_at, u.created_at) AS last_active_at,
                (
                    :weightArticles * COALESCE(p.article_count, 0) +
                    :weightFollowers * COALESCE(f.follower_count, 0) +
                    :weightLikes * COALESCE(lk.like_count, 0) +
                    :weightComments * COALESCE(c.comment_count, 0) +
                    :weightViews * LOG10(COALESCE(p.view_count, 0) + 1)
                ) AS heat_score
            FROM users u
            LEFT JOIN (
                SELECT followed_id, COUNT(*) AS follower_count 
                FROM follows 
                GROUP BY followed_id
            ) f ON u.id = f.followed_id
            LEFT JOIN (
                SELECT user_id, COUNT(*) AS article_count, SUM(view_count) AS view_count 
                FROM posts 
                WHERE is_draft = false 
                GROUP BY user_id
            ) p ON u.id = p.user_id
            LEFT JOIN (
                SELECT po.user_id, COUNT(l.id) AS like_count 
                FROM posts po 
                JOIN likes l ON po.id = l.post_id 
                WHERE po.is_draft = false 
                GROUP BY po.user_id
            ) lk ON u.id = lk.user_id
            LEFT JOIN (
                SELECT po.user_id, COUNT(cm.id) AS comment_count 
                FROM posts po 
                JOIN comments cm ON po.id = cm.post_id 
                WHERE po.is_draft = false 
                GROUP BY po.user_id
            ) c ON u.id = c.user_id
            WHERE p.article_count > 0
            ORDER BY heat_score DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("weightArticles", WEIGHT_ARTICLES);
        query.setParameter("weightFollowers", WEIGHT_FOLLOWERS);
        query.setParameter("weightLikes", WEIGHT_LIKES);
        query.setParameter("weightComments", WEIGHT_COMMENTS);
        query.setParameter("weightViews", WEIGHT_VIEWS);
        query.setParameter("limit", effectiveLimit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<HotAuthorResponse> hotAuthors = new ArrayList<>();
        for (Object[] row : results) {
            HotAuthorResponse response = new HotAuthorResponse();
            response.setId(((Number) row[0]).longValue());
            response.setUsername((String) row[1]);
            response.setNickname((String) row[2]);
            response.setAvatarUrl((String) row[3]);
            response.setBio((String) row[4]);
            response.setFollowerCount(((Number) row[5]).longValue());
            response.setArticleCount(((Number) row[6]).longValue());
            response.setLikeCount(((Number) row[7]).longValue());
            response.setCommentCount(((Number) row[8]).longValue());
            response.setViewCount(((Number) row[9]).longValue());
            
            // 处理最后活跃时间
            Object lastActiveAt = row[10];
            if (lastActiveAt instanceof Timestamp) {
                response.setLastActiveAt(((Timestamp) lastActiveAt).toLocalDateTime());
            } else if (lastActiveAt instanceof LocalDateTime) {
                response.setLastActiveAt((LocalDateTime) lastActiveAt);
            }
            
            // 处理热度值
            Object heatScore = row[11];
            if (heatScore instanceof BigDecimal) {
                response.setHeatScore(((BigDecimal) heatScore).doubleValue());
            } else if (heatScore instanceof Number) {
                response.setHeatScore(((Number) heatScore).doubleValue());
            } else {
                response.setHeatScore(0.0);
            }
            
            hotAuthors.add(response);
        }

        return hotAuthors;
    }
}
