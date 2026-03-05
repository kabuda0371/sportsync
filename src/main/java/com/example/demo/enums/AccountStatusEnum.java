package com.example.demo.enums;

import lombok.Getter;

/**
 * 账号状态枚举
 */
@Getter
public enum AccountStatusEnum {
    PENDING("pending", "待批准"),
    APPROVED("approved", "已批准"),
    SUSPENDED("suspended", "已封禁");

    private final String value;
    private final String desc;

    AccountStatusEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
