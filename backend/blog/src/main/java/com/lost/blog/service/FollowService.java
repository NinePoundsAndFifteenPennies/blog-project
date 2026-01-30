package com.lost.blog.service;

import com.lost.blog.dto.FollowResponse;
import com.lost.blog.dto.FollowStatsResponse;
import com.lost.blog.dto.FollowUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 关注服务接口
 * 提供关注功能的核心业务逻辑
 */
public interface FollowService {

    /**
     * 关注用户
     * @param userId 要关注的用户ID
     * @param currentUser 当前登录用户
     * @return 关注操作结果
     */
    FollowResponse followUser(Long userId, UserDetails currentUser);

    /**
     * 取消关注
     * @param userId 要取消关注的用户ID
     * @param currentUser 当前登录用户
     * @return 取消关注操作结果
     */
    FollowResponse unfollowUser(Long userId, UserDetails currentUser);

    /**
     * 检查是否已关注某用户
     * @param userId 目标用户ID
     * @param currentUser 当前登录用户（可为null）
     * @return 是否已关注
     */
    boolean isFollowing(Long userId, UserDetails currentUser);

    /**
     * 检查两个用户是否为朋友（互相关注）
     * @param userId 目标用户ID
     * @param currentUser 当前登录用户（可为null）
     * @return 是否为朋友
     */
    boolean isFriend(Long userId, UserDetails currentUser);

    /**
     * 获取用户的关注统计信息
     * @param userId 用户ID
     * @param currentUser 当前登录用户（可为null，用于判断关注关系）
     * @return 关注统计信息
     */
    FollowStatsResponse getFollowStats(Long userId, UserDetails currentUser);

    /**
     * 获取用户的关注列表（该用户关注了谁）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @param currentUser 当前登录用户（可为null，用于判断朋友关系）
     * @return 关注列表
     */
    Page<FollowUserResponse> getFollowingList(Long userId, Pageable pageable, UserDetails currentUser);

    /**
     * 获取用户的粉丝列表（谁关注了该用户）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @param currentUser 当前登录用户（可为null，用于判断朋友关系）
     * @return 粉丝列表
     */
    Page<FollowUserResponse> getFollowerList(Long userId, Pageable pageable, UserDetails currentUser);

    /**
     * 获取用户的朋友列表（互相关注的用户）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 朋友列表
     */
    Page<FollowUserResponse> getFriendList(Long userId, Pageable pageable);
}
