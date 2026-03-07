package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "预订状态更新请求数据")
public class BookingStatusUpdateDTO {

    @NotBlank(message = "Approval status cannot be empty")
    @Schema(description = "审批状态 (approved / rejected)", example = "approved")
    private String status;

    @Size(max = 500, message = "Staff note cannot exceed 500 characters")
    @Schema(description = "工作人员备注（可选），说明审批原因或建议", example = "场地暂时维修，建议改用B场")
    private String staffNote;

    @Schema(description = "建议的替代设施ID（可选），拒绝时可提供替代方案", example = "2")
    private Long suggestedFacilityId;
}
