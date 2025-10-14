package com.lost.blog.service;

import com.lost.blog.model.User;

public interface UserService {
    User registerUser(String username, String password, String email);
    
    User updateUserAvatar(String username, String avatarUrl);
    
    User findByUsername(String username);
}
