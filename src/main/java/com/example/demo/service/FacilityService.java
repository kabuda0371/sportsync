package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Facility;
import com.example.demo.vo.FacilityVO;
import com.example.demo.dto.FacilityDTO;

import java.util.List;

public interface FacilityService extends IService<Facility> {
    List<FacilityVO> getAllFacilities();
    FacilityVO getFacilityById(Long id);
    void createFacility(FacilityDTO dto);
    void updateFacility(Long id, FacilityDTO dto);
    void deleteFacility(Long id);
    void assignStaff(Long facilityId, Long staffId);
}
