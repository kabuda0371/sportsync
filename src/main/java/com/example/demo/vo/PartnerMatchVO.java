package com.example.demo.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "匹配伙伴信息")
public class PartnerMatchVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "偏好运动列表")
    private List<String> preferredSport;

    @Schema(description = "技能水平")
    private String skillLevel;

    @Schema(description = "可用时间列表")
    private List<String> availability;

    @Schema(description = "活动简介")
    private String partnerBio;
}
