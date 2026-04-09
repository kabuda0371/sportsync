package com.example.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Schema(description = "伙伴匹配活动简介请求参数")
public class PartnerProfileDTO {

    @NotNull(message = "Partner matching enabled flag cannot be null")
    @Schema(description = "是否开启伙伴匹配", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean partnerMatchingEnabled;

    @NotNull(message = "Preferred sport cannot be null")
    @Size(min = 1, message = "At least one sport must be selected")
    @Schema(description = "偏好运动列表 (badminton/football/swimming/tennis)", example = "[\"badminton\", \"tennis\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> preferredSport;

    @NotBlank(message = "Skill level cannot be empty")
    @Schema(description = "技能水平 (beginner/intermediate/advanced)", example = "intermediate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skillLevel;

    @NotNull(message = "Availability cannot be null")
    @Size(min = 1, message = "At least one availability slot must be selected")
    @Schema(description = "可用时间列表 (weekday_morning/weekday_afternoon/weekday_evening/weekend_morning/weekend_afternoon/weekend_evening)", example = "[\"weekday_evening\", \"weekend_morning\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> availability;

    @Size(max = 500, message = "Partner bio cannot exceed 500 characters")
    @Schema(description = "活动简介（可选）", example = "喜欢周末打羽毛球双打，找水平差不多的搭档")
    private String partnerBio;
}
