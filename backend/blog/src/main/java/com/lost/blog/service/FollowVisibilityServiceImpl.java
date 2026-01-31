package com.lost.blog.service;

import com.lost.blog.dto.FollowVisibilityRequest;
import com.lost.blog.dto.FollowVisibilityResponse;
import com.lost.blog.dto.VisibilitySettingDto;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.FollowInfoType;
import com.lost.blog.model.FollowVisibility;
import com.lost.blog.model.User;
import com.lost.blog.model.VisibilitySetting;
import com.lost.blog.repository.FollowRepository;
import com.lost.blog.repository.FollowVisibilityRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 关注可见性服务实现类
 * 
 * 支持分别设置和检查四种类型的可见性：
 * - FOLLOWING: 关注列表
 * - FOLLOWERS: 粉丝列表
 * - FRIENDS: 朋友列表
 * - STATS: 统计数据
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

        // 更新各个类型的可见性设置（只更新非null的字段）
        if (request.getFollowing() != null) {
            updateSetting(visibility.getFollowingSetting(), request.getFollowing());
        }
        if (request.getFollowers() != null) {
            updateSetting(visibility.getFollowersSetting(), request.getFollowers());
        }
        if (request.getFriends() != null) {
            updateSetting(visibility.getFriendsSetting(), request.getFriends());
        }
        if (request.getStats() != null) {
            updateSetting(visibility.getStatsSetting(), request.getStats());
        }

        visibilityRepository.save(visibility);
        logger.info("用户 {} 更新了关注可见性设置", user.getUsername());

        return toResponse(visibility);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canViewFollowInfo(Long profileOwnerId, UserDetails viewer, FollowInfoType infoType) {
        User profileOwner = userRepository.findById(profileOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为: " + profileOwnerId + " 的用户"));

        // 获取可见性设置，如果不存在则使用默认设置（公开）
        FollowVisibility visibility = visibilityRepository.findByUser(profileOwner)
                .orElse(null);

        // 如果没有设置可见性，默认公开
        if (visibility == null) {
            return true;
        }

        // 根据类型获取对应的可见性设置
        VisibilitySetting setting = getSettingByType(visibility, infoType);

        // 规则1：如果viewer是profileOwner本人 -> 允许访问
        if (viewer != null && viewer.getUsername().equals(profileOwner.getUsername())) {
            return true;
        }

        if (viewer != null) {
            User viewerUser = userRepository.findByUsername(viewer.getUsername()).orElse(null);
            String viewerNickname = viewerUser != null ? viewerUser.getNickname() : null;

            // 规则2：如果viewerNickname在blockedNicknames中 -> 拒绝访问（最高优先级）
            if (viewerNickname != null && !viewerNickname.isEmpty()) {
                Set<String> blockedNicknames = setting.getBlockedNicknameSet();
                if (blockedNicknames.contains(viewerNickname)) {
                    logger.debug("用户 {} 被禁止查看用户 {} 的{}信息", 
                            viewerNickname, profileOwner.getUsername(), infoType);
                    return false;
                }
            }

            // 规则3：如果isPublic为true -> 允许访问
            if (setting.isPublic()) {
                return true;
            }

            // 规则4：如果viewerNickname在allowedNicknames中 -> 允许访问
            if (viewerNickname != null && !viewerNickname.isEmpty()) {
                Set<String> allowedNicknames = setting.getAllowedNicknameSet();
                if (allowedNicknames.contains(viewerNickname)) {
                    return true;
                }
            }

            // 规则5：如果visibleToFriends为true且viewer是朋友 -> 允许访问
            if (setting.isVisibleToFriends() && viewerUser != null) {
                boolean isFriend = followRepository.existsByFollowerAndFollowed(profileOwner, viewerUser)
                        && followRepository.existsByFollowerAndFollowed(viewerUser, profileOwner);
                if (isFriend) {
                    return true;
                }
            }

            // 规则6：如果visibleToFollowing为true且profileOwner关注了viewer -> 允许访问
            if (setting.isVisibleToFollowing() && viewerUser != null) {
                boolean isFollowing = followRepository.existsByFollowerAndFollowed(profileOwner, viewerUser);
                if (isFollowing) {
                    return true;
                }
            }

            // 规则7：否则 -> 拒绝访问
            return false;
        } else {
            // 未登录用户：只有isPublic为true才允许访问
            return setting.isPublic();
        }
    }

    /**
     * 根据类型获取对应的可见性设置
     */
    private VisibilitySetting getSettingByType(FollowVisibility visibility, FollowInfoType infoType) {
        switch (infoType) {
            case FOLLOWING:
                return visibility.getFollowingSetting();
            case FOLLOWERS:
                return visibility.getFollowersSetting();
            case FRIENDS:
                return visibility.getFriendsSetting();
            case STATS:
                return visibility.getStatsSetting();
            default:
                return VisibilitySetting.createDefault();
        }
    }

    /**
     * 更新单个可见性设置
     */
    private void updateSetting(VisibilitySetting setting, VisibilitySettingDto dto) {
        if (dto.getIsPublic() != null) {
            setting.setPublic(dto.getIsPublic());
        }
        if (dto.getVisibleToFriends() != null) {
            setting.setVisibleToFriends(dto.getVisibleToFriends());
        }
        if (dto.getVisibleToFollowing() != null) {
            setting.setVisibleToFollowing(dto.getVisibleToFollowing());
        }
        if (dto.getAllowedNicknames() != null) {
            setting.setAllowedNicknameSet(dto.getAllowedNicknames());
        }
        if (dto.getBlockedNicknames() != null) {
            setting.setBlockedNicknameSet(dto.getBlockedNicknames());
        }
    }

    /**
     * 创建默认的可见性设置（全部公开）
     */
    private FollowVisibility createDefaultVisibility(User user) {
        FollowVisibility visibility = new FollowVisibility();
        visibility.setUser(user);
        visibility.setFollowingSetting(VisibilitySetting.createDefault());
        visibility.setFollowersSetting(VisibilitySetting.createDefault());
        visibility.setFriendsSetting(VisibilitySetting.createDefault());
        visibility.setStatsSetting(VisibilitySetting.createDefault());
        return visibility;
    }

    /**
     * 将实体转换为响应DTO
     */
    private FollowVisibilityResponse toResponse(FollowVisibility visibility) {
        return new FollowVisibilityResponse(
                toSettingDto(visibility.getFollowingSetting()),
                toSettingDto(visibility.getFollowersSetting()),
                toSettingDto(visibility.getFriendsSetting()),
                toSettingDto(visibility.getStatsSetting())
        );
    }

    /**
     * 将VisibilitySetting转换为DTO
     */
    private VisibilitySettingDto toSettingDto(VisibilitySetting setting) {
        return new VisibilitySettingDto(
                setting.isPublic(),
                setting.isVisibleToFriends(),
                setting.isVisibleToFollowing(),
                setting.getAllowedNicknameSet(),
                setting.getBlockedNicknameSet()
        );
    }
}
