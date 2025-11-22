package com.lost.blog.repository;

import com.lost.blog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
