package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.entity.User;
import com.example.blog.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户认证接口", description = "处理注册与登录发牌逻辑")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Boolean> register(@RequestBody User user) {
        try {
            // 直接传 user 对象
            return Result.success(userService.register(user));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "登录成功后返回 JWT Token 字符串")
    public Result<String> login(@RequestBody User user) {
        try {
            String token = userService.login(user.getUsername(), user.getPassword());
            return Result.success(token);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    @GetMapping("/info")
    @Operation(summary = "获取当前登录用户信息", description = "根据 Token 获取当前用户资料（包含头像）")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("请先登录！");

        User user = userService.getById(userId);
        user.setPassword(null); // 绝对不能把密码传给前端
        return Result.success(user);
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户资料", description = "例如前端调用 OSS 拿到头像 URL 后，调用此接口保存")
    public Result<Boolean> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("请先登录！");

        // 只允许更新自己的信息
        user.setId(userId);
        // 防御性编程：防止普通用户通过这个接口把自己的 role 改成 ADMIN
        user.setRole(null);
        user.setPassword(null);

        return Result.success(userService.updateById(user));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "获取公开用户信息", description = "用于点击头像查看他人主页，无需 Token")
    public Result<User> getPublicUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 绝对不能把密码传给前端
        }
        return Result.success(user);
    }
}