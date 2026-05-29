package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private LocalDateTime createdAt;
    private List<GroupMemberDTO> members;
}
