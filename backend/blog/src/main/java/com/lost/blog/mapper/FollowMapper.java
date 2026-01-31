package com.lost.blog.mapper;

import com.lost.blog.dto.FollowUserResponse;
import com.lost.blog.model.Follow;
import com.lost.blog.model.User;

/**
 * 关注关系映射器
 * 负责将Follow实体转换为DTO
 */
public class FollowMapper {

    /**
     * 将关注关系转换为关注用户响应（用于关注列表）
     * 返回被关注者的信息
     * @param follow 关注关系
     * @param isFriend 是否为朋友（互相关注）
     * @return 关注用户响应DTO
     */
    public static FollowUserResponse toFollowingUserResponse(Follow follow, boolean isFriend) {
        if (follow == null) {
            return null;
        }
        User followed = follow.getFollowed();
        return new FollowUserResponse(
                followed.getId(),
                followed.getUsername(),
                followed.getNickname(),
                followed.getAvatarUrl(),
                followed.getBio(),
                follow.getCreatedAt(),
                isFriend
        );
    }

    /**
     * 将关注关系转换为粉丝用户响应（用于粉丝列表）
     * 返回关注者的信息
     * @param follow 关注关系
     * @param isFriend 是否为朋友（互相关注）
     * @return 粉丝用户响应DTO
     */
    public static FollowUserResponse toFollowerUserResponse(Follow follow, boolean isFriend) {
        if (follow == null) {
            return null;
        }
        User follower = follow.getFollower();
        return new FollowUserResponse(
                follower.getId(),
                follower.getUsername(),
                follower.getNickname(),
                follower.getAvatarUrl(),
                follower.getBio(),
                follow.getCreatedAt(),
                isFriend
        );
    }
}
