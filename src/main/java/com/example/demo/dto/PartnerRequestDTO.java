package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "发送配对请求参数")
public class PartnerRequestDTO {

    @NotNull(message = "Target user ID cannot be null")
    @Schema(description = "目标用户ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @Schema(description = "附言（可选）", example = "Hi, I'd love to play badminton together!")
    private String message;
}
