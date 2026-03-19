package com.example.blog.controller;

import com.example.blog.annotation.AdminOnly;
import com.example.blog.common.Result;
import com.example.blog.entity.Article;
import com.example.blog.service.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "文章管理介面", description = "提供文章的 (CRUD) API")
public class ArticleController {

    @Autowired
    private IArticleService articleService;

    @PostMapping
    @AdminOnly //管理员
    @Operation(summary = "新增文章", description = "传入标题内容即可新增一篇文章")
    public Result<Boolean> save(@RequestBody Article article) {
        boolean isSuccess = articleService.save(article);
        return isSuccess ? Result.success(true) : Result.error("新增失败");
    }

    @DeleteMapping("/{id}")
    @AdminOnly //管理员
    @Operation(summary = "刪除文章", description = "根據文章 ID 刪除指定的文章")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean isSuccess = articleService.removeById(id);
        return isSuccess ? Result.success(true) : Result.error("刪除失败");
    }

    @PutMapping
    @AdminOnly //管理员
    @Operation(summary = "修改文章", description = "傳入包含 ID 的文章物件进行修改")
    public Result<Boolean> update(@RequestBody Article article) {
        boolean isSuccess = articleService.updateById(article);
        return isSuccess ? Result.success(true) : Result.error("修改失败");
    }

    // 4. 查询所有文章 (包含分类名称)
    @GetMapping
    @Operation(summary = "查询所有文章", description = "获取数据库中所有的文章列表（包含分类名）")
    public Result<List<Article>> list() {
        List<Article> list = articleService.getArticleListWithCategory();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询", description = "获取单篇文章的详细内容（包含分类名）")
    public Result<Article> getById(@PathVariable Long id) {
        Article article = articleService.getArticleDetailWithCategory(id);
        return article != null ? Result.success(article) : Result.error("文章不存在");
    }



    @PostMapping("/{id}/like")
    @Operation(summary = "文章点赞/取消点赞", description = "必须携带 Token。自动判断，点过就是取消，没点就是点赞")
    public Result<Boolean> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("请先登录！");

        try {
            boolean currentStatus = articleService.toggleLike(id, userId);
            return Result.success(currentStatus);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 🌟 新增：获取我的点赞列表 (用于个人主页)
    @GetMapping("/my/liked")
    @Operation(summary = "获取我点赞的文章", description = "必须携带 Token。用于个人主页展示")
    public Result<List<Article>> getMyLikedArticles(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("请先登录！");

        List<Article> list = articleService.getLikedArticlesByUserId(userId);
        return Result.success(list);
    }



}