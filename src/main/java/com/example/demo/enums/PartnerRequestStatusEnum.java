package com.example.demo.enums;

import lombok.Getter;

/**
 * 配对请求状态枚举
 */
@Getter
public enum PartnerRequestStatusEnum {
    PENDING("pending", "待处理"),
    ACCEPTED("accepted", "已接受"),
    REJECTED("rejected", "已拒绝");

    private final String value;
    private final String desc;

    PartnerRequestStatusEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
