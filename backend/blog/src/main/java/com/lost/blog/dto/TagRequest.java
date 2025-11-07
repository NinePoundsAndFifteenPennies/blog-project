package com.lost.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 标签请求DTO
 * 用于创建和更新标签
 */
public class TagRequest {

    @NotEmpty(message = "标签名称不能为空")
    @Size(min = 1, max = 50, message = "标签名称长度必须在1-50个字符之间")
    private String name;

    @Size(max = 200, message = "标签描述长度不能超过200个字符")
    private String description;

    @Size(max = 7, message = "颜色代码长度不能超过7个字符")
    private String color;  // 标签颜色，格式如 #FF5733

    @Size(max = 50, message = "图标名称长度不能超过50个字符")
    private String icon;  // 标签图标名称或图标类

    private Integer sortOrder;  // 排序顺序，数字越小越靠前

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
