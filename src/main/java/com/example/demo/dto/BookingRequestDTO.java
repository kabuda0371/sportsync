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
    @NotNull(message = "Facility ID cannot be empty")
    private Long facilityId;

    @Schema(description = "预订日期", example = "2024-03-10")
    @NotNull(message = "Booking date cannot be empty")
    @FutureOrPresent(message = "Booking date must be today or in the future")
    private LocalDate bookingDate;

    @Schema(description = "开始时间", example = "10:00:00")
    @NotNull(message = "Start time cannot be empty")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "11:00:00")
    @NotNull(message = "End time cannot be empty")
    private LocalTime endTime;

    @Schema(description = "活动描述（可选），说明预期活动内容", example = "羽毛球双打比赛")
    @Size(max = 500, message = "Activity description cannot exceed 500 characters")
    @NotNull(message = "Activity description cannot be empty")
    private String activityDescription;
}
