package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Booking;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.vo.BookingVO;

import java.util.List;
import java.time.LocalDate;

public interface BookingService extends IService<Booking> {
    BookingVO createBooking(Long userId, BookingRequestDTO requestDTO);
    List<BookingVO> getUserBookings(Long userId);
    List<BookingVO> getBookingsForFacilityAndDate(Long facilityId, LocalDate date);
}
