package com.lost.blog.service; // 确保包名正确

import com.lost.blog.model.User;

public interface UserService {
    User registerUser(String username, String password, String email);
    
    User updateAvatar(String username, String avatarUrl);
    
    User findByUsername(String username);
}