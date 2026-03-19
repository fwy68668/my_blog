package com.example.blog.controller;

import com.example.blog.annotation.AdminOnly;
import com.example.blog.common.Result;
import com.example.blog.entity.Category;
import com.example.blog.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理接口")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping
    @Operation(summary = "获取所有分类", description = "所有人可访问")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    @AdminOnly
    @Operation(summary = "新增分类", description = "仅管理员")
    public Result<Boolean> save(@RequestBody Category category) {
        return Result.success(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @Operation(summary = "删除分类", description = "仅管理员")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }
}