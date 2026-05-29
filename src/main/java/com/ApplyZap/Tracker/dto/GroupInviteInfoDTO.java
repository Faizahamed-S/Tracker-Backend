package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupInviteInfoDTO {
    private String groupName;
    private String inviterName;
    private String email;
    private boolean valid;
}
