package com.lost.blog.service;

import com.lost.blog.dto.UserProfileUpdateRequest;
import com.lost.blog.model.User;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("错误：用户名已存在！");
        }

        // 检查邮箱是否已存在（只检查一次）
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("错误：该邮箱已被注册！");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }
    
    @Override
    public User updateUserAvatar(String username, String avatarUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }
    
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public User updateProfile(String username, UserProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getNickname() != null) {
            String nickname = request.getNickname().trim();
            if (nickname.isEmpty()) {
                throw new IllegalArgumentException("昵称不能为空");
            }
            if (!Objects.equals(nickname, user.getNickname()) && userRepository.existsByNickname(nickname)) {
                throw new RuntimeException("昵称已存在！");
            }
            user.setNickname(nickname);
        }

        if (request.getBio() != null) {
            user.setBio(trimToNull(request.getBio()));
        }

        if (request.getSocialLink() != null) {
            user.setSocialLink(trimToNull(request.getSocialLink()));
        }

        if (request.getGender() != null) {
            user.setGender(trimToNull(request.getGender()));
        }

        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }

        if (request.getLocation() != null) {
            user.setLocation(trimToNull(request.getLocation()));
        }

        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("邮箱不能为空");
            }
            if (!Objects.equals(email, user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("该邮箱已被注册！");
            }
            user.setEmail(email);
        }

        if (request.getNewPassword() != null || request.getCurrentPassword() != null) {
            if (request.getNewPassword() == null || request.getCurrentPassword() == null) {
                throw new IllegalArgumentException("修改密码需要同时提供当前密码和新密码");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("当前密码不正确");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return userRepository.save(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
