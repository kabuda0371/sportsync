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

    /**
     * 设施级时间冲突检测（带行级锁，FOR UPDATE）
     * 注意：awaiting_partner 也占用时段，避免邀约期间被新订单抢走
     */
    @Select("SELECT COUNT(*) FROM bookings " +
            "WHERE facility_id = #{facilityId} " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN ('pending', 'approved', 'awaiting_partner') " +
            "AND start_time < #{endTime} " +
            "AND end_time > #{startTime} " +
            "FOR UPDATE")
    long countConflictForUpdate(@Param("facilityId") Long facilityId,
                                @Param("bookingDate") LocalDate bookingDate,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    /**
     * 用户作为主预订人 (booking.user_id) 的时间冲突；带 FOR UPDATE 锁
     * 用于校验搭档 B 在该时段是否已有自己的预订（防止双重预订）
     */
    @Select("SELECT COUNT(*) FROM bookings " +
            "WHERE user_id = #{userId} " +
            "AND booking_date = #{bookingDate} " +
            "AND status IN ('pending', 'approved', 'awaiting_partner') " +
            "AND start_time < #{endTime} " +
            "AND end_time > #{startTime} " +
            "FOR UPDATE")
    long countUserOwnConflictForUpdate(@Param("userId") Long userId,
                                        @Param("bookingDate") LocalDate bookingDate,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime);

    /**
     * 用户作为已接受搭档（booking_invitations.status='accepted'）的时间冲突；带 FOR UPDATE 锁
     * 用于校验搭档 B 是否已接受了别人在同一时段的邀约
     */
    @Select("SELECT COUNT(*) FROM bookings b " +
            "JOIN booking_invitations bi ON b.booking_id = bi.booking_id " +
            "WHERE bi.invitee_id = #{userId} AND bi.status = 'accepted' " +
            "AND b.booking_date = #{bookingDate} " +
            "AND b.status IN ('pending', 'approved', 'awaiting_partner') " +
            "AND b.start_time < #{endTime} " +
            "AND b.end_time > #{startTime} " +
            "FOR UPDATE")
    long countUserInvitedConflictForUpdate(@Param("userId") Long userId,
                                            @Param("bookingDate") LocalDate bookingDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime);

    /**
     * 行级锁住单条 booking 记录；用于 respondToInvitation 串行化同一订单的并发响应，
     * 同时防止与 cancelBooking / updateBookingStatus 的状态写竞争
     */
    @Select("SELECT booking_id AS id, user_id, facility_id, booking_date, start_time, end_time, status, " +
            "activity_description, staff_note, suggested_facility_id, partner_ids, created_at, updated_at " +
            "FROM bookings WHERE booking_id = #{bookingId} FOR UPDATE")
    Booking lockBookingById(@Param("bookingId") Long bookingId);
}
