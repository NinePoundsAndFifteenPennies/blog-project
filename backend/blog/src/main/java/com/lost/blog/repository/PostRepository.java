package com.lost.blog.repository;

import com.lost.blog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // JpaRepository 已经提供了所有基础的CRUD方法
    // 后续我们可以根据需要在这里添加自定义的查询方法，
    // 例如：findAllByUser(User user) 来查找某个用户的所有文章
    boolean existsByTitle(String title); // 根据标题检查文章是否存在
}