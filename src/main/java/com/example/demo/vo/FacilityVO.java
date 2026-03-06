package com.example.demo.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityVO {
    @Schema(description = "设施ID")
    private Long id;
    @Schema(description = "设施名称")
    private String name;
    @Schema(description = "设施类型")
    private String type;
    @Schema(description = "设施描述")
    private String description;
    @Schema(description = "使用指南")
    private String usageGuidelines;
    @Schema(description = "容量限制")
    private Integer capacityLimit;
    @Schema(description = "每次预订限制时长(分钟)")
    private Integer timeSlotLimitMinutes;
    @Schema(description = "指派管理员ID")
    private Long assignedStaffId;
    @Schema(description = "指派管理员姓名")
    private String assignedStaffName;
}
