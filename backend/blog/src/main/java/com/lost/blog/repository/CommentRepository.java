package com.lost.blog.repository;

import com.lost.blog.model.Comment;
import com.lost.blog.model.CommentStatus;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 获取文章的所有评论（分页）
    Page<Comment> findByPost(Post post, Pageable pageable);

    // 获取文章的所有顶层评论（分页）- parent_id为null
    Page<Comment> findByPostAndParentIsNull(Post post, Pageable pageable);

    // 获取评论的直接子评论（分页）
    Page<Comment> findByParent(Comment parent, Pageable pageable);

    // 获取评论的所有子评论（不分页）- 用于级联删除
    java.util.List<Comment> findByParent(Comment parent);

    // 统计评论的子评论数量
    long countByParent(Comment parent);

    // 获取用户的所有评论（分页）
    Page<Comment> findByUser(User user, Pageable pageable);

    // 统计用户的评论数量
    long countByUser(User user);

    // 统计文章的评论数
    long countByPost(Post post);

    // 删除文章的所有评论（级联删除时使用）
    void deleteByPost(Post post);

    // 按创建时间排序获取顶层评论（升序）
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findTopLevelCommentsByPostOrderByCreatedAtAsc(@Param("postId") Long postId, Pageable pageable);

    // 按创建时间排序获取顶层评论（降序）
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByPostOrderByCreatedAtDesc(@Param("postId") Long postId, Pageable pageable);

    // 按创建时间排序获取顶层评论（动态方向）
    default Page<Comment> findTopLevelCommentsByPostOrderByCreatedAt(Long postId, boolean ascending, Pageable pageable) {
        return ascending ? findTopLevelCommentsByPostOrderByCreatedAtAsc(postId, pageable) : findTopLevelCommentsByPostOrderByCreatedAtDesc(postId, pageable);
    }

    // 按热度排序获取顶层评论（升序）
    // 热度公式: (likeCount * 1 + replyCount * 3 + bonus) / POW(hours + 2, 1.5)
    // bonus = if(replyCount > 5, 10, 0)
    @Query(value = """
        SELECT c.* FROM comments c
        LEFT JOIN (SELECT comment_id, COUNT(*) as like_count FROM likes WHERE comment_id IS NOT NULL GROUP BY comment_id) l ON c.id = l.comment_id
        LEFT JOIN (SELECT parent_id, COUNT(*) as reply_count FROM comments WHERE parent_id IS NOT NULL GROUP BY parent_id) r ON c.id = r.parent_id
        WHERE c.post_id = :postId AND c.parent_id IS NULL
        ORDER BY (COALESCE(l.like_count, 0) * 1 + COALESCE(r.reply_count, 0) * 3 + IF(COALESCE(r.reply_count, 0) > 5, 10, 0)) / 
                 POW(TIMESTAMPDIFF(HOUR, c.created_at, NOW()) + 2, 1.5) ASC
        """, 
        countQuery = "SELECT COUNT(*) FROM comments WHERE post_id = :postId AND parent_id IS NULL",
        nativeQuery = true)
    Page<Comment> findTopLevelCommentsByPostOrderByHotnessAsc(@Param("postId") Long postId, Pageable pageable);

    // 按热度排序获取顶层评论（降序）
    @Query(value = """
        SELECT c.* FROM comments c
        LEFT JOIN (SELECT comment_id, COUNT(*) as like_count FROM likes WHERE comment_id IS NOT NULL GROUP BY comment_id) l ON c.id = l.comment_id
        LEFT JOIN (SELECT parent_id, COUNT(*) as reply_count FROM comments WHERE parent_id IS NOT NULL GROUP BY parent_id) r ON c.id = r.parent_id
        WHERE c.post_id = :postId AND c.parent_id IS NULL
        ORDER BY (COALESCE(l.like_count, 0) * 1 + COALESCE(r.reply_count, 0) * 3 + IF(COALESCE(r.reply_count, 0) > 5, 10, 0)) / 
                 POW(TIMESTAMPDIFF(HOUR, c.created_at, NOW()) + 2, 1.5) DESC
        """, 
        countQuery = "SELECT COUNT(*) FROM comments WHERE post_id = :postId AND parent_id IS NULL",
        nativeQuery = true)
    Page<Comment> findTopLevelCommentsByPostOrderByHotnessDesc(@Param("postId") Long postId, Pageable pageable);

    // 按热度排序获取顶层评论（动态方向）
    default Page<Comment> findTopLevelCommentsByPostOrderByHotness(Long postId, boolean ascending, Pageable pageable) {
        return ascending ? findTopLevelCommentsByPostOrderByHotnessAsc(postId, pageable) : findTopLevelCommentsByPostOrderByHotnessDesc(postId, pageable);
    }

    // ======================= 管理后台查询 =======================

    /**
     * 管理后台搜索评论（支持多条件过滤）
     * @param content 评论内容关键词（模糊匹配）
     * @param author 作者用户名或昵称（模糊匹配）
     * @param postTitle 文章标题（模糊匹配）
     * @param status 评论状态
     * @param startDateTime 创建开始时间
     * @param endDateTime 创建结束时间
     * @param includeReplies 是否包含子评论
     * @param pageable 分页参数
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE (:content IS NULL OR c.content LIKE %:content%) " +
           "AND (:author IS NULL OR c.user.username LIKE %:author% OR c.user.nickname LIKE %:author%) " +
           "AND (:postTitle IS NULL OR c.post.title LIKE %:postTitle%) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:startDateTime IS NULL OR c.createdAt >= :startDateTime) " +
           "AND (:endDateTime IS NULL OR c.createdAt <= :endDateTime) " +
           "AND (:includeReplies = true OR c.parent IS NULL) " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> adminSearchComments(
            @Param("content") String content,
            @Param("author") String author,
            @Param("postTitle") String postTitle,
            @Param("status") CommentStatus status,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("includeReplies") boolean includeReplies,
            Pageable pageable
    );

}
