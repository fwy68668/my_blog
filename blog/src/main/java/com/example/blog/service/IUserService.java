package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.User;

public interface IUserService extends IService<User> {
    // 登录验证，成功则返回 JWT Token
    String login(String username, String password);

    // 注册新用户
    boolean register(User user);
}