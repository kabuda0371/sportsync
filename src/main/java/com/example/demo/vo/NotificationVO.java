package com.example.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知信息")
public class NotificationVO {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "关联的预订ID")
    private Long bookingId;

    @Schema(description = "通知内容")
    private String message;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "通知创建时间")
    private LocalDateTime createdAt;
}
