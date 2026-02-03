package com.lost.blog.mapper;

import com.lost.blog.dto.UserResponse;
import com.lost.blog.model.User;

public class UserMapper {
    
    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getNickname(),
            user.getBio(),
            user.getSocialLink(),
            user.getGender(),
            user.getBirthday() != null ? user.getBirthday().toString() : null,
            user.getLocation(),
            user.getRole() != null ? user.getRole().name() : "USER"
        );
    }
}
