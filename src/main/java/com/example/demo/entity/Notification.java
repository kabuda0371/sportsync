package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notifications")
public class Notification {

    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookingId;

    private String message;

    @Builder.Default
    private Boolean isRead = false;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
