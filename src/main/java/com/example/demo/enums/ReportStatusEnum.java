package com.example.demo.enums;

import lombok.Getter;

@Getter
public enum ReportStatusEnum {
    NOTED("noted", "Noted"),
    REPAIR_IN_PROGRESS("repair_in_progress", "Repair in progress"),
    RESOLVED("resolved", "Resolved");

    private final String value;
    private final String desc;

    ReportStatusEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
