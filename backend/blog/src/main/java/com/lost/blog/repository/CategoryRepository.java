package com.lost.blog.repository;

import com.lost.blog.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category数据访问接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * 根据分类名称查找分类
     */
    Optional<Category> findByName(String name);
    
    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 查询使用次数最多的分类（按文章数排序）
     */
    @Query("SELECT c, COUNT(p) as postCount FROM Category c LEFT JOIN Post p ON p.category = c GROUP BY c.id ORDER BY postCount DESC")
    List<Category> findPopularCategories();

    /**
     * 管理员搜索分类（按文章数量降序排序）
     */
    @Query("SELECT c, COUNT(p) as postCount FROM Category c LEFT JOIN Post p ON p.category = c " +
           "WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:createdBy IS NULL OR LOWER(c.createdBy.username) LIKE LOWER(CONCAT('%', :createdBy, '%'))) " +
           "AND (:startDate IS NULL OR c.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR c.createdAt <= :endDate) " +
           "GROUP BY c.id " +
           "ORDER BY COUNT(p) DESC, c.createdAt DESC")
    Page<Object[]> adminSearchCategories(@Param("name") String name,
                                         @Param("createdBy") String createdBy,
                                         @Param("startDate") java.time.LocalDateTime startDate,
                                         @Param("endDate") java.time.LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * 查找属于指定分类的文章ID和标题列表
     */
    @Query("SELECT p.id, p.title FROM Post p WHERE p.category.id = :categoryId")
    List<Object[]> findPostsByCategoryId(@Param("categoryId") Long categoryId);
}
