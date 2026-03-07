package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Facility;
import com.example.demo.mapper.FacilityMapper;
import com.example.demo.service.FacilityService;
import com.example.demo.vo.FacilityVO;
import com.example.demo.dto.FacilityDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.entity.User;
import com.example.demo.enums.UserRoleEnum;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements FacilityService {

    @Autowired
    private UserService userService;

    @Override
    public List<FacilityVO> getAllFacilities() {
        return this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public FacilityVO getFacilityById(Long id) {
        Facility facility = this.getById(id);
        if (facility == null) {
            throw new BusinessException(404, "Facility not found");
        }
        return convertToVO(facility);
    }

    @Override
    public void createFacility(FacilityDTO dto) {
        Facility facility = Facility.builder()
                .name(dto.getName())
                .type(dto.getType())
                .description(dto.getDescription())
                .usageGuidelines(dto.getUsageGuidelines())
                .capacityLimit(dto.getCapacityLimit())
                .timeSlotLimitMinutes(dto.getTimeSlotLimitMinutes())
                .assignedStaffId(dto.getAssignedStaffId())
                .build();
        this.save(facility);
    }

    @Override
    public void updateFacility(Long id, FacilityDTO dto) {
        Facility existingFacility = this.getById(id);
        if (existingFacility == null) {
            throw new BusinessException(404, "Facility not found");
        }
        existingFacility.setName(dto.getName());
        existingFacility.setType(dto.getType());
        existingFacility.setDescription(dto.getDescription());
        existingFacility.setUsageGuidelines(dto.getUsageGuidelines());
        existingFacility.setCapacityLimit(dto.getCapacityLimit());
        existingFacility.setTimeSlotLimitMinutes(dto.getTimeSlotLimitMinutes());
        existingFacility.setAssignedStaffId(dto.getAssignedStaffId());
        
        this.updateById(existingFacility);
    }

    @Override
    public void deleteFacility(Long id) {
        Facility existingFacility = this.getById(id);
        if (existingFacility == null) {
            throw new BusinessException(404, "Facility not found");
        }
        this.removeById(id);
    }

    @Override
    public void assignStaff(Long facilityId, Long staffId) {
        Facility facility = this.getById(facilityId);
        if (facility == null) {
            throw new BusinessException(404, "Facility not found");
        }
        
        User staff = userService.getById(staffId);
        if (staff == null) {
            throw new BusinessException(404, "User not found");
        }
        
        if (!UserRoleEnum.STAFF.getValue().equals(staff.getRole())) {
            throw new BusinessException(400, "The user is not a staff member and cannot be assigned");
        }
        
        facility.setAssignedStaffId(staffId);
        this.updateById(facility);
    }

    private FacilityVO convertToVO(Facility facility) {
        FacilityVO vo = FacilityVO.builder()
                .id(facility.getId())
                .name(facility.getName())
                .type(facility.getType())
                .description(facility.getDescription())
                .usageGuidelines(facility.getUsageGuidelines())
                .capacityLimit(facility.getCapacityLimit())
                .timeSlotLimitMinutes(facility.getTimeSlotLimitMinutes())
                .assignedStaffId(facility.getAssignedStaffId())
                .build();
                
        if (facility.getAssignedStaffId() != null) {
            User staff = userService.getById(facility.getAssignedStaffId());
            if (staff != null) {
                vo.setAssignedStaffName(staff.getName());
            }
        }
        return vo;
    }
}
