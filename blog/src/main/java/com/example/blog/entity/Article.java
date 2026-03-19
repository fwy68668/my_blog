package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article")
@Schema(description = "文章实体对象")
public class Article {

    @TableId(type = IdType.AUTO)
    @Schema(description = "文章 ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "所属分类 ID")
    private Long categoryId;

    @Schema(description = "浏览量", hidden = true) // 后端自动控制，前端新增时不传
    private Integer viewCount;

    @Schema(description = "点赞数", hidden = true)
    private Integer likeCount;

    @Schema(description = "建立时间", hidden = true)
    private LocalDateTime createTime;

    // 🌟 重点：这个字段数据库表里没有，是专门为了给前端展示“分类名称”用的
    @TableField(exist = false)
    @Schema(description = "分类名称 (前端展示用)", hidden = true)
    private String categoryName;
}