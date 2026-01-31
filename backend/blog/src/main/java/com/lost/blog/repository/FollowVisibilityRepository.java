package com.lost.blog.repository;

import com.lost.blog.model.FollowVisibility;
import com.lost.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 关注可见性设置数据访问接口
 */
@Repository
public interface FollowVisibilityRepository extends JpaRepository<FollowVisibility, Long> {

    /**
     * 根据用户查找可见性设置
     * @param user 用户
     * @return 可见性设置
     */
    Optional<FollowVisibility> findByUser(User user);

    /**
     * 根据用户ID查找可见性设置
     * @param userId 用户ID
     * @return 可见性设置
     */
    Optional<FollowVisibility> findByUserId(Long userId);

    /**
     * 检查用户是否有可见性设置
     * @param user 用户
     * @return 是否存在
     */
    boolean existsByUser(User user);
}
