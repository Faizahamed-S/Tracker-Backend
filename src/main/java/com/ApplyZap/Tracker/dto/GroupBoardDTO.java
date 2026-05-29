package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBoardDTO {
    private Long id;
    private String name;
    private List<GroupMemberDTO> members;
    private List<GroupJobRowDTO> jobs;
}
