package com.example.demo.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 运动类型枚举
 */
@Getter
public enum SportEnum {
    BADMINTON("badminton", "羽毛球"),
    FOOTBALL("football", "足球"),
    SWIMMING("swimming", "游泳"),
    TENNIS("tennis", "网球");

    private final String value;
    private final String desc;

    SportEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(e -> e.getValue().equals(value));
    }
}
