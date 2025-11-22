package com.lost.blog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 分类请求DTO
 * 用于创建和更新分类
 */
public class CategoryRequest {

    @NotEmpty(message = "分类名称不能为空")
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s_-]+$", message = "分类名称只能包含中文、英文、数字、空格、下划线和连字符")
    private String name;

    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    private String description;

    @Size(max = 7, message = "颜色代码长度不能超过7个字符")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色代码格式必须为 #RRGGBB，例如 #FF5733")
    private String color;  // 分类颜色，格式如 #FF5733

    @Size(max = 50, message = "图标名称长度不能超过50个字符")
    private String icon;  // 分类图标名称或图标类

    @Min(value = 0, message = "排序顺序必须大于或等于0")
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
