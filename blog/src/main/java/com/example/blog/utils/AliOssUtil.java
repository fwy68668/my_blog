package com.example.blog.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class AliOssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    /**
     * 实现文件上传到阿里云 OSS (按年月自动分目录)
     */
    public String upload(MultipartFile file) throws Exception {
        // 1. 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "unknown";
        }

        // 2. 安全获取文件后缀 (例如 .png, .jpg)
        String extension = "";
        int lastIndexOfDot = originalFilename.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            extension = originalFilename.substring(lastIndexOfDot);
        }

        // 3. 获取当前年月，生成文件夹路径 (格式：yyyy/MM，例如 2026/03)
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        // 4. 构造带有时间目录的全新文件名 (例如：2026/03/550e8400-e29b-41d4-a716-446655440000.jpg)
        String fileName = datePath + "/" + UUID.randomUUID().toString() + extension;

        // 5. 创建 OSSClient 实例并上传
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, fileName, inputStream);

            // 6. 拼接并返回前端的图片外链 URL (确保 endpoint 配置时不带 http://)
            // 最终格式例如: https://your-blog-bucket.oss-cn-hangzhou.aliyuncs.com/2026/03/xxx.jpg
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}