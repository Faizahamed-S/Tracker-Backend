package com.ApplyZap.Tracker.dto;

import com.ApplyZap.Tracker.model.GroupRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDTO {
    private Long memberId;
    private String displayName;
    private GroupRole role;
    private Long userId; // optional, for frontend to know user identity
}
