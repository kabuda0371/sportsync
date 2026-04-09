package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户信息响应对象")
public class UserVO {
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "角色", example = "member")
    private String role;

    @Schema(description = "账号状态", example = "approved")
    private String accountStatus;

    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "地址", example = "北京市朝阳区XXX号")
    private String address;

    @Schema(description = "是否开启伙伴匹配", example = "false")
    private Boolean partnerMatchingEnabled;

    @Schema(description = "偏好运动列表", example = "[\"badminton\", \"tennis\"]")
    private List<String> preferredSport;

    @Schema(description = "技能水平", example = "intermediate")
    private String skillLevel;

    @Schema(description = "可用时间列表", example = "[\"weekday_evening\", \"weekend_morning\"]")
    private List<String> availability;

    @Schema(description = "活动简介", example = "喜欢周末打羽毛球双打")
    private String partnerBio;

    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
}
