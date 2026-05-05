package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Booking;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingStatusUpdateDTO;
import com.example.demo.vo.BookingVO;

import java.util.List;
import java.time.LocalDate;

public interface BookingService extends IService<Booking> {
    BookingVO createBooking(Long userId, BookingRequestDTO requestDTO);
    List<BookingVO> getUserBookings(Long userId);
    List<BookingVO> getUpcomingBookings(Long userId);
    List<BookingVO> getBookingsForFacilityAndDate(Long facilityId, LocalDate date);
    IPage<BookingVO> getPendingBookings(Long staffId, int page, int size, String status);
    void updateBookingStatus(Long staffId, Long bookingId, BookingStatusUpdateDTO reviewDTO);
    void cancelBooking(Long userId, Long bookingId);
    void markBookingCompleted(Long staffId, Long bookingId);

    /**
     * 携搭档预订 — 受邀人响应邀约
     * @param inviteeId 当前登录用户（B）
     * @param bookingId 邀约对应的预订
     * @param accept    true=接受，false=拒绝
     */
    void respondToInvitation(Long inviteeId, Long bookingId, boolean accept);
}
