package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
@Schema(description = "文章评论")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Schema(description = "关联的文章 ID", example = "1")
    private Long articleId;
    @Schema(description = "留言的用户 ID", hidden = true) // 拦截器自动获取，前端不传
    private Long userId;
    @Schema(description = "评论内容", example = "前排支持大佬！")
    private String content;
    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
    private LocalDateTime createTime;
}