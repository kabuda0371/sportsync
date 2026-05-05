package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.dto.CreateReportDTO;
import com.example.demo.dto.UpdateReportStatusDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.EquipmentReportService;
import com.example.demo.vo.EquipmentReportVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class EquipmentReportController {

    @Autowired
    private EquipmentReportService equipmentReportService;

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public Result<EquipmentReportVO> createReport(@Valid @RequestBody CreateReportDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "User not logged in");
        }
        return Result.success(equipmentReportService.createReport(userId, dto));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public Result<List<EquipmentReportVO>> getMyReports() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "User not logged in");
        }
        return Result.success(equipmentReportService.getMyReports(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public Result<IPage<EquipmentReportVO>> getAllReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String status) {
        Long staffId = UserContext.getUserId();
        return Result.success(equipmentReportService.getAllReports(staffId, page, size, status));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusDTO dto) {
        Long staffId = UserContext.getUserId();
        equipmentReportService.updateReportStatus(staffId, id, dto);
        return Result.success(null);
    }
}
