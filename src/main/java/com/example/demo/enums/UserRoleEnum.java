package com.example.demo.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {
    MEMBER("member", "普通会员"),
    STAFF("staff", "员工"),
    ADMIN("admin", "管理员");

    private final String value;
    private final String desc;

    UserRoleEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
