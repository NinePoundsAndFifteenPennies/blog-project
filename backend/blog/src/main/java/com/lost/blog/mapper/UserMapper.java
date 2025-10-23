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
            user.getAvatarUrl()
        );
    }
}

