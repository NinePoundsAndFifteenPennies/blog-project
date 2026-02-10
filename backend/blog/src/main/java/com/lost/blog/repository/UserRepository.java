package com.lost.blog.repository; // 确保包名正确

import com.lost.blog.model.Role;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // ======================= 管理员用户查询方法 =======================

    /**
     * 多条件搜索用户（支持用户名、邮箱、角色、状态、注册时间范围）
     */
    @Query(value = """
        SELECT u.* FROM users u
        WHERE (:username IS NULL OR :username = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:email IS NULL OR :email = '' 
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:role IS NULL OR :role = '' OR u.role = :role)
          AND (:enabled IS NULL OR u.enabled = :enabled)
          AND (:startDate IS NULL OR u.created_at >= :startDate)
          AND (:endDate IS NULL OR u.created_at <= :endDate)
        ORDER BY u.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM users u
        WHERE (:username IS NULL OR :username = '' 
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:email IS NULL OR :email = '' 
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:role IS NULL OR :role = '' OR u.role = :role)
          AND (:enabled IS NULL OR u.enabled = :enabled)
          AND (:startDate IS NULL OR u.created_at >= :startDate)
          AND (:endDate IS NULL OR u.created_at <= :endDate)
        """,
        nativeQuery = true)
    Page<User> searchUsers(
            @Param("username") String username,
            @Param("email") String email,
            @Param("role") String role,
            @Param("enabled") Boolean enabled,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 根据角色统计用户数
    long countByRole(Role role);

    // 根据启用状态统计用户数
    long countByEnabled(boolean enabled);

    // ======================= 仪表盘统计方法 =======================

    // 统计指定时间范围内注册的用户数
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
