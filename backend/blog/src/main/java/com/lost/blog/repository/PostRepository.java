package com.lost.blog.repository;

import com.lost.blog.model.Post;
import com.lost.blog.model.PostStatus;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // JpaRepository 已经提供了所有基础的CRUD方法
    // 后续我们可以根据需要在这里添加自定义的查询方法，
    // 例如：findAllByUser(User user) 来查找某个用户的所有文章

    // 检查标题是否存在（同一用户下）
    boolean existsByTitleAndUser(String title, User user);

    // 检查标题是否存在，但排除指定文章（用于更新时检查）
    boolean existsByTitleAndUserAndIdNot(String title, User user, Long id);

    // 获取所有已发布的文章（分页）
    Page<Post> findByStatusIn(java.util.Collection<PostStatus> statuses, Pageable pageable);

    // 获取用户的所有文章（包括草稿）
    Page<Post> findByUser(User user, Pageable pageable);

    // 获取用户的已发布文章（不包括草稿）
    Page<Post> findByUserAndStatusIn(User user, java.util.Collection<PostStatus> statuses, Pageable pageable);

    // 获取用户的草稿
    Page<Post> findByUserAndDraftTrue(User user, Pageable pageable);

    // 统计使用某个分类的文章数量
    long countByCategory(com.lost.blog.model.Category category);

    // 统计已发布文章总数（用于社区统计）
    long countByStatusIn(java.util.Collection<PostStatus> statuses);

    // 统计用户的文章数量
    long countByUser(User user);

    // 查找使用某个分类的所有文章
    java.util.List<Post> findByCategory(com.lost.blog.model.Category category);

    // 按标题模糊搜索文章（返回ID和标题，用于分类管理时选择文章）
    @Query("SELECT p.id, p.title FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY p.createdAt DESC")
    java.util.List<Object[]> searchPostsByTitle(@Param("title") String title, Pageable pageable);

    // 按创建时间排序获取已发布文章（升序）
    @Query("SELECT p FROM Post p WHERE p.status IN :statuses ORDER BY p.createdAt ASC")
    Page<Post> findByStatusInOrderByCreatedAtAsc(@Param("statuses") java.util.Collection<PostStatus> statuses,
                                                 Pageable pageable);

    // 按创建时间排序获取已发布文章（降序）
    @Query("SELECT p FROM Post p WHERE p.status IN :statuses ORDER BY p.createdAt DESC")
    Page<Post> findByStatusInOrderByCreatedAtDesc(@Param("statuses") java.util.Collection<PostStatus> statuses,
                                                  Pageable pageable);

    // 按创建时间排序获取已发布文章（动态方向）
    default Page<Post> findByStatusInOrderByCreatedAt(boolean ascending,
                                                      java.util.Collection<PostStatus> statuses,
                                                      Pageable pageable) {
        return ascending ? findByStatusInOrderByCreatedAtAsc(statuses, pageable)
            : findByStatusInOrderByCreatedAtDesc(statuses, pageable);
    }

    // 按热度排序获取已发布文章（升序）
    // 热度公式: (viewCount * 0.1 + likeCount * 5 + commentCount * 10) / POW(hours + 2, 1.2)
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.is_draft = false
          AND p.status = 'PUBLISHED'
        ORDER BY (p.view_count * 0.1 + COALESCE(l.like_count, 0) * 5 + COALESCE(c.comment_count, 0) * 10) / 
                 POW(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.2) ASC
        """, 
        countQuery = "SELECT COUNT(*) FROM posts WHERE is_draft = false AND status = 'PUBLISHED'",
        nativeQuery = true)
    Page<Post> findByDraftFalseOrderByHotnessAsc(Pageable pageable);

    // 按热度排序获取已发布文章（降序）
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.is_draft = false
          AND p.status = 'PUBLISHED'
        ORDER BY (p.view_count * 0.1 + COALESCE(l.like_count, 0) * 5 + COALESCE(c.comment_count, 0) * 10) / 
                 POW(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.2) DESC
        """, 
        countQuery = "SELECT COUNT(*) FROM posts WHERE is_draft = false AND status = 'PUBLISHED'",
        nativeQuery = true)
    Page<Post> findByDraftFalseOrderByHotnessDesc(Pageable pageable);

    // 按热度排序获取已发布文章（动态方向）
    default Page<Post> findByDraftFalseOrderByHotness(boolean ascending, Pageable pageable) {
        return ascending ? findByDraftFalseOrderByHotnessAsc(pageable) : findByDraftFalseOrderByHotnessDesc(pageable);
    }

    // ======================= 搜索相关方法 =======================

    /**
     * 综合搜索已发布文章（按创建时间降序）
     * 支持按关键词（标题+内容+作者昵称+标签名）、作者、标签进行搜索
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE p.is_draft = false
          AND p.status IN ('PUBLISHED', 'PENDING_REVISION')
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        ORDER BY p.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE p.is_draft = false
          AND p.status IN ('PUBLISHED', 'PENDING_REVISION')
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        """,
        nativeQuery = true)
    Page<Post> searchPostsByTimeDesc(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("title") String title,
            @Param("tag") String tag,
            Pageable pageable);

    /**
     * 综合搜索已发布文章（按创建时间升序）
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE p.is_draft = false
          AND p.status IN ('PUBLISHED', 'PENDING_REVISION')
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        ORDER BY p.created_at ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE p.is_draft = false
          AND p.status IN ('PUBLISHED', 'PENDING_REVISION')
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        """,
        nativeQuery = true)
    Page<Post> searchPostsByTimeAsc(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("title") String title,
            @Param("tag") String tag,
            Pageable pageable);

    /**
     * 综合搜索已发布文章（按热度降序）
     * 使用子查询先过滤文章ID，然后再JOIN获取完整数据并排序
     */
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.id IN (
            SELECT DISTINCT p2.id FROM posts p2
            LEFT JOIN users u ON p2.user_id = u.id
            LEFT JOIN post_tags pt ON p2.id = pt.post_id
            LEFT JOIN tags t ON pt.tag_id = t.id
            WHERE p2.is_draft = false
              AND p2.status = 'PUBLISHED'
              AND (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(p2.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p2.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:author IS NULL OR :author = '' 
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
              AND (:title IS NULL OR :title = '' 
                   OR LOWER(p2.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:tag IS NULL OR :tag = '' 
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        )
        ORDER BY (p.view_count * 0.1 + COALESCE(l.like_count, 0) * 5 + COALESCE(c.comment_count, 0) * 10) / 
                 POW(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.2) DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE p.is_draft = false
          AND p.status = 'PUBLISHED'
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        """,
        nativeQuery = true)
    Page<Post> searchPostsByHotnessDesc(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("title") String title,
            @Param("tag") String tag,
            Pageable pageable);

    /**
     * 综合搜索已发布文章（按热度升序）
     * 使用子查询先过滤文章ID，然后再JOIN获取完整数据并排序
     */
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.id IN (
            SELECT DISTINCT p2.id FROM posts p2
            LEFT JOIN users u ON p2.user_id = u.id
            LEFT JOIN post_tags pt ON p2.id = pt.post_id
            LEFT JOIN tags t ON pt.tag_id = t.id
            WHERE p2.is_draft = false
              AND p2.status = 'PUBLISHED'
              AND (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(p2.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p2.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:author IS NULL OR :author = '' 
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
              AND (:title IS NULL OR :title = '' 
                   OR LOWER(p2.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:tag IS NULL OR :tag = '' 
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        )
        ORDER BY (p.view_count * 0.1 + COALESCE(l.like_count, 0) * 5 + COALESCE(c.comment_count, 0) * 10) / 
                 POW(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.2) ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE p.is_draft = false
          AND p.status = 'PUBLISHED'
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
        """,
        nativeQuery = true)
    Page<Post> searchPostsByHotnessAsc(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("title") String title,
            @Param("tag") String tag,
            Pageable pageable);

    // ======================= 管理员文章搜索方法 =======================

    /**
     * 管理员搜索文章（支持按状态、作者、标题、分类、标签、时间范围过滤）
     * 包含所有文章（草稿、待审核、已发布、已拒绝、修改待审核）
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN categories c ON p.category_id = c.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:status IS NULL OR :status = '' OR p.status = :status)
          AND (:categoryId IS NULL OR p.category_id = :categoryId)
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
          AND (:startDate IS NULL OR p.created_at >= :startDate)
          AND (:endDate IS NULL OR p.created_at <= :endDate)
        ORDER BY p.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN categories c ON p.category_id = c.id
        LEFT JOIN post_tags pt ON p.id = pt.post_id
        LEFT JOIN tags t ON pt.tag_id = t.id
        WHERE (:title IS NULL OR :title = '' 
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:author IS NULL OR :author = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :author, '%')))
          AND (:status IS NULL OR :status = '' OR p.status = :status)
          AND (:categoryId IS NULL OR p.category_id = :categoryId)
          AND (:tag IS NULL OR :tag = '' 
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tag, '%')))
          AND (:startDate IS NULL OR p.created_at >= :startDate)
          AND (:endDate IS NULL OR p.created_at <= :endDate)
        """,
        nativeQuery = true)
    Page<Post> adminSearchPosts(
            @Param("title") String title,
            @Param("author") String author,
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("tag") String tag,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Pageable pageable);

    /**
     * 根据状态查找文章
     */
    Page<Post> findByStatusOrderByCreatedAtDesc(com.lost.blog.model.PostStatus status, Pageable pageable);

    /**
     * 统计各状态文章数量
     */
    long countByStatus(com.lost.blog.model.PostStatus status);

    // ======================= 仪表盘统计方法 =======================

    // 统计指定时间范围内创建的文章数
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    // 统计总浏览量（所有文章viewCount之和）
    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM Post p")
    long sumAllViewCount();

    // 查询热门文章TOP N（按viewCount降序，只含已发布文章）
    @Query(value = """
        SELECT p.id, p.title, u.nickname, p.view_count,
               COALESCE(l.like_count, 0) as like_count,
               COALESCE(c.comment_count, 0) as comment_count
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.status = 'PUBLISHED' AND p.is_draft = false
        ORDER BY p.view_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    java.util.List<Object[]> findTopHotPosts(@Param("limit") int limit);
}
