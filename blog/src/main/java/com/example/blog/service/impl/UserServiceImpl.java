package com.example.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.entity.User;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.IUserService;
import com.example.blog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        // 1. 将前端传来的明文密码进行 MD5 加密
        String encryptPwd = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 去数据库比对用户名和加密后的密码
        User user = query().eq("username", username).eq("password", encryptPwd).one();

        if (user == null) {
            throw new RuntimeException("用户名或密码错误！");
        }

        // 3. 登录成功，生成并派发 Token
        return jwtUtil.generateToken(user.getId(), user.getRole());
    }

    @Override
    public boolean register(User user) {
        long count = query().eq("username", user.getUsername()).count();
        if (count > 0) throw new RuntimeException("用户名已被占用！");

        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setRole("USER");
        // 如果前端没传昵称，默认使用用户名
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        return save(user);
    }
}