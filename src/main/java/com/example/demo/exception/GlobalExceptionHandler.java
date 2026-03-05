package com.example.demo.exception;

import com.example.demo.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * 全局异常处理器
 * 拦截所有 Controller 层抛出的异常，并统一封装返回格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 参数校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return Result.error(400, "参数校验失败", errors);
    }

    /**
     * 处理业务异常 (如注册邮箱已存在等)
     * 这里捕获最基础的 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException ex) {
        return Result.error(500, ex.getMessage());
    }

    /**
     * 处理其他所有未捕获的系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        ex.printStackTrace(); // 真实项目中推荐使用日志记录：log.error("System Error", ex)
        return Result.error(500, "系统内部错误，请联系管理员");
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 返回 BusinessException 中携带的自定义状态码，如果没有指定则默认为 400
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 JSON 解析异常，例如日期格式错误等
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return Result.error(400, "参数格式错误，请检查输入数据的格式");
    }
}
