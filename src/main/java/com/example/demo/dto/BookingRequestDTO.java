package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequestDTO {

    @Schema(description = "设施ID", example = "1")
    @NotNull(message = "设施ID不能为空")
    private Long facilityId;

    @Schema(description = "预订日期", example = "2024-03-10")
    @NotNull(message = "预订日期不能为空")
    @FutureOrPresent(message = "预订日期必须是今天或以后的日期")
    private LocalDate bookingDate;

    @Schema(description = "开始时间", example = "10:00:00")
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "11:00:00")
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @Schema(description = "活动描述（可选），说明预期活动内容", example = "羽毛球双打比赛")
    @Size(max = 500, message = "活动描述不能超过500个字符")
    @NotNull(message = "活动描述不能为空")
    private String activityDescription;
}
