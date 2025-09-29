package com.lost.blog.service; // 确保包名正确

import com.lost.blog.model.User;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // 声明这是一个Service组件，Spring会自动扫描并注册它
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 使用构造函数进行依赖注入，这是Spring推荐的方式
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String username, String password, String email) {
        // 1. 检查用户名或邮箱是否已经被注册
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("错误：用户名已存在！");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("错误：该邮箱已被注册！");
        }
        // --- 新增的检查逻辑 ---
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("错误：该邮箱已被注册！");
        }
        // 2. 创建一个新的User对象
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        // 3. 对密码进行加密处理
        user.setPassword(passwordEncoder.encode(password));

        // 4. 保存用户到数据库
        return userRepository.save(user);
    }
}