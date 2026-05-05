package com.example.demo.enums;

import lombok.Getter;

@Getter
public enum NotificationTypeEnum {
    GENERAL("GENERAL", "General notification"),
    BOOKING("BOOKING", "Booking notification"),
    REPORT("REPORT", "Equipment report notification"),
    PARTNER_REQUEST_RECEIVED("PARTNER_REQUEST_RECEIVED", "Partner request received"),
    PARTNER_REQUEST_ACCEPTED("PARTNER_REQUEST_ACCEPTED", "Partner request accepted"),
    PARTNER_REQUEST_REJECTED("PARTNER_REQUEST_REJECTED", "Partner request rejected");

    private final String value;
    private final String desc;

    NotificationTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
