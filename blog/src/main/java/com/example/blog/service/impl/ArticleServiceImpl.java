package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.entity.UserLikeRecord;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.UserLikeRecordMapper;
import com.example.blog.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserLikeRecordMapper userLikeRecordMapper;
    @Override
    public List<Article> getArticleListWithCategory() {
        // 1. 查询所有文章
        List<Article> articles = this.list();

        // 2. 遍历文章，如果有 categoryId，就去查出对应的 name 塞进去
        for (Article article : articles) {
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.selectById(article.getCategoryId());
                if (category != null) {
                    article.setCategoryName(category.getName());
                }
            }
        }
        return articles;
    }

    @Override
    public Article getArticleDetailWithCategory(Long id) {
        Article article = this.getById(id);
        if (article != null && article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                article.setCategoryName(category.getName());
            }
        }
        return article;
    }
    @Override
    @Transactional // 开启事务保护
    public boolean toggleLike(Long articleId, Long userId) {
        // 1. 快速查一下文章存不存在 (不再把整篇文章拉到内存里，节省性能)
        long count = this.query().eq("id", articleId).count();
        if (count == 0) throw new RuntimeException("文章不存在！");

        // 2. 查点赞记录
        QueryWrapper<UserLikeRecord> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("article_id", articleId);
        UserLikeRecord record = userLikeRecordMapper.selectOne(query);

        if (record != null) {
            // 🌟 修复：取消点赞（原子级递减，利用 CASE WHEN 防止并发扣成负数）
            userLikeRecordMapper.deleteById(record.getId());
            this.update()
                    .setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END")
                    .eq("id", articleId)
                    .update();
            return false;
        } else {
            // 🌟 修复：新增点赞（原子级递增，利用 coalesce 防止初始 null 值报错）
            UserLikeRecord newRecord = new UserLikeRecord();
            newRecord.setUserId(userId);
            newRecord.setArticleId(articleId);
            userLikeRecordMapper.insert(newRecord);

            this.update()
                    .setSql("like_count = coalesce(like_count, 0) + 1")
                    .eq("id", articleId)
                    .update();
            return true;
        }
    }

    @Override
    public List<Article> getLikedArticlesByUserId(Long userId) {
        // 1. 查出该用户的所有点赞记录
        QueryWrapper<UserLikeRecord> query = new QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("create_time");
        List<UserLikeRecord> records = userLikeRecordMapper.selectList(query);

        if (records.isEmpty()) return new ArrayList<>();

        // 2. 提取出所有的文章 ID
        List<Long> articleIds = records.stream().map(UserLikeRecord::getArticleId).collect(Collectors.toList());

        // 3. 查出文章详情并组装分类名称 (复用之前的逻辑)
        List<Article> articles = this.listByIds(articleIds);
        for (Article article : articles) {
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.selectById(article.getCategoryId());
                if (category != null) article.setCategoryName(category.getName());
            }
        }
        return articles;
    }
}