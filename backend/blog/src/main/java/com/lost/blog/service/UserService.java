package com.lost.blog.service;

import com.lost.blog.model.User;
import com.lost.blog.dto.UserProfileUpdateRequest;

public interface UserService {
    User registerUser(String username, String password, String email);
    
    User updateUserAvatar(String username, String avatarUrl);
    
    User findByUsername(String username);

    User updateProfile(String username, UserProfileUpdateRequest request);
    
    /**
     * Get public user profile by username (for viewing other users' profiles)
     * @param username The username to look up
     * @return User entity with public information
     */
    User getPublicProfile(String username);
}
