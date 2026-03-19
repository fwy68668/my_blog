package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.Comment;
import java.util.List;

public interface ICommentService extends IService<Comment> {
    List<Comment> getCommentsByArticleId(Long articleId);
}