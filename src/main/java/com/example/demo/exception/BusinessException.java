package com.example.demo.exception;

public class BusinessException extends RuntimeException {
    
    private Integer code = 400; // Default error code

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
