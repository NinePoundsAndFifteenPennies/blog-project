package com.lost.blog.repository;

import com.lost.blog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
