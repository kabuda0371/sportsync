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
    @NotBlank(message = "设施名称不能为空")
    private String name;

    @Schema(description = "设施类型", example = "羽毛球场", required = true)
    @NotBlank(message = "设施类型不能为空")
    private String type;

    @Schema(description = "设施描述", example = "室内标准羽毛球场地")
    private String description;

    @Schema(description = "使用指南", example = "请穿软底运动鞋")
    private String usageGuidelines;

    @Schema(description = "容量限制", example = "4")
    @NotNull(message = "容量限制不能为空")
    @Positive(message = "容量限制必须为正数")
    private Integer capacityLimit;

    @Schema(description = "时间段限制（分钟）", example = "60")
    @NotNull(message = "时间段限制不能为空")
    @Positive(message = "时间段限制必须为正数")
    private Integer timeSlotLimitMinutes;

    @Schema(description = "指派管理员ID", example = "2")
    private Long assignedStaffId;
}
