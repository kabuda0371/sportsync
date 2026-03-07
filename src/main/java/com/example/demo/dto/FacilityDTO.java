package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Schema(description = "设施创建或更新请求参数")
public class FacilityDTO {

    @Schema(description = "设施名称", example = "一号羽毛球场", required = true)
    @NotBlank(message = "Facility name cannot be empty")
    private String name;

    @Schema(description = "设施类型", example = "羽毛球场", required = true)
    @NotBlank(message = "Facility type cannot be empty")
    private String type;

    @Schema(description = "设施描述", example = "室内标准羽毛球场地")
    private String description;

    @Schema(description = "使用指南", example = "请穿软底运动鞋")
    private String usageGuidelines;

    @Schema(description = "容量限制", example = "4")
    @NotNull(message = "Capacity limit cannot be empty")
    @Positive(message = "Capacity limit must be a positive number")
    private Integer capacityLimit;

    @Schema(description = "时间段限制（分钟）", example = "60")
    @NotNull(message = "Time slot limit cannot be empty")
    @Positive(message = "Time slot limit must be a positive number")
    private Integer timeSlotLimitMinutes;

    @Schema(description = "指派管理员ID", example = "2")
    private Long assignedStaffId;
}
