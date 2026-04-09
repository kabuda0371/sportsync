package com.example.demo.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 可用时间枚举
 */
@Getter
public enum AvailabilityEnum {
    WEEKDAY_MORNING("weekday_morning", "工作日上午"),
    WEEKDAY_AFTERNOON("weekday_afternoon", "工作日下午"),
    WEEKDAY_EVENING("weekday_evening", "工作日晚上"),
    WEEKEND_MORNING("weekend_morning", "周末上午"),
    WEEKEND_AFTERNOON("weekend_afternoon", "周末下午"),
    WEEKEND_EVENING("weekend_evening", "周末晚上");

    private final String value;
    private final String desc;

    AvailabilityEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(e -> e.getValue().equals(value));
    }
}
