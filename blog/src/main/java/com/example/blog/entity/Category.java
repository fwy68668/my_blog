package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
@Schema(description = "文章分类实体")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "分类名称", example = "技术笔记")
    private String name;

    private LocalDateTime createTime;
}