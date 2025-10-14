package com.lost.blog.service;

public interface FileService {
    String uploadAvatar(String username, org.springframework.web.multipart.MultipartFile file);
    
    String updateAvatar(String username, org.springframework.web.multipart.MultipartFile file);
    
    void deleteAvatar(String avatarUrl);
}
