package com.lost.blog.repository;

import com.lost.blog.model.Post;
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
    Page<Post> findByDraftFalse(Pageable pageable);

    // 获取用户的所有文章（包括草稿）
    Page<Post> findByUser(User user, Pageable pageable);

    // 获取用户的已发布文章（不包括草稿）
    Page<Post> findByUserAndDraftFalse(User user, Pageable pageable);

    // 获取用户的草稿
    Page<Post> findByUserAndDraftTrue(User user, Pageable pageable);

    // 统计使用某个分类的文章数量
    long countByCategory(com.lost.blog.model.Category category);

    // 查找使用某个分类的所有文章
    java.util.List<Post> findByCategory(com.lost.blog.model.Category category);

    // 按创建时间排序获取已发布文章（升序）
    @Query("SELECT p FROM Post p WHERE p.draft = false ORDER BY p.createdAt ASC")
    Page<Post> findByDraftFalseOrderByCreatedAtAsc(Pageable pageable);

    // 按创建时间排序获取已发布文章（降序）
    @Query("SELECT p FROM Post p WHERE p.draft = false ORDER BY p.createdAt DESC")
    Page<Post> findByDraftFalseOrderByCreatedAtDesc(Pageable pageable);

    // 按创建时间排序获取已发布文章（动态方向）
    default Page<Post> findByDraftFalseOrderByCreatedAt(boolean ascending, Pageable pageable) {
        return ascending ? findByDraftFalseOrderByCreatedAtAsc(pageable) : findByDraftFalseOrderByCreatedAtDesc(pageable);
    }

    // 按热度排序获取已发布文章（升序）
    // 热度公式: (viewCount * 0.1 + likeCount * 5 + commentCount * 10) / POW(hours + 2, 1.2)
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.is_draft = false
        ORDER BY (p.view_count * 0.1 + COALESCE(l.like_count, 0) * 5 + COALESCE(c.comment_count, 0) * 10) / 
                 POW(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.2) ASC
        """, 
        countQuery = "SELECT COUNT(*) FROM posts WHERE is_draft = false",
        nativeQuery = true)
    Page<Post> findByDraftFalseOrderByHotnessAsc(Pageable pageable);

    // 按热度排序获取已发布文章（降序）
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN (SELECT post_id, COUNT(*) as like_count FROM likes WHERE post_id IS NOT NULL GROUP BY post_id) l ON p.id = l.post_id
        LEFT JOIN (SELECT post_id, COUNT(*) as comment_count FROM comments GROUP BY post_id) c ON p.id = c.post_id
        WHERE p.is_draft = false
        ORDER BY (p.view_count * 0.1 + COALESCE(l.like_count, 0) * 5 + COALESCE(c.comment_count, 0) * 10) / 
                 POW(TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2, 1.2) DESC
        """, 
        countQuery = "SELECT COUNT(*) FROM posts WHERE is_draft = false",
        nativeQuery = true)
    Page<Post> findByDraftFalseOrderByHotnessDesc(Pageable pageable);

    // 按热度排序获取已发布文章（动态方向）
    default Page<Post> findByDraftFalseOrderByHotness(boolean ascending, Pageable pageable) {
        return ascending ? findByDraftFalseOrderByHotnessAsc(pageable) : findByDraftFalseOrderByHotnessDesc(pageable);
    }
}
