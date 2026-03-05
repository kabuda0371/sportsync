package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.FacilityService;
import com.example.demo.vo.FacilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@Tag(name = "设施管理", description = "查看体育设施列表及详情")
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    @GetMapping
    @Operation(summary = "获取所有设施列表", description = "返回系统中所有可用的体育设施信息")
    public Result<List<FacilityVO>> getAllFacilities() {
        return Result.success(facilityService.getAllFacilities());
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过ID获取设施详情", description = "根据设施ID返回具体的设施描述、容量及使用指南")
    public Result<FacilityVO> getFacilityById(@PathVariable Long id) {
        return Result.success(facilityService.getFacilityById(id));
    }
}
