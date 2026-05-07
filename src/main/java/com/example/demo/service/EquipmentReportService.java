package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.CreateReportDTO;
import com.example.demo.dto.UpdateReportStatusDTO;
import com.example.demo.entity.EquipmentReport;
import com.example.demo.vo.EquipmentReportVO;

import java.util.List;
import java.time.LocalDate;

public interface EquipmentReportService extends IService<EquipmentReport> {

    EquipmentReportVO createReport(Long userId, CreateReportDTO dto);

    IPage<EquipmentReportVO> getAllReports(Long staffId, int page, int size, String status);

    List<EquipmentReportVO> getMyReports(Long userId);
    List<EquipmentReportVO> getMyReports(Long userId, String status, Long facilityId, LocalDate startDate, LocalDate endDate);

    void updateReportStatus(Long staffId, Long reportId, UpdateReportStatusDTO dto);
}
