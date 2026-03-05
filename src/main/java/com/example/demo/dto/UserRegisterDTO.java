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
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "用户邮箱", example = "newuser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$", message = "密码必须包含大小写字母、数字和特殊字符，且长度在8-20之间")
    @Schema(description = "强密码，必须包含大小写字母、数字和特殊字符，长度8-20", example = "StrongP@ss123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "用户姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    @Schema(description = "出生日期", example = "1990-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dateOfBirth;

    @NotBlank(message = "地址不能为空")
    @Schema(description = "用户地址", example = "北京市朝阳区XXX号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
}
