package com.lost.blog.repository;

import com.lost.blog.model.Post;
import com.lost.blog.model.PostViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PostViewLogRepository extends JpaRepository<PostViewLog, Long> {
    
    // JPA 会自动翻译成 SQL：查询该 Post 该 IP 在指定时间之后是否有记录
    boolean existsByPostAndIpAndCreateTimeAfter(Post post, String ip, LocalDateTime time);

    // 删除指定文章的所有浏览日志（用于删除文章时级联删除）
    void deleteByPost(Post post);
}
