package com.lost.blog.repository;

import com.lost.blog.model.Follow;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关注关系数据访问接口
 * 
 * 设计说明：
 * - 提供基础的CRUD操作和自定义查询
 * - 支持分页查询，适用于关注/粉丝列表展示
 * - 预留批量查询接口，便于后台系统生成关系图
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * 检查是否存在关注关系
     * @param follower 关注者
     * @param followed 被关注者
     * @return 是否存在关注关系
     */
    boolean existsByFollowerAndFollowed(User follower, User followed);

    /**
     * 查找特定的关注关系
     * @param follower 关注者
     * @param followed 被关注者
     * @return 关注关系
     */
    Optional<Follow> findByFollowerAndFollowed(User follower, User followed);

    /**
     * 删除关注关系
     * @param follower 关注者
     * @param followed 被关注者
     */
    void deleteByFollowerAndFollowed(User follower, User followed);

    /**
     * 统计用户的关注数（该用户关注了多少人）
     * @param follower 关注者
     * @return 关注数量
     */
    long countByFollower(User follower);

    /**
     * 统计用户的粉丝数（有多少人关注该用户）
     * @param followed 被关注者
     * @return 粉丝数量
     */
    long countByFollowed(User followed);

    /**
     * 获取用户的关注列表（该用户关注了谁）- 分页
     * @param follower 关注者
     * @param pageable 分页参数
     * @return 关注列表
     */
    Page<Follow> findByFollower(User follower, Pageable pageable);

    /**
     * 获取用户的粉丝列表（谁关注了该用户）- 分页
     * @param followed 被关注者
     * @param pageable 分页参数
     * @return 粉丝列表
     */
    Page<Follow> findByFollowed(User followed, Pageable pageable);

    /**
     * 获取用户的所有关注关系（用于后台关系图生成）
     * @param follower 关注者
     * @return 所有关注关系
     */
    List<Follow> findByFollower(User follower);

    /**
     * 获取用户的所有粉丝关系（用于后台关系图生成）
     * @param followed 被关注者
     * @return 所有粉丝关系
     */
    List<Follow> findByFollowed(User followed);

    /**
     * 查找互相关注的用户（朋友关系）
     * 使用JPQL子查询，检查是否存在反向关注关系
     * @param user 用户
     * @param pageable 分页参数
     * @return 朋友列表（互相关注的用户）
     */
    @Query("SELECT f FROM Follow f WHERE f.follower = :user " +
           "AND EXISTS (SELECT f2 FROM Follow f2 WHERE f2.follower = f.followed AND f2.followed = :user)")
    Page<Follow> findMutualFollows(@Param("user") User user, Pageable pageable);

    /**
     * 统计互相关注的用户数量（朋友数量）
     * @param user 用户
     * @return 朋友数量
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :user " +
           "AND EXISTS (SELECT f2 FROM Follow f2 WHERE f2.follower = f.followed AND f2.followed = :user)")
    long countMutualFollows(@Param("user") User user);

    /**
     * 获取所有关注关系（用于后台系统生成完整关系图）
     * 注意：在用户量大时应使用分页
     * @param pageable 分页参数
     * @return 所有关注关系
     */
    @Query("SELECT f FROM Follow f ORDER BY f.createdAt DESC")
    Page<Follow> findAllFollowRelations(Pageable pageable);
}
