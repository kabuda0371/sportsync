package com.example.demo.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配对请求信息")
public class PartnerRequestVO {

    @Schema(description = "请求ID")
    private Long id;

    @Schema(description = "发起人ID")
    private Long requesterId;

    @Schema(description = "发起人姓名")
    private String requesterName;

    @Schema(description = "目标用户ID")
    private Long targetId;

    @Schema(description = "目标用户姓名")
    private String targetName;

    @Schema(description = "请求状态 (pending/accepted/rejected)")
    private String status;

    @Schema(description = "附言")
    private String message;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
