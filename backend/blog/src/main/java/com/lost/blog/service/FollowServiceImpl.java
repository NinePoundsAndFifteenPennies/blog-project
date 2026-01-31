package com.lost.blog.service;

import com.lost.blog.dto.FollowResponse;
import com.lost.blog.dto.FollowStatsResponse;
import com.lost.blog.dto.FollowUserResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.FollowMapper;
import com.lost.blog.model.Follow;
import com.lost.blog.model.FollowInfoType;
import com.lost.blog.model.User;
import com.lost.blog.repository.FollowRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关注服务实现类
 * 
 * 设计说明：
 * - 使用事务确保数据一致性
 * - 自动检测并标记朋友关系（双向关注）
 * - 防止自己关注自己
 * - 使用批量查询优化N+1问题
 * - 集成可见性检查，保护用户隐私（支持分别控制四种类型的可见性）
 * - 提供丰富的日志记录便于排查问题
 */
@Service
public class FollowServiceImpl implements FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowVisibilityService visibilityService;
    private final NotificationService notificationService;

    @Autowired
    public FollowServiceImpl(FollowRepository followRepository, 
                             UserRepository userRepository,
                             FollowVisibilityService visibilityService,
                             NotificationService notificationService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.visibilityService = visibilityService;
        this.notificationService = notificationService;
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

        // 创建关注通知
        notificationService.createFollowedNotification(follower, followed);

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

        // 检查统计数据的可见性权限
        boolean canView = visibilityService.canViewFollowInfo(userId, currentUser, FollowInfoType.STATS);
        
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

        // 如果无权查看，返回隐藏的统计信息（关系信息仍然返回）
        if (!canView) {
            logger.debug("用户无权查看用户 {} 的关注统计信息", userId);
            return FollowStatsResponse.hidden(isFollowing, isFriend);
        }

        long followingCount = followRepository.countByFollower(user);
        long followerCount = followRepository.countByFollowed(user);
        long friendCount = followRepository.countMutualFollows(user);

        return new FollowStatsResponse(followingCount, followerCount, friendCount, isFollowing, isFriend);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFollowingList(Long userId, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        // 检查关注列表的可见性权限
        if (!visibilityService.canViewFollowInfo(userId, currentUser, FollowInfoType.FOLLOWING)) {
            logger.debug("用户无权查看用户 {} 的关注列表", userId);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Page<Follow> follows = followRepository.findByFollower(user, pageable);

        // 批量查询朋友关系，避免N+1问题
        Set<Long> followedIds = follows.getContent().stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());
        
        Set<Long> friendIds = followedIds.isEmpty() 
                ? Collections.emptySet()
                : followRepository.findFollowerIdsByFollowedAndFollowerIds(user, followedIds);

        return follows.map(follow -> {
            boolean isFriend = friendIds.contains(follow.getFollowed().getId());
            return FollowMapper.toFollowingUserResponse(follow, isFriend);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFollowerList(Long userId, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        // 检查粉丝列表的可见性权限
        if (!visibilityService.canViewFollowInfo(userId, currentUser, FollowInfoType.FOLLOWERS)) {
            logger.debug("用户无权查看用户 {} 的粉丝列表", userId);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Page<Follow> follows = followRepository.findByFollowed(user, pageable);

        // 批量查询朋友关系，避免N+1问题
        Set<Long> followerIds = follows.getContent().stream()
                .map(f -> f.getFollower().getId())
                .collect(Collectors.toSet());
        
        Set<Long> friendIds = followerIds.isEmpty()
                ? Collections.emptySet()
                : followRepository.findFollowedIdsByFollowerAndFollowedIds(user, followerIds);

        return follows.map(follow -> {
            boolean isFriend = friendIds.contains(follow.getFollower().getId());
            return FollowMapper.toFollowerUserResponse(follow, isFriend);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFriendList(Long userId, Pageable pageable, UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + userId + " 的用户"));

        // 检查朋友列表的可见性权限
        if (!visibilityService.canViewFollowInfo(userId, currentUser, FollowInfoType.FRIENDS)) {
            logger.debug("用户无权查看用户 {} 的朋友列表", userId);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Page<Follow> mutualFollows = followRepository.findMutualFollows(user, pageable);

        return mutualFollows.map(follow -> FollowMapper.toFollowingUserResponse(follow, true));
    }
}
