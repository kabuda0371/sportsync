package com.example.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingVO {
    @Schema(description = "Booking record ID")
    private Long id;

    @Schema(description = "Booking owner user ID")
    private Long userId;

    @Schema(description = "Facility ID")
    private Long facilityId;

    @Schema(description = "Booking date")
    private LocalDate bookingDate;

    @Schema(description = "Start time")
    private LocalTime startTime;

    @Schema(description = "End time")
    private LocalTime endTime;

    @Schema(description = "Booking status: awaiting_partner, pending, approved, rejected, cancelled, completed")
    private String status;

    @Schema(description = "Activity description provided during booking")
    private String activityDescription;

    @Schema(description = "Staff note")
    private String staffNote;

    @Schema(description = "Suggested alternative facility ID")
    private Long suggestedFacilityId;

    @Schema(description = "Accepted partner user IDs")
    private List<Long> partnerIds;

    @Schema(description = "Accepted partner names")
    private List<String> partnerNames;

    @Schema(description = "Invitation response details for each invited partner")
    private List<InvitationMemberStatusVO> invitationStatuses;

    @Schema(description = "Current viewer invitation status: pending, accepted, declined")
    private String myInvitationStatus;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
