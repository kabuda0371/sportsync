package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.BookingInvitation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookingInvitationMapper extends BaseMapper<BookingInvitation> {

    /**
     * 行级锁：拿到邀约记录并阻塞并发响应
     * 配合事务内的 UPDATE ... WHERE status='pending' 原子推进，确保单次响应
     */
    @Select("SELECT invitation_id AS id, booking_id, invitee_id, status, responded_at, created_at " +
            "FROM booking_invitations " +
            "WHERE booking_id = #{bookingId} AND invitee_id = #{inviteeId} " +
            "FOR UPDATE")
    BookingInvitation lockByBookingAndInvitee(@Param("bookingId") Long bookingId,
                                               @Param("inviteeId") Long inviteeId);

    /**
     * 统计某 booking 仍处于 pending 的邀约数；用于判断是否所有搭档都已响应
     */
    @Select("SELECT COUNT(*) FROM booking_invitations " +
            "WHERE booking_id = #{bookingId} AND status = 'pending'")
    long countPending(@Param("bookingId") Long bookingId);

    /**
     * 查询某用户作为受邀人、且仍可见（pending 或 accepted）的所有 booking_id
     * 拒绝后用户视图不再展示，符合"信息断层修复 + 拒绝后断联"的设计要求
     */
    @Select("SELECT booking_id FROM booking_invitations " +
            "WHERE invitee_id = #{userId} AND status IN ('pending', 'accepted')")
    List<Long> findVisibleBookingIdsForInvitee(@Param("userId") Long userId);

    /**
     * 查询某用户对某 booking 的邀约状态（用于 VO 渲染）
     */
    @Select("SELECT status FROM booking_invitations " +
            "WHERE booking_id = #{bookingId} AND invitee_id = #{inviteeId} " +
            "LIMIT 1")
    String findStatus(@Param("bookingId") Long bookingId,
                      @Param("inviteeId") Long inviteeId);
}
