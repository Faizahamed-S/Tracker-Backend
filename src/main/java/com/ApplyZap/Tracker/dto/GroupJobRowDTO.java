package com.ApplyZap.Tracker.dto;

import com.ApplyZap.Tracker.model.GroupJobCellStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupJobRowDTO {
    private Long jobId;
    private String normalizedUrl;
    private String originalUrl;
    private String companyName;
    private String roleName;
    private LocalDateTime dateAdded;
    private Long addedByUserId;
    private Long addedByMemberId; // for star: member id in this group who added (null if they left)
    private List<GroupJobStatusCellDTO> statuses;
}
