package com.example.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationMemberStatusVO {

    @Schema(description = "Invitee user ID")
    private Long inviteeId;

    @Schema(description = "Invitee display name")
    private String inviteeName;

    @Schema(description = "Invitation status: pending, accepted, declined")
    private String status;
}
