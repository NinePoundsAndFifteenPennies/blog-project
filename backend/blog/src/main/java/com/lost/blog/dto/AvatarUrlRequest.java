package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;

public class AvatarUrlRequest {
    
    @NotEmpty(message = "头像URL不能为空")
    private String avatarUrl;

    public AvatarUrlRequest() {
    }

    public AvatarUrlRequest(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
