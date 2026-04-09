package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户资料补全请求参数")
public class ProfileUpdateDTO {

    @NotNull(message = "Date of birth cannot be empty")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "出生日期", example = "1990-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address cannot be empty")
    @Schema(description = "用户地址", example = "北京市朝阳区XXX号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
}
