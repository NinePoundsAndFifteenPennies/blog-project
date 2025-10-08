package com.lost.blog.repository;

import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // 获取用户的草稿
    Page<Post> findByUserAndDraftTrue(User user, Pageable pageable);
}
