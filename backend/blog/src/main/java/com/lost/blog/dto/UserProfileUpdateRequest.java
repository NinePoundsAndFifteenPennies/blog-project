package com.lost.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(min = 1, max = 50, message = "昵称长度必须在1到50个字符之间")
    private String nickname;

    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String bio;

    @Size(max = 255, message = "社交链接长度不能超过255个字符")
    @Pattern(regexp = "^$|https?://.*", message = "社交链接必须以 http/https 开头或留空")
    private String socialLink;

    @Size(max = 20, message = "性别长度不能超过20个字符")
    private String gender;

    /**
     * 生日字符串，允许为空；非空时在服务层解析为 LocalDate 并校验不晚于今日
     */
    private String birthday;

    @Size(max = 100, message = "所在地长度不能超过100个字符")
    private String location;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 6, message = "当前密码长度至少为6个字符")
    private String currentPassword;

    @Size(min = 6, message = "新密码长度至少为6个字符")
    private String newPassword;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSocialLink() {
        return socialLink;
    }

    public void setSocialLink(String socialLink) {
        this.socialLink = socialLink;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
