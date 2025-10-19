package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class ReplyRequest {

    @NotEmpty(message = "回复内容不能为空")
    @Size(min = 1, max = 3000, message = "回复内容长度必须在1到3000个字符之间")
    private String content;

    private Long replyToUserId;  // Optional: ID of the user being replied to

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }
}
