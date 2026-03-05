package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.dto.BookingRequestDTO;
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
            throw new BusinessException(401, "User not authenticated");
        }
        return Result.success(bookingService.createBooking(userId, requestDTO));
    }

    @GetMapping("/my")
    @Operation(summary = "查看我的预订记录", description = "获取当前登录用户的所有预订历史及其状态")
    public Result<List<BookingVO>> getMyBookings() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        return Result.success(bookingService.getUserBookings(userId));
    }

    @GetMapping("/available")
    @Operation(summary = "查询设施已预订时段", description = "根据设施ID和日期，查询该日期下已被占用或待审批的时段，用于辅助用户选择可用时间数据。")
    public Result<List<BookingVO>> getAvailable(@ParameterObject @Valid FacilityQueryDTO query) {

        System.out.println(query.getFacilityId());
        System.out.println("------------");
        return Result.success(bookingService.getBookingsForFacilityAndDate(query.getFacilityId(), query.getDate()));
    }
}
