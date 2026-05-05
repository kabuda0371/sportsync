package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "重置密码请求参数")
public class ResetPasswordDTO {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Schema(description = "用户邮箱", example = "member@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Reset code cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Reset code must be a 6-digit number")
    @Schema(description = "6位重置验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])[^\\s]{8,30}$", message = "Password must be 8-30 characters and include uppercase, lowercase, numbers, and special characters")
    @Schema(description = "新密码", example = "StrongP@ss123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
