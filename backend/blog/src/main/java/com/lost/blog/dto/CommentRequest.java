package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    @NotEmpty(message = "评论内容不能为空")
    @Size(min = 1, max = 3000, message = "评论内容长度必须在1到3000个字符之间")
    private String content;

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
