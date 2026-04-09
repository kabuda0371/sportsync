package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "邮箱验证请求参数")
public class VerifyEmailDTO {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Schema(description = "用户邮箱", example = "user@example.com")
    private String email;

    @NotBlank(message = "Verification code cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    @Schema(description = "6位数字验证码", example = "123456")
    private String code;
}
