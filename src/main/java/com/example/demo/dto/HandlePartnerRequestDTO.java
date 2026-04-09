package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "处理配对请求参数")
public class HandlePartnerRequestDTO {

    @NotBlank(message = "Status cannot be empty")
    @Schema(description = "操作: accepted 或 rejected", example = "accepted", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}
