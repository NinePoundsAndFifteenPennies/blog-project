package com.lost.blog.repository; // 确保包名正确

import com.lost.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository // 告诉Spring这是一个数据仓库Bean
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    // 统计最近活跃的用户数（用于在线用户统计）
    Long countByUpdatedAtAfter(LocalDateTime threshold);
    
    // 统计在线用户数（考虑updatedAt为null的情况）
    Long countByUpdatedAtAfterOrUpdatedAtIsNullAndCreatedAtAfter(LocalDateTime updatedThreshold, LocalDateTime createdThreshold);
}
