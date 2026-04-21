package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.CreateReportDTO;
import com.example.demo.dto.UpdateReportStatusDTO;
import com.example.demo.entity.EquipmentReport;
import com.example.demo.entity.Facility;
import com.example.demo.entity.User;
import com.example.demo.enums.ReportStatusEnum;
import com.example.demo.enums.UserRoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.EquipmentReportMapper;
import com.example.demo.service.EquipmentReportService;
import com.example.demo.service.FacilityService;
import com.example.demo.service.UserService;
import com.example.demo.vo.EquipmentReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EquipmentReportServiceImpl extends ServiceImpl<EquipmentReportMapper, EquipmentReport>
        implements EquipmentReportService {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private UserService userService;

    private static final Set<String> VALID_STATUSES = Arrays.stream(ReportStatusEnum.values())
            .map(ReportStatusEnum::getValue)
            .collect(Collectors.toSet());

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentReportVO createReport(Long userId, CreateReportDTO dto) {
        Facility facility = facilityService.getById(dto.getFacilityId());
        if (facility == null) {
            throw new BusinessException(404, "Facility not found");
        }

        EquipmentReport report = EquipmentReport.builder()
                .userId(userId)
                .facilityId(dto.getFacilityId())
                .description(dto.getDescription())
                .status(ReportStatusEnum.NOTED.getValue())
                .build();

        this.save(report);
        return convertToVO(report, facility, userService.getById(userId));
    }

    @Override
    public IPage<EquipmentReportVO> getAllReports(Long staffId, int page, int size, String status) {
        validateStaffOrAdmin(staffId);

        User staff = userService.getById(staffId);
        LambdaQueryWrapper<EquipmentReport> query = new LambdaQueryWrapper<EquipmentReport>()
                .orderByDesc(EquipmentReport::getCreatedAt);

        if (status != null && !status.isBlank()) {
            query.eq(EquipmentReport::getStatus, status);
        }

        if (UserRoleEnum.STAFF.getValue().equals(staff.getRole())) {
            List<Long> assignedFacilityIds = facilityService.lambdaQuery()
                    .eq(Facility::getAssignedStaffId, staffId)
                    .list()
                    .stream()
                    .map(Facility::getId)
                    .collect(Collectors.toList());

            if (assignedFacilityIds.isEmpty()) {
                Page<EquipmentReportVO> empty = new Page<>(page, size);
                empty.setTotal(0);
                empty.setRecords(List.of());
                return empty;
            }
            query.in(EquipmentReport::getFacilityId, assignedFacilityIds);
        }

        IPage<EquipmentReport> reportPage = this.page(new Page<>(page, size), query);
        Page<EquipmentReportVO> voPage = new Page<>(page, size);
        voPage.setTotal(reportPage.getTotal());
        voPage.setRecords(reportPage.getRecords().stream()
                .map(r -> {
                    Facility facility = facilityService.getById(r.getFacilityId());
                    User reporter = userService.getById(r.getUserId());
                    return convertToVO(r, facility, reporter);
                })
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<EquipmentReportVO> getMyReports(Long userId) {
        return this.lambdaQuery()
                .eq(EquipmentReport::getUserId, userId)
                .orderByDesc(EquipmentReport::getCreatedAt)
                .list()
                .stream()
                .map(r -> {
                    Facility facility = facilityService.getById(r.getFacilityId());
                    User reporter = userService.getById(r.getUserId());
                    return convertToVO(r, facility, reporter);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReportStatus(Long staffId, Long reportId, UpdateReportStatusDTO dto) {
        validateStaffOrAdmin(staffId);

        String newStatus = dto.getStatus();
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new BusinessException(400, "Invalid status. Must be one of: noted, repair_in_progress, resolved");
        }

        EquipmentReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException(404, "Report not found");
        }

        // 普通 staff 只能更新分配给自己设施的报告
        User staff = userService.getById(staffId);
        if (UserRoleEnum.STAFF.getValue().equals(staff.getRole())) {
            Facility facility = facilityService.getById(report.getFacilityId());
            if (facility == null || !staffId.equals(facility.getAssignedStaffId())) {
                throw new BusinessException(403, "No permission to update this report");
            }
        }

        report.setStatus(newStatus);
        this.updateById(report);
    }

    private void validateStaffOrAdmin(Long userId) {
        User user = userService.getById(userId);
        if (user == null
                || (!UserRoleEnum.STAFF.getValue().equals(user.getRole())
                && !UserRoleEnum.ADMIN.getValue().equals(user.getRole()))) {
            throw new BusinessException(403, "Access denied: staff or admin only");
        }
    }

    private EquipmentReportVO convertToVO(EquipmentReport report, Facility facility, User reporter) {
        return EquipmentReportVO.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .reporterName(reporter != null ? reporter.getName() : null)
                .facilityId(report.getFacilityId())
                .facilityName(facility != null ? facility.getName() : null)
                .facilityType(facility != null ? facility.getType() : null)
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
