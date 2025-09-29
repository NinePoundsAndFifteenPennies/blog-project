package com.lost.blog.security; // 确保包名正确

import com.lost.blog.model.User;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service // 声明为Service组件
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    // 判断是邮箱还是用户名
                    if (usernameOrEmail.contains("@")) {
                        return new UsernameNotFoundException("未找到邮箱为: " + usernameOrEmail + " 的用户");
                    } else {
                        return new UsernameNotFoundException("未找到用户名为: " + usernameOrEmail + " 的用户");
                    }
                });
         // 将我们的User实体转换为Spring Security所需的UserDetails对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}

