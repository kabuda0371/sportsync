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
@Schema(description = "Notification payload")
public class NotificationVO {

    @Schema(description = "Notification ID")
    private Long id;

    @Schema(description = "Notification type")
    private String type;

    @Schema(description = "Related business entity ID")
    private Long relatedId;

    @Schema(description = "Related booking ID")
    private Long bookingId;

    @Schema(description = "Notification message")
    private String message;

    @Schema(description = "Whether the notification has been read")
    private Boolean isRead;

    @Schema(description = "Notification creation time")
    private LocalDateTime createdAt;
}
