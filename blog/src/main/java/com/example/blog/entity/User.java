package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
@Schema(description = "用户实体")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名", example = "testuser")
    private String username;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "角色 (ADMIN 或 USER)", hidden = true) // hidden=true 表示前端注册时不需要传这个字段
    private String role;
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像 URL")
    private String avatar;
    private LocalDateTime createTime;
}