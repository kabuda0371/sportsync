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
import com.example.demo.service.UserService;
import com.example.demo.vo.BookingVO;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatusEnum;
import com.example.demo.enums.BookingStatusEnum;
import com.example.demo.enums.UserRoleEnum;
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

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBooking(Long userId, BookingRequestDTO requestDTO) {
        // Validate facility exists
        Facility facility = facilityService.getById(requestDTO.getFacilityId());
        if (facility == null) {
            throw new BusinessException(404, "设施不存在");
        }

        // Validate user account status
        User user = userService.getById(userId);
        if (user == null || !AccountStatusEnum.APPROVED.getValue().equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException(403, "账号未获得批准，无法预定设施");
        }

        // Validate time
        if (requestDTO.getStartTime().isAfter(requestDTO.getEndTime())) {
            throw new BusinessException(400, "开始时间必须早于结束时间");
        }

        // Check for conflicts
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getFacilityId, requestDTO.getFacilityId())
                .eq(Booking::getBookingDate, requestDTO.getBookingDate())
                .in(Booking::getStatus, BookingStatusEnum.PENDING.getValue(), BookingStatusEnum.APPROVED.getValue())
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
                .status(BookingStatusEnum.PENDING.getValue())
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
    public List<BookingVO> getUpcomingBookings(Long userId) {
        LocalDate today = LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();

        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getUserId, userId)
                .and(w -> w.gt(Booking::getBookingDate, today)
                        .or(w2 -> w2.eq(Booking::getBookingDate, today).gt(Booking::getStartTime, now)))
                .orderByAsc(Booking::getBookingDate, Booking::getStartTime);

        return this.list(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingVO> getBookingsForFacilityAndDate(Long facilityId, LocalDate date) {
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getFacilityId, facilityId)
                .eq(Booking::getBookingDate, date)
                .in(Booking::getStatus, BookingStatusEnum.PENDING.getValue(), BookingStatusEnum.APPROVED.getValue())
                .orderByAsc(Booking::getStartTime);
        
        return this.list(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingVO> getPendingBookings(Long staffId) {
        // Validate staff role
        User user = userService.getById(staffId);
        if (user == null || (!UserRoleEnum.STAFF.getValue().equals(user.getRole()) && !UserRoleEnum.ADMIN.getValue().equals(user.getRole()))) {
            throw new BusinessException(403, "没有权限查看待审批预订");
        }

        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getStatus, BookingStatusEnum.PENDING.getValue())
                .orderByAsc(Booking::getBookingDate, Booking::getStartTime);

        return this.list(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookingStatus(Long staffId, Long bookingId, String status) {
        // Validate staff role
        User user = userService.getById(staffId);
        if (user == null || (!UserRoleEnum.STAFF.getValue().equals(user.getRole()) && !UserRoleEnum.ADMIN.getValue().equals(user.getRole()))) {
            throw new BusinessException(403, "没有权限审批预订");
        }

        // 前端可能传大写，统一转小写再校验
        String normalizedStatus = status.toLowerCase();
        if (!BookingStatusEnum.APPROVED.getValue().equals(normalizedStatus) && !BookingStatusEnum.REJECTED.getValue().equals(normalizedStatus)) {
            throw new BusinessException(400, "无效的审批状态");
        }

        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预订记录不存在");
        }

        if (!BookingStatusEnum.PENDING.getValue().equals(booking.getStatus())) {
            throw new BusinessException(400, "只能审批待处理的预订");
        }

        booking.setStatus(normalizedStatus);
        this.updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预订记录不存在");
        }

        // 只能取消自己的预订
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权取消他人的预订");
        }

        // 只能取消待处理的预订
        if (!BookingStatusEnum.PENDING.getValue().equals(booking.getStatus())) {
            throw new BusinessException(400, "只能取消待处理的预订");
        }

        booking.setStatus(BookingStatusEnum.CANCELLED.getValue());
        this.updateById(booking);
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
