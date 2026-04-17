package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.CreateReportDTO;
import com.example.demo.dto.UpdateReportStatusDTO;
import com.example.demo.entity.EquipmentReport;
import com.example.demo.vo.EquipmentReportVO;

import java.util.List;

public interface EquipmentReportService extends IService<EquipmentReport> {

    EquipmentReportVO createReport(Long userId, CreateReportDTO dto);

    List<EquipmentReportVO> getAllReports(Long staffId);

    List<EquipmentReportVO> getMyReports(Long userId);

    void updateReportStatus(Long staffId, Long reportId, UpdateReportStatusDTO dto);
}
