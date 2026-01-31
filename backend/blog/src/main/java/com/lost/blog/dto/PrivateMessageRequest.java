package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 发送私信请求DTO
 */
public class PrivateMessageRequest {

    @NotEmpty(message = "消息内容不能为空")
    @Size(max = 3000, message = "消息内容不能超过3000个字符")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
