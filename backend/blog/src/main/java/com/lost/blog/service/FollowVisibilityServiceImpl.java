package com.lost.blog.service;

import com.lost.blog.dto.FollowVisibilityRequest;
import com.lost.blog.dto.FollowVisibilityResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.FollowVisibility;
import com.lost.blog.model.User;
import com.lost.blog.repository.FollowRepository;
import com.lost.blog.repository.FollowVisibilityRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 关注可见性服务实现类
 * 
 * 可见性规则（按优先级）：
 * 1. 如果viewer是profileOwner本人 -> 允许访问
 * 2. 如果viewerNickname在blockedNicknames中 -> 拒绝访问（最高优先级）
 * 3. 如果isPublic为true -> 允许访问
 * 4. 如果viewerNickname在allowedNicknames中 -> 允许访问
 * 5. 如果visibleToFriends为true且viewer是朋友 -> 允许访问
 * 6. 如果visibleToFollowing为true且profileOwner关注了viewer -> 允许访问
 * 7. 否则 -> 拒绝访问
 */
@Service
public class FollowVisibilityServiceImpl implements FollowVisibilityService {

    private static final Logger logger = LoggerFactory.getLogger(FollowVisibilityServiceImpl.class);

    private final FollowVisibilityRepository visibilityRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Autowired
    public FollowVisibilityServiceImpl(FollowVisibilityRepository visibilityRepository,
                                       UserRepository userRepository,
                                       FollowRepository followRepository) {
        this.visibilityRepository = visibilityRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FollowVisibilityResponse getVisibilitySettings(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        FollowVisibility visibility = visibilityRepository.findByUser(user)
                .orElse(createDefaultVisibility(user));

        return toResponse(visibility);
    }

    @Override
    @Transactional
    public FollowVisibilityResponse updateVisibilitySettings(FollowVisibilityRequest request, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        FollowVisibility visibility = visibilityRepository.findByUser(user)
                .orElseGet(() -> {
                    FollowVisibility newVisibility = new FollowVisibility();
                    newVisibility.setUser(user);
                    return newVisibility;
                });

        // 更新各个字段（只更新非null的字段）
        if (request.getIsPublic() != null) {
            visibility.setPublic(request.getIsPublic());
        }
        if (request.getVisibleToFriends() != null) {
            visibility.setVisibleToFriends(request.getVisibleToFriends());
        }
        if (request.getVisibleToFollowing() != null) {
            visibility.setVisibleToFollowing(request.getVisibleToFollowing());
        }
        if (request.getAllowedNicknames() != null) {
            visibility.setAllowedNicknameSet(request.getAllowedNicknames());
        }
        if (request.getBlockedNicknames() != null) {
            visibility.setBlockedNicknameSet(request.getBlockedNicknames());
        }

        visibilityRepository.save(visibility);
        logger.info("用户 {} 更新了关注可见性设置", user.getUsername());

        return toResponse(visibility);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canViewFollowInfo(Long profileOwnerId, UserDetails viewer) {
        User profileOwner = userRepository.findById(profileOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + profileOwnerId + " 的用户"));

        // 获取可见性设置，如果不存在则使用默认设置（公开）
        FollowVisibility visibility = visibilityRepository.findByUser(profileOwner)
                .orElse(null);

        // 如果没有设置可见性，默认公开
        if (visibility == null) {
            return true;
        }

        // 规则1：如果viewer是profileOwner本人 -> 允许访问
        // 先通过username比较避免不必要的数据库查询
        if (viewer != null && viewer.getUsername().equals(profileOwner.getUsername())) {
            return true;
        }

        if (viewer != null) {
            User viewerUser = userRepository.findByUsername(viewer.getUsername()).orElse(null);
            
            String viewerNickname = viewerUser != null ? viewerUser.getNickname() : null;

            // 规则2：如果viewerNickname在blockedNicknames中 -> 拒绝访问（最高优先级）
            if (viewerNickname != null && !viewerNickname.isEmpty()) {
                Set<String> blockedNicknames = visibility.getBlockedNicknameSet();
                if (blockedNicknames.contains(viewerNickname)) {
                    logger.debug("用户 {} 被禁止查看用户 {} 的关注信息", viewerNickname, profileOwner.getUsername());
                    return false;
                }
            }

            // 规则3：如果isPublic为true -> 允许访问
            if (visibility.isPublic()) {
                return true;
            }

            // 规则4：如果viewerNickname在allowedNicknames中 -> 允许访问
            if (viewerNickname != null && !viewerNickname.isEmpty()) {
                Set<String> allowedNicknames = visibility.getAllowedNicknameSet();
                if (allowedNicknames.contains(viewerNickname)) {
                    return true;
                }
            }

            // 规则5：如果visibleToFriends为true且viewer是朋友 -> 允许访问
            if (visibility.isVisibleToFriends() && viewerUser != null) {
                boolean isFriend = followRepository.existsByFollowerAndFollowed(profileOwner, viewerUser)
                        && followRepository.existsByFollowerAndFollowed(viewerUser, profileOwner);
                if (isFriend) {
                    return true;
                }
            }

            // 规则6：如果visibleToFollowing为true且profileOwner关注了viewer -> 允许访问
            if (visibility.isVisibleToFollowing() && viewerUser != null) {
                boolean isFollowing = followRepository.existsByFollowerAndFollowed(profileOwner, viewerUser);
                if (isFollowing) {
                    return true;
                }
            }

            // 规则7：否则 -> 拒绝访问
            return false;
        } else {
            // 未登录用户
            // 规则2不适用（没有nickname）
            // 规则3：如果isPublic为true -> 允许访问
            return visibility.isPublic();
        }
    }

    /**
     * 创建默认的可见性设置（公开）
     */
    private FollowVisibility createDefaultVisibility(User user) {
        FollowVisibility visibility = new FollowVisibility();
        visibility.setUser(user);
        visibility.setPublic(true);
        visibility.setVisibleToFriends(false);
        visibility.setVisibleToFollowing(false);
        return visibility;
    }

    /**
     * 将实体转换为响应DTO
     */
    private FollowVisibilityResponse toResponse(FollowVisibility visibility) {
        return new FollowVisibilityResponse(
                visibility.isPublic(),
                visibility.isVisibleToFriends(),
                visibility.isVisibleToFollowing(),
                visibility.getAllowedNicknameSet(),
                visibility.getBlockedNicknameSet()
        );
    }
}
