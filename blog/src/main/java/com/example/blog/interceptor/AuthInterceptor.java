package com.example.blog.interceptor;

import com.example.blog.annotation.AdminOnly;
import com.example.blog.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行跨域预检请求 (很重要，前后端分离常踩坑)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        boolean isAdminOnly = handlerMethod.hasMethodAnnotation(AdminOnly.class);

        // 获取请求头中的 Token
        String authHeader = request.getHeader("Authorization");

        System.out.println("\n====== [Auth 拦截器调试] ======");
        System.out.println("👉 访问接口: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("👉 原始 Authorization Header: " + authHeader);

        String token = null;
        if (authHeader != null && !authHeader.isEmpty()) {
            // 【史诗级容错增强】
            // 如果前端乖乖传了 Bearer，我们截取后面的部分
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                // 如果前端忘记加 Bearer 前缀，我们直接把整个字符串当成 Token！
                token = authHeader;
            }
            System.out.println("✅ 成功提取 Token: " + token);
        } else {
            System.out.println("❌ Header 为空！");
        }

        // 验证 Token
        if (token != null && !token.isEmpty()) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                Long userId = claims.get("userId", Long.class);

                System.out.println("✅ Token 解析成功！当前用户ID: " + userId + ", 角色: " + role);
                request.setAttribute("userId", userId);

                // 权限比对
                if (isAdminOnly && !"ADMIN".equals(role)) {
                    System.out.println("❌ 拦截：该接口需要 ADMIN，但当前用户是 " + role);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write("{\"code\":403, \"message\":\"权限不足，只有管理员可操作！\"}");
                    return false;
                }

                System.out.println("🚀 验证通过，放行！");
                return true;

            } catch (Exception e) {
                System.out.println("❌ 拦截：Token 解析异常或已过期: " + e.getMessage());
                response.setStatus(401);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401, \"message\":\"登录已过期或无效，请重新登录！\"}");
                return false;
            }
        }

        // 没有 Token 且必须是管理员的接口
        if (isAdminOnly) {
            System.out.println("❌ 拦截：未提供 Token，拒绝访问");
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401, \"message\":\"请先登录！\"}");
            return false;
        }

        System.out.println("🚀 验证通过 (无需Token的公开接口)，放行！");
        return true;
    }
}