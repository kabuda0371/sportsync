package com.example.demo.common;

import lombok.Data;

@Data
public class Result<T> {
    
    private Integer code;    // 状态码：200成功，其他失败
    private String message;  // 提示信息
    private T data;          // 返回的数据（如果有）

    // 构造私有化，要求使用静态工厂方法获取对象
    private Result() {}

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回，没有包含额外数据
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功返回，携带附加数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功返回，携带自定义消息和附加数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败返回，携带失败提示信息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败返回，自定义状态码和失败提示信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败返回，除了状态码和提示，还带有更详细的错误数据（如参数校验的 details 字典）
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
