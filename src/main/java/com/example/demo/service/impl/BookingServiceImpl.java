package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.entity.Booking;
import com.example.demo.entity.Facility;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.BookingMapper;
import com.example.demo.service.BookingService;
import com.example.demo.service.FacilityService;
import com.example.demo.vo.BookingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private FacilityService facilityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBooking(Long userId, BookingRequestDTO requestDTO) {
        // Validate facility exists
        Facility facility = facilityService.getById(requestDTO.getFacilityId());
        if (facility == null) {
            throw new BusinessException(404, "设施不存在");
        }

        // Validate time
        if (requestDTO.getStartTime().isAfter(requestDTO.getEndTime())) {
            throw new BusinessException(400, "开始时间必须早于结束时间");
        }

        // Check for conflicts
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getFacilityId, requestDTO.getFacilityId())
                .eq(Booking::getBookingDate, requestDTO.getBookingDate())
                .in(Booking::getStatus, "PENDING", "APPROVED")
                .and(w -> w
                        .lt(Booking::getStartTime, requestDTO.getEndTime())
                        .gt(Booking::getEndTime, requestDTO.getStartTime())
                );

        long conflictCount = this.count(queryWrapper);
        if (conflictCount > 0) {
            throw new BusinessException(409, "时间段已被占用");
        }

        // Create booking
        Booking booking = Booking.builder()
                .userId(userId)
                .facilityId(requestDTO.getFacilityId())
                .bookingDate(requestDTO.getBookingDate())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .status("PENDING")
                .build();

        this.save(booking);

        return convertToVO(booking);
    }

    @Override
    public List<BookingVO> getUserBookings(Long userId) {
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getUserId, userId)
                .orderByDesc(Booking::getBookingDate, Booking::getStartTime);
        
        return this.list(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingVO> getBookingsForFacilityAndDate(Long facilityId, LocalDate date) {
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getFacilityId, facilityId)
                .eq(Booking::getBookingDate, date)
                .in(Booking::getStatus, "PENDING", "APPROVED")
                .orderByAsc(Booking::getStartTime);
        
        return this.list(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private BookingVO convertToVO(Booking booking) {
        return BookingVO.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .facilityId(booking.getFacilityId())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
