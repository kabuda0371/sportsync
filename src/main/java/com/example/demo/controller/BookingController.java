package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingStatusUpdateDTO;
import com.example.demo.dto.FacilityQueryDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.BookingService;
import com.example.demo.vo.BookingVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "预订管理", description = "处理设施申请预订、查看个人预订及查询可用时段")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    @Operation(summary = "申请预订设施", description = "提交预订申请，需提供设施ID、日期及时间段")
    public Result<BookingVO> createBooking(@Valid @RequestBody BookingRequestDTO requestDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return Result.success(bookingService.createBooking(userId, requestDTO));
    }

    @GetMapping("/my")
    @Operation(summary = "查看我的预订记录", description = "获取当前登录用户的所有预订历史及其状态")
    public Result<List<BookingVO>> getMyBookings() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return Result.success(bookingService.getUserBookings(userId));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "查看即将进行的预订", description = "获取当前登录用户未来即将进行的预订")
    public Result<List<BookingVO>> getUpcomingBookings() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return Result.success(bookingService.getUpcomingBookings(userId));
    }

    @GetMapping("/available")
    @Operation(summary = "查询设施已预订时段", description = "根据设施ID和日期，查询该日期下已被占用或待审批的时段，用于辅助用户选择可用时间数据。")
    public Result<List<BookingVO>> getAvailable(@ParameterObject @Valid FacilityQueryDTO query) {
        return Result.success(bookingService.getBookingsForFacilityAndDate(query.getFacilityId(), query.getDate()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "查询待审批预订", description = "获取所有待审批的预订记录，仅限员工或管理员")
    public Result<List<BookingVO>> getPendingBookings() {
        Long staffId = UserContext.getUserId();
        return Result.success(bookingService.getPendingBookings(staffId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "审批预订", description = "批准或拒绝预订申请，仅限员工或管理员")
    public Result<Void> updateBookingStatus(
            @Parameter(description = "预订ID") @PathVariable Long id,
            @Valid @RequestBody BookingStatusUpdateDTO statusDTO) {
        Long staffId = UserContext.getUserId();
        bookingService.updateBookingStatus(staffId, id, statusDTO.getStatus());
        return Result.success(null);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "取消预订", description = "会员取消自己的待处理预订（仅限 pending 状态）")
    public Result<Void> cancelBooking(
            @Parameter(description = "预订ID") @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        bookingService.cancelBooking(userId, id);
        return Result.success(null);
    }
}

