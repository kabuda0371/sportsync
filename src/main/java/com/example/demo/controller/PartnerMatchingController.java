package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.dto.HandlePartnerRequestDTO;
import com.example.demo.dto.PartnerProfileDTO;
import com.example.demo.dto.PartnerRequestDTO;
import com.example.demo.service.PartnerMatchingService;
import com.example.demo.vo.PartnerMatchVO;
import com.example.demo.vo.PartnerRequestVO;
import com.example.demo.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/partner-matching")
@Tag(name = "伙伴匹配", description = "伙伴匹配活动简介管理和配对请求接口")
@RequiredArgsConstructor
public class PartnerMatchingController {

    private final PartnerMatchingService partnerMatchingService;

    @PutMapping("/profile")
    @Operation(summary = "更新活动简介", description = "创建或更新伙伴匹配活动简介（偏好运动、技能水平、可用时间），同时开启/关闭匹配功能")
    public Result<UserVO> updatePartnerProfile(@Valid @RequestBody PartnerProfileDTO dto) {
        Long userId = UserContext.getUserId();
        UserVO userVO = partnerMatchingService.updatePartnerProfile(userId, dto);
        return Result.success("活动简介更新成功", userVO);
    }

    @GetMapping("/matches")
    @Operation(summary = "获取匹配伙伴列表", description = "根据当前用户的活动简介，查找匹配的伙伴（运动交集匹配，优先相同技能水平和可用时间），支持分页、筛选和按运动搜索")
    public Result<IPage<PartnerMatchVO>> getMatches(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "按技能水平筛选（可选）") @RequestParam(required = false) String skillLevel,
            @Parameter(description = "按可用时间筛选（可选）") @RequestParam(required = false) String availability,
            @Parameter(description = "按指定运动搜索（可选，不传则使用用户自己的偏好运动）") @RequestParam(required = false) String sport) {
        Long userId = UserContext.getUserId();
        IPage<PartnerMatchVO> matches = partnerMatchingService.getMatches(userId, page, size, skillLevel, availability, sport);
        return Result.success("获取匹配列表成功", matches);
    }

    @GetMapping("/partners")
    @Operation(summary = "获取我的伙伴列表", description = "获取所有已接受的伙伴关系列表")
    public Result<IPage<PartnerMatchVO>> getMyPartners(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<PartnerMatchVO> partners = partnerMatchingService.getMyPartners(userId, page, size);
        return Result.success("获取伙伴列表成功", partners);
    }

    @PostMapping("/requests")
    @Operation(summary = "发送配对请求", description = "向目标用户发送伙伴配对请求")
    public Result<PartnerRequestVO> sendRequest(@Valid @RequestBody PartnerRequestDTO dto) {
        Long userId = UserContext.getUserId();
        PartnerRequestVO requestVO = partnerMatchingService.sendRequest(userId, dto);
        return Result.success("配对请求已发送", requestVO);
    }

    @GetMapping("/requests/received")
    @Operation(summary = "查看收到的配对请求", description = "查看其他用户发送给自己的待处理配对请求")
    public Result<IPage<PartnerRequestVO>> getReceivedRequests(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<PartnerRequestVO> requests = partnerMatchingService.getReceivedRequests(userId, page, size);
        return Result.success("获取收到的请求成功", requests);
    }

    @GetMapping("/requests/sent")
    @Operation(summary = "查看已发出的配对请求", description = "查看自己发送的所有配对请求及其状态")
    public Result<IPage<PartnerRequestVO>> getSentRequests(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<PartnerRequestVO> requests = partnerMatchingService.getSentRequests(userId, page, size);
        return Result.success("获取已发出的请求成功", requests);
    }

    @PatchMapping("/requests/{id}")
    @Operation(summary = "处理配对请求", description = "接受或拒绝收到的伙伴配对请求")
    public Result<PartnerRequestVO> handleRequest(
            @Parameter(description = "请求ID") @PathVariable Long id,
            @Valid @RequestBody HandlePartnerRequestDTO dto) {
        Long userId = UserContext.getUserId();
        PartnerRequestVO requestVO = partnerMatchingService.handleRequest(userId, id, dto.getStatus());
        return Result.success("请求处理成功", requestVO);
    }

    @DeleteMapping("/requests/{id}")
    @Operation(summary = "取消配对请求", description = "取消自己发送的待处理配对请求")
    public Result<Void> cancelRequest(@Parameter(description = "请求ID") @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        partnerMatchingService.cancelRequest(userId, id);
        return Result.success("配对请求已取消", null);
    }
}
