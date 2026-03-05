package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "设施可用时段查询参数")
public class FacilityQueryDTO {

    @Schema(description = "设施ID", example = "1")
    private Long facilityId;

    @Schema(description = "查询日期", example = "2024-03-10")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
}
