package com.example.blog.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;    // 狀態碼 (200成功, 500失敗等)
    private String message;  // 提示訊息
    private T data;          // 實際回傳的資料

    // 成功
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    // 失敗
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}