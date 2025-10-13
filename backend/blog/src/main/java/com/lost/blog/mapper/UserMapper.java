package com.lost.blog.mapper;

import com.lost.blog.dto.UserResponse;
import com.lost.blog.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserResponse toResponse(User user) {
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
