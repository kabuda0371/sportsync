package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户注册请求参数")
public class UserRegisterDTO {
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Schema(description = "用户邮箱", example = "newuser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$", message = "Password must contain uppercase and lowercase letters, numbers, and special characters, and be 8-20 characters long")
    @Schema(description = "强密码，必须包含大小写字母、数字和特殊字符，长度8-20", example = "StrongP@ss123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Name cannot be empty")
    @Schema(description = "用户姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Date of birth cannot be empty")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "出生日期", example = "1990-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address cannot be empty")
    @Schema(description = "用户地址", example = "北京市朝阳区XXX号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
}
