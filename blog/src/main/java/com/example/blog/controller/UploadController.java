package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传接口", description = "用于上传图片到阿里云 OSS")
public class UploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping
    @Operation(summary = "图片上传", description = "上传单张图片，返回阿里云图片 URL")
    public Result<String> uploadImage(MultipartFile file) {
        try {
            String imageUrl = aliOssUtil.upload(file);
            return Result.success(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }
}