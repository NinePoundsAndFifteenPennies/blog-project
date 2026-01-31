package com.lost.blog.service;

import com.lost.blog.dto.FollowVisibilityRequest;
import com.lost.blog.dto.FollowVisibilityResponse;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 关注可见性服务接口
 * 提供关注信息可见性设置的管理功能
 */
public interface FollowVisibilityService {

    /**
     * 获取当前用户的可见性设置
     * @param currentUser 当前登录用户
     * @return 可见性设置
     */
    FollowVisibilityResponse getVisibilitySettings(UserDetails currentUser);

    /**
     * 更新当前用户的可见性设置
     * @param request 可见性设置请求
     * @param currentUser 当前登录用户
     * @return 更新后的可见性设置
     */
    FollowVisibilityResponse updateVisibilitySettings(FollowVisibilityRequest request, UserDetails currentUser);

    /**
     * 检查viewer是否有权限查看profileOwner的关注信息
     * @param profileOwnerId 资料所有者的用户ID
     * @param viewer 查看者（可为null，表示未登录用户）
     * @return 是否有权限查看
     */
    boolean canViewFollowInfo(Long profileOwnerId, UserDetails viewer);
}
