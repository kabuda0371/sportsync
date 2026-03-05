package com.example.demo.enums;

import lombok.Getter;

/**
 * 认证方式枚举
 */
@Getter
public enum AuthProviderEnum {
    LOCAL("local", "本地"),
    GOOGLE("google", "Google"),
    FACEBOOK("facebook", "Facebook");

    private final String value;
    private final String desc;

    AuthProviderEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
