package com.lost.blog.repository;

import com.lost.blog.model.Comment;
import com.lost.blog.model.Post;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // 统计文章的评论数
    long countByPost(Post post);

    // 删除文章的所有评论（级联删除时使用）
    void deleteByPost(Post post);

}
