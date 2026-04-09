package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements FacilityService {

    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String FACILITY_LIST_KEY = "facility:list";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public List<FacilityVO> getAllFacilities() {
        // 1. 尝试从 Redis 获取缓存
        String cached = stringRedisTemplate.opsForValue().get(FACILITY_LIST_KEY);
        if (cached != null) {
            try {
                log.debug("从 Redis 缓存获取设施列表");
                return objectMapper.readValue(cached, new TypeReference<List<FacilityVO>>() {});
            } catch (Exception e) {
                log.warn("反序列化设施缓存失败，回退查库", e);
                stringRedisTemplate.delete(FACILITY_LIST_KEY);
            }
        }

        // 2. 缓存未命中，查数据库
        List<FacilityVO> list = this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 写入 Redis 缓存
        try {
            String json = objectMapper.writeValueAsString(list);
            stringRedisTemplate.opsForValue().set(FACILITY_LIST_KEY, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("设施列表已缓存至 Redis，过期时间 {} 小时", CACHE_EXPIRE_HOURS);
        } catch (Exception e) {
            log.warn("设施列表写入 Redis 缓存失败", e);
        }

        return list;
    }

    @Override
    public IPage<FacilityVO> getFacilitiesPage(int page, int size, String type, Integer minCapacity) {
        QueryWrapper<Facility> wrapper = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (minCapacity != null) {
            wrapper.ge("capacity_limit", minCapacity);
        }
        Page<Facility> facilityPage = this.page(new Page<>(page, size), wrapper);
        Page<FacilityVO> voPage = new Page<>(page, size);
        voPage.setTotal(facilityPage.getTotal());
        voPage.setRecords(facilityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<String> getAllTypes() {
        return this.list().stream()
                .map(Facility::getType)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
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
        clearFacilityCache();
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
        clearFacilityCache();
    }

    @Override
    public void deleteFacility(Long id) {
        Facility existingFacility = this.getById(id);
        if (existingFacility == null) {
            throw new BusinessException(404, "Facility not found");
        }
        this.removeById(id);
        clearFacilityCache();
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
        clearFacilityCache();
    }

    private void clearFacilityCache() {
        stringRedisTemplate.delete(FACILITY_LIST_KEY);
        log.debug("设施列表缓存已清除");
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
