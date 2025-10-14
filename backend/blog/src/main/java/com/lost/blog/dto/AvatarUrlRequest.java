package com.lost.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AvatarUrlRequest {
    
    @NotBlank(message = "头像URL不能为空")
    @JsonProperty("avatarUrl")
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
