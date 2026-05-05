package com.example.demo.controller;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partner-matching")
@PreAuthorize("hasRole('MEMBER')")
@Tag(name = "伙伴匹配", description = "仅限会员使用的伙伴匹配与请求管理接口")
@RequiredArgsConstructor
public class PartnerMatchingController {

    private final PartnerMatchingService partnerMatchingService;

    @PutMapping("/profile")
    @Operation(summary = "创建或更新匹配资料", description = "创建或更新当前会员的运动偏好、技能水平与可用时间等匹配资料")
    public Result<UserVO> updatePartnerProfile(@Valid @RequestBody PartnerProfileDTO dto) {
        Long userId = UserContext.getUserId();
        UserVO userVO = partnerMatchingService.updatePartnerProfile(userId, dto);
        return Result.success("匹配资料更新成功", userVO);
    }

    @GetMapping("/matches")
    @Operation(summary = "获取匹配伙伴列表", description = "分页查询与当前会员资料相匹配的伙伴列表，并支持按技能、时间和运动类型筛选")
    public Result<IPage<PartnerMatchVO>> getMatches(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "技能水平筛选，可选") @RequestParam(required = false) String skillLevel,
            @Parameter(description = "可用时间筛选，可选") @RequestParam(required = false) String availability,
            @Parameter(description = "运动类型筛选，可选") @RequestParam(required = false) String sport) {
        Long userId = UserContext.getUserId();
        IPage<PartnerMatchVO> matches = partnerMatchingService.getMatches(userId, page, size, skillLevel, availability, sport);
        return Result.success("获取匹配伙伴列表成功", matches);
    }

    @GetMapping("/partners")
    @Operation(summary = "获取我的伙伴列表", description = "分页查询当前会员已建立关系的伙伴列表")
    public Result<IPage<PartnerMatchVO>> getMyPartners(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<PartnerMatchVO> partners = partnerMatchingService.getMyPartners(userId, page, size);
        return Result.success("获取我的伙伴列表成功", partners);
    }

    @PostMapping("/requests")
    @Operation(summary = "发送伙伴请求", description = "向目标会员发送伙伴匹配请求")
    public Result<PartnerRequestVO> sendRequest(@Valid @RequestBody PartnerRequestDTO dto) {
        Long userId = UserContext.getUserId();
        PartnerRequestVO requestVO = partnerMatchingService.sendRequest(userId, dto);
        return Result.success("伙伴请求发送成功", requestVO);
    }

    @GetMapping("/requests/received")
    @Operation(summary = "获取收到的伙伴请求", description = "分页查询当前会员收到的伙伴请求")
    public Result<IPage<PartnerRequestVO>> getReceivedRequests(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<PartnerRequestVO> requests = partnerMatchingService.getReceivedRequests(userId, page, size);
        return Result.success("获取收到的伙伴请求成功", requests);
    }

    @GetMapping("/requests/sent")
    @Operation(summary = "获取发出的伙伴请求", description = "分页查询当前会员已发出的伙伴请求")
    public Result<IPage<PartnerRequestVO>> getSentRequests(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<PartnerRequestVO> requests = partnerMatchingService.getSentRequests(userId, page, size);
        return Result.success("获取发出的伙伴请求成功", requests);
    }

    @PatchMapping("/requests/{id}")
    @Operation(summary = "处理伙伴请求", description = "接受或拒绝收到的伙伴请求")
    public Result<PartnerRequestVO> handleRequest(
            @Parameter(description = "请求ID") @PathVariable Long id,
            @Valid @RequestBody HandlePartnerRequestDTO dto) {
        Long userId = UserContext.getUserId();
        PartnerRequestVO requestVO = partnerMatchingService.handleRequest(userId, id, dto.getStatus());
        return Result.success("处理伙伴请求成功", requestVO);
    }

    @DeleteMapping("/requests/{id}")
    @Operation(summary = "取消伙伴请求", description = "取消当前会员已发出的伙伴请求")
    public Result<Void> cancelRequest(@Parameter(description = "请求ID") @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        partnerMatchingService.cancelRequest(userId, id);
        return Result.success("取消伙伴请求成功", null);
    }

    @DeleteMapping("/partners/{partnerId}")
    @Operation(summary = "解除伙伴关系", description = "解除当前会员与指定伙伴的已接受关系")
    public Result<Void> removePartner(@Parameter(description = "伙伴用户ID") @PathVariable Long partnerId) {
        Long userId = UserContext.getUserId();
        partnerMatchingService.removePartner(userId, partnerId);
        return Result.success("伙伴关系已解除", null);
    }
}
