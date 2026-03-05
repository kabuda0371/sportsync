package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Facility;
import com.example.demo.mapper.FacilityMapper;
import com.example.demo.service.FacilityService;
import com.example.demo.vo.FacilityVO;
import com.example.demo.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements FacilityService {

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
            throw new BusinessException(404, "没有此设备");
        }
        return convertToVO(facility);
    }

    private FacilityVO convertToVO(Facility facility) {
        return FacilityVO.builder()
                .id(facility.getId())
                .name(facility.getName())
                .type(facility.getType())
                .description(facility.getDescription())
                .usageGuidelines(facility.getUsageGuidelines())
                .capacityLimit(facility.getCapacityLimit())
                .timeSlotLimitMinutes(facility.getTimeSlotLimitMinutes())
                .build();
    }
}
