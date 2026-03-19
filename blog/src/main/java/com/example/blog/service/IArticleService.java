package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.Article;

import java.util.List;

//继承IService是MybatisPlus提供的 为了统一操作数据库
public interface IArticleService extends IService<Article> {
    // 获取包含分类名称的文章列表
    List<Article> getArticleListWithCategory();

    // 获取包含分类名称的文章详情
    Article getArticleDetailWithCategory(Long id);

    //切换点赞状态 (点赞/取消点赞)，返回当前是 true(已赞) 还是 false(未赞)
    boolean toggleLike(Long articleId, Long userId);

    //新增：获取某个用户点赞过的所有文章 (用于个人主页)
    List<Article> getLikedArticlesByUserId(Long userId);

    // 删除文章 (级联删除评论和点赞)
    void deleteArticleWithCascades(Long articleId);

}