package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.entity.Comment;
import com.example.blog.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "评论管理接口")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @PostMapping
    @Operation(summary = "发表评论", description = "必须携带 Token。只需传 articleId 和 content")
    public Result<Boolean> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        // 从拦截器 request 中取出经过验证的用户 ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("未授权的访问");

        comment.setUserId(userId);
        return Result.success(commentService.save(comment));
    }

    @GetMapping("/article/{articleId}")
    @Operation(summary = "查看文章评论", description = "任何人可看，无需 Token")
    public Result<List<Comment>> getComments(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }
}