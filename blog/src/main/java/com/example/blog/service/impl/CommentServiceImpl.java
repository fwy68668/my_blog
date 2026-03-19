package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    @Autowired
    private UserServiceImpl userService;
    @Override
    public List<Comment> getCommentsByArticleId(Long articleId) {
        QueryWrapper<Comment> query = new QueryWrapper<>();
        query.eq("article_id", articleId).orderByDesc("create_time");
        List<Comment> comments = this.list(query);

        // 核心补丁：遍历评论，查出用户信息并组装
        for (Comment comment : comments) {
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setAvatar(user.getAvatar());
            }
        }
        return comments;
    }
}