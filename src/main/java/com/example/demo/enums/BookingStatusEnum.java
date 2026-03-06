package com.example.demo.enums;

import lombok.Getter;

/**
 * 预订状态枚举
 */
@Getter
public enum BookingStatusEnum {
    PENDING("pending", "待审批"),
    APPROVED("approved", "已批准"),
    REJECTED("rejected", "已拒绝"),
    CANCELLED("cancelled", "已取消"),
    COMPLETED("completed", "已完成");

    private final String value;
    private final String desc;

    BookingStatusEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
