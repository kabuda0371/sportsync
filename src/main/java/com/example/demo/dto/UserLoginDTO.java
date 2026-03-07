package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户登录请求参数")
public class UserLoginDTO {
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Schema(description = "用户邮箱", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Schema(description = "用户密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
