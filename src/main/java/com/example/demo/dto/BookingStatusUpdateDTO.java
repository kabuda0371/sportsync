package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "预订状态更新请求数据")
public class BookingStatusUpdateDTO {
    
    @NotBlank(message = "状态不为空")
    @Schema(description = "审批状态(approved/rejected)", example = "approved")
    private String status;
}
