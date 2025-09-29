package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import com.lost.blog.model.ContentType; // 导入枚举
import jakarta.validation.constraints.NotNull; // 导入NotNull

public class PostRequest {

    @NotEmpty(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题长度不能超过100个字符")
    private String title;

    @NotEmpty(message = "文章内容不能为空")
    private String content;

    @NotNull(message = "必须指定内容类型")
    private ContentType contentType;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
}
