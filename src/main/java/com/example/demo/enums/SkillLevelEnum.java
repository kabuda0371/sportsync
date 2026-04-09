package com.example.demo.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 技能水平枚举
 */
@Getter
public enum SkillLevelEnum {
    BEGINNER("beginner", "初学者"),
    INTERMEDIATE("intermediate", "中级"),
    ADVANCED("advanced", "高级");

    private final String value;
    private final String desc;

    SkillLevelEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(e -> e.getValue().equals(value));
    }
}
