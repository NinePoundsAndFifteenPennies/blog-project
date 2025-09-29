
package com.lost.blog.model; // 确保包名与你的项目一致

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity // 声明这是一个JPA实体类，它将映射到数据库的一张表
@Table(name = "users") // 指定映射的数据库表名为 "users"
public class User {

    @Id // 标记这是主键字段
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键生成策略为自增
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Column(nullable = false, unique = true, length = 50) // 设置数据库列的约束：非空、唯一、长度50
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为6个字符")
    @Column(nullable = false)
    private String password;

    @Email(message = "邮箱格式不正确")
    @NotEmpty(message = "邮箱不能为空")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 在持久化之前自动设置创建时间
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 在更新之前自动设置更新时间
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    // (如果使用了Lombok，则无需手动编写这些)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}