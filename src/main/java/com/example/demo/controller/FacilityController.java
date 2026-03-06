package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.FacilityService;
import com.example.demo.vo.FacilityVO;
import com.example.demo.dto.FacilityDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
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

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建设施", description = "管理员创建新的体育设施")
    public Result<Void> createFacility(@Valid @RequestBody FacilityDTO dto) {
        facilityService.createFacility(dto);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新设施", description = "管理员更新现有的体育设施信息")
    public Result<Void> updateFacility(@PathVariable Long id, @Valid @RequestBody FacilityDTO dto) {
        facilityService.updateFacility(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除设施", description = "管理员删除指定的体育设施")
    public Result<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/assign-staff/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分配管理员", description = "将指定的员工分配给设施作为管理员")
    public Result<Void> assignStaff(@PathVariable Long id, @PathVariable Long staffId) {
        facilityService.assignStaff(id, staffId);
        return Result.success(null);
    }
}
