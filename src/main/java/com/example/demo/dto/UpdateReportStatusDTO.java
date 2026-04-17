package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateReportStatusDTO {

    @NotBlank(message = "Status is required")
    private String status;
}
