package com.example.demo.enums;

import lombok.Getter;

/**
 * 携搭档预订邀约的响应状态
 */
@Getter
public enum InvitationStatusEnum {
    PENDING("pending", "待响应"),
    ACCEPTED("accepted", "已接受"),
    DECLINED("declined", "已拒绝");

    private final String value;
    private final String desc;

    InvitationStatusEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
