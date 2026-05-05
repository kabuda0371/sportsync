package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvitationResponseDTO {

    @Schema(description = "是否接受邀约：true=接受，false=拒绝", example = "true")
    @NotNull(message = "Response decision (accept) cannot be empty")
    private Boolean accept;
}
