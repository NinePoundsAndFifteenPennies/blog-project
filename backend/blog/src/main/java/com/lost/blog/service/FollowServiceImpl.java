package com.lost.blog.service;

import com.lost.blog.dto.FollowResponse;
import com.lost.blog.dto.FollowStatsResponse;
import com.lost.blog.dto.FollowUserResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.FollowMapper;
import com.lost.blog.model.Follow;
import com.lost.blog.model.User;
import com.lost.blog.repository.FollowRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关注服务实现类
 * 
 * 设计说明：
 * - 使用事务确保数据一致性
 * - 自动检测并标记朋友关系（双向关注）
 * - 防止自己关注自己
 * - 提供丰富的日志记录便于排查问题
 */
@Service
public class FollowServiceImpl implements FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Autowired
    public FollowServiceImpl(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FollowResponse followUser(Long userId, UserDetails currentUser) {
        User follower = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        User followed = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        // 防止自己关注自己
        if (follower.getId().equals(followed.getId())) {
            logger.warn("用户 {} 尝试关注自己", follower.getUsername());
            return new FollowResponse(false, false, "不能关注自己");
        }

        // 检查是否已经关注
        if (followRepository.existsByFollowerAndFollowed(follower, followed)) {
            boolean isFriend = followRepository.existsByFollowerAndFollowed(followed, follower);
            logger.info("用户 {} 已经关注了用户 {}", follower.getUsername(), followed.getUsername());
            return new FollowResponse(true, isFriend, "已经关注该用户");
        }

        // 创建关注关系
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        followRepository.save(follow);

        // 检查是否形成朋友关系（对方是否也关注了当前用户）
        boolean isFriend = followRepository.existsByFollowerAndFollowed(followed, follower);
        
        if (isFriend) {
            logger.info("用户 {} 关注了用户 {}，双方成为朋友", follower.getUsername(), followed.getUsername());
            return new FollowResponse(true, true, "关注成功，你们已成为朋友");
        } else {
            logger.info("用户 {} 关注了用户 {}", follower.getUsername(), followed.getUsername());
            return new FollowResponse(true, false, "关注成功");
        }
    }

    @Override
    @Transactional
    public FollowResponse unfollowUser(Long userId, UserDetails currentUser) {
        User follower = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        User followed = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        // 检查是否存在关注关系
        if (!followRepository.existsByFollowerAndFollowed(follower, followed)) {
            logger.info("用户 {} 未关注用户 {}，无需取消", follower.getUsername(), followed.getUsername());
            return new FollowResponse(false, false, "未关注该用户");
        }

        // 删除关注关系
        followRepository.deleteByFollowerAndFollowed(follower, followed);

        logger.info("用户 {} 取消关注了用户 {}", follower.getUsername(), followed.getUsername());
        return new FollowResponse(false, false, "取消关注成功");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, UserDetails currentUser) {
        if (currentUser == null) {
            return false;
        }

        User follower = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        if (follower == null) {
            return false;
        }

        User followed = userRepository.findById(userId).orElse(null);
        if (followed == null) {
            return false;
        }

        return followRepository.existsByFollowerAndFollowed(follower, followed);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFriend(Long userId, UserDetails currentUser) {
        if (currentUser == null) {
            return false;
        }

        User user1 = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        if (user1 == null) {
            return false;
        }

        User user2 = userRepository.findById(userId).orElse(null);
        if (user2 == null) {
            return false;
        }

        // 朋友关系需要双向关注
        return followRepository.existsByFollowerAndFollowed(user1, user2) 
            && followRepository.existsByFollowerAndFollowed(user2, user1);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatsResponse getFollowStats(Long userId, UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        long followingCount = followRepository.countByFollower(user);
        long followerCount = followRepository.countByFollowed(user);
        long friendCount = followRepository.countMutualFollows(user);

        // 如果当前用户已登录，检查与目标用户的关系
        Boolean isFollowing = null;
        Boolean isFriend = null;
        
        if (currentUser != null) {
            User currentUserEntity = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            if (currentUserEntity != null && !currentUserEntity.getId().equals(userId)) {
                isFollowing = followRepository.existsByFollowerAndFollowed(currentUserEntity, user);
                isFriend = isFollowing && followRepository.existsByFollowerAndFollowed(user, currentUserEntity);
            }
        }

        return new FollowStatsResponse(followingCount, followerCount, friendCount, isFollowing, isFriend);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFollowingList(Long userId, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        Page<Follow> follows = followRepository.findByFollower(user, pageable);

        return follows.map(follow -> {
            // 检查是否为朋友关系
            boolean isFriend = followRepository.existsByFollowerAndFollowed(follow.getFollowed(), user);
            return FollowMapper.toFollowingUserResponse(follow, isFriend);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFollowerList(Long userId, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        Page<Follow> follows = followRepository.findByFollowed(user, pageable);

        return follows.map(follow -> {
            // 检查是否为朋友关系
            boolean isFriend = followRepository.existsByFollowerAndFollowed(user, follow.getFollower());
            return FollowMapper.toFollowerUserResponse(follow, isFriend);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFriendList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        Page<Follow> mutualFollows = followRepository.findMutualFollows(user, pageable);

        return mutualFollows.map(follow -> FollowMapper.toFollowingUserResponse(follow, true));
    }
}
