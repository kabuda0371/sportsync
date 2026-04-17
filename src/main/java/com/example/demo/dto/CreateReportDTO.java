package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportDTO {

    @NotNull(message = "Facility ID is required")
    private Long facilityId;

    @NotBlank(message = "Description is required")
    private String description;
}
