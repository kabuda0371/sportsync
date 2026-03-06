package com.example.demo.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingVO {
    @Schema(description = "预订记录ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "设施ID")
    private Long facilityId;
    @Schema(description = "预订日期")
    private LocalDate bookingDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "预订状态 (pending, approved, rejected, cancelled)")
    private String status;
    @Schema(description = "活动描述，会员提交预订时填写的预期活动内容")
    private String activityDescription;
    @Schema(description = "工作人员审批备注")
    private String staffNote;
    @Schema(description = "建议替代设施ID（当拒绝时工作人员可提供替代方案）")
    private Long suggestedFacilityId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
