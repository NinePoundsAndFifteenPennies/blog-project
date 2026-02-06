package com.lost.blog.repository;

import com.lost.blog.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tag数据访问接口
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根据标签名称查找标签
     */
    Optional<Tag> findByName(String name);
    
    /**
     * 检查标签名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据标签名称列表查找标签
     */
    List<Tag> findByNameIn(Set<String> names);
    
    /**
     * 查询使用次数最多的标签（按文章数排序）
     */
    @Query("SELECT t FROM Tag t LEFT JOIN t.posts p GROUP BY t.id ORDER BY COUNT(p) DESC")
    List<Tag> findPopularTags();
    
    /**
     * 根据ID查找标签，并加载关联的文章
     */
    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.posts WHERE t.id = :id")
    Optional<Tag> findByIdWithPosts(Long id);

    /**
     * 管理员搜索标签（按文章数量降序排序）
     */
    @Query("SELECT t, COUNT(p) as postCount FROM Tag t LEFT JOIN t.posts p " +
           "WHERE (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:createdBy IS NULL OR LOWER(t.createdBy.username) LIKE LOWER(CONCAT('%', :createdBy, '%'))) " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
           "GROUP BY t.id " +
           "ORDER BY COUNT(p) DESC, t.createdAt DESC")
    Page<Object[]> adminSearchTags(@Param("name") String name,
                                   @Param("createdBy") String createdBy,
                                   @Param("startDate") java.time.LocalDateTime startDate,
                                   @Param("endDate") java.time.LocalDateTime endDate,
                                   Pageable pageable);
}
