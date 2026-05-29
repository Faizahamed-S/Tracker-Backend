package com.ApplyZap.Tracker.dto;

import com.ApplyZap.Tracker.model.GroupJobCellStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupJobStatusCellDTO {
    private Long memberId;
    private GroupJobCellStatus status;
}
