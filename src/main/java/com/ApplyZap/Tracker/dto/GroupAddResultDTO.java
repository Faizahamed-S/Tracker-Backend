package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAddResultDTO {
    private Long groupId;
    private boolean success;
    private Long jobId;
    private String error;
}
