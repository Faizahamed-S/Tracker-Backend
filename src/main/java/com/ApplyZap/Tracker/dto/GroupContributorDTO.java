package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupContributorDTO {
    private Long memberId;
    private Long userId;
    private String displayName;
    private long jobsAdded;
}
