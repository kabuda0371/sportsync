package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentReportVO {

    private Long id;

    private Long userId;

    private String reporterName;

    private Long facilityId;

    private String facilityName;

    private String facilityType;

    private String description;

    private String status;

    private LocalDateTime createdAt;
}
