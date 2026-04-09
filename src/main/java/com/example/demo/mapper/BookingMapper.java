package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalTime;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    @Select("SELECT COUNT(*) FROM bookings " +
            "WHERE facility_id = #{facilityId} " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN ('pending', 'approved') " +
            "AND start_time < #{endTime} " +
            "AND end_time > #{startTime} " +
            "FOR UPDATE")
    long countConflictForUpdate(@Param("facilityId") Long facilityId,
                                @Param("bookingDate") LocalDate bookingDate,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);
}
