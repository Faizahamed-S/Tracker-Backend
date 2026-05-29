package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupSummaryDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private LocalDateTime createdAt;
}
