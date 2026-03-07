package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingStatusUpdateDTO;
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
import com.example.demo.service.NotificationService;
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

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBooking(Long userId, BookingRequestDTO requestDTO) {
        // Validate facility exists
        Facility facility = facilityService.getById(requestDTO.getFacilityId());
        if (facility == null) {
            throw new BusinessException(404, "Facility not found");
        }

        // Validate user account status
        User user = userService.getById(userId);
        if (user == null || !AccountStatusEnum.APPROVED.getValue().equalsIgnoreCase(user.getAccountStatus())) {
            throw new BusinessException(403, "Account not approved, unable to book facilities");
        }

        // Validate time
        if (requestDTO.getStartTime().isAfter(requestDTO.getEndTime())) {
            throw new BusinessException(400, "Start time must be earlier than end time");
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
            throw new BusinessException(409, "Time slot is already occupied");
        }

        // Create booking
        Booking booking = Booking.builder()
                .userId(userId)
                .facilityId(requestDTO.getFacilityId())
                .bookingDate(requestDTO.getBookingDate())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .status(BookingStatusEnum.PENDING.getValue())
                .activityDescription(requestDTO.getActivityDescription())
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
            throw new BusinessException(403, "No permission to view pending bookings");
        }

        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getStatus, BookingStatusEnum.PENDING.getValue())
                .orderByAsc(Booking::getBookingDate, Booking::getStartTime);

        // 如果是 STAFF，只显示分配给该员工的设施的预订
        if (UserRoleEnum.STAFF.getValue().equals(user.getRole())) {
            List<Long> assignedFacilityIds = facilityService.lambdaQuery()
                    .eq(Facility::getAssignedStaffId, staffId)
                    .list()
                    .stream()
                    .map(Facility::getId)
                    .collect(Collectors.toList());

            if (assignedFacilityIds.isEmpty()) {
                // Return empty list if staff has no assigned facilities
                return List.of();
            }
            queryWrapper.in(Booking::getFacilityId, assignedFacilityIds);
        }

        return this.list(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookingStatus(Long staffId, Long bookingId, BookingStatusUpdateDTO reviewDTO) {
        // Validate staff role
        User user = userService.getById(staffId);
        if (user == null || (!UserRoleEnum.STAFF.getValue().equals(user.getRole()) && !UserRoleEnum.ADMIN.getValue().equals(user.getRole()))) {
            throw new BusinessException(403, "No permission to approve bookings");
        }

        // 前端可能传大写，统一转小写再校验
        String normalizedStatus = reviewDTO.getStatus().toLowerCase();
        if (!BookingStatusEnum.APPROVED.getValue().equals(normalizedStatus) && !BookingStatusEnum.REJECTED.getValue().equals(normalizedStatus)) {
            throw new BusinessException(400, "Invalid approval status, must be approved or rejected");
        }

        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }

        // 校验员工是否有权限审批该设施
        if (UserRoleEnum.STAFF.getValue().equals(user.getRole())) {
            Facility facility = facilityService.getById(booking.getFacilityId());
            if (facility == null || !staffId.equals(facility.getAssignedStaffId())) {
                throw new BusinessException(403, "No permission to approve bookings for this facility");
            }
        }

        if (!BookingStatusEnum.PENDING.getValue().equals(booking.getStatus())) {
            throw new BusinessException(400, "Can only approve pending bookings");
        }

        // 如果提供了替代设施，验证该设施存在
        if (reviewDTO.getSuggestedFacilityId() != null) {
            Facility suggested = facilityService.getById(reviewDTO.getSuggestedFacilityId());
            if (suggested == null) {
                throw new BusinessException(404, "Suggested alternative facility not found");
            }
        }

        booking.setStatus(normalizedStatus);
        booking.setStaffNote(reviewDTO.getStaffNote());
        booking.setSuggestedFacilityId(reviewDTO.getSuggestedFacilityId());
        this.updateById(booking);

        // 审批完成后，自动向会员发送站内通知
        String message;
        if (BookingStatusEnum.APPROVED.getValue().equals(normalizedStatus)) {
            message = "Your booking request (ID: " + bookingId + ") has been approved! Looking forward to your visit.";
        } else {
            String note = (reviewDTO.getStaffNote() != null && !reviewDTO.getStaffNote().isBlank())
                    ? "备注：" + reviewDTO.getStaffNote()
                    : "请联系体育中心了解详情。";
            message = "Your booking request (ID: " + bookingId + ") has been rejected. " + note;
        }
        notificationService.sendNotification(booking.getUserId(), bookingId, message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }

        // 只能取消自己的预订
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(403, "No permission to cancel others' bookings");
        }

        // 只能取消待处理的预订
        if (!BookingStatusEnum.PENDING.getValue().equals(booking.getStatus())) {
            throw new BusinessException(400, "Can only cancel pending bookings");
        }

        booking.setStatus(BookingStatusEnum.CANCELLED.getValue());
        this.updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBookingCompleted(Long staffId, Long bookingId) {
        // 校验工作人员身份
        User staff = userService.getById(staffId);
        if (staff == null || (!UserRoleEnum.STAFF.getValue().equals(staff.getRole())
                && !UserRoleEnum.ADMIN.getValue().equals(staff.getRole()))) {
            throw new BusinessException(403, "No permission to perform this action");
        }

        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "Booking record not found");
        }

        // 校验员工是否有权限管理该设施
        if (UserRoleEnum.STAFF.getValue().equals(staff.getRole())) {
            Facility facility = facilityService.getById(booking.getFacilityId());
            if (facility == null || !staffId.equals(facility.getAssignedStaffId())) {
                throw new BusinessException(403, "No permission to mark bookings for this facility as completed");
            }
        }

        // 只有已批准的预订才能标记为已完成
        if (!BookingStatusEnum.APPROVED.getValue().equals(booking.getStatus())) {
            throw new BusinessException(400, "Only approved bookings can be marked as completed");
        }

        booking.setStatus(BookingStatusEnum.COMPLETED.getValue());
        this.updateById(booking);

        // 自动向会员发送完成通知
        String message = "Your facility booking (ID: " + bookingId + ") has been confirmed completed by staff. Thank you for using the sports center facilities!";
        notificationService.sendNotification(booking.getUserId(), bookingId, message);
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
                .activityDescription(booking.getActivityDescription())
                .staffNote(booking.getStaffNote())
                .suggestedFacilityId(booking.getSuggestedFacilityId())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
