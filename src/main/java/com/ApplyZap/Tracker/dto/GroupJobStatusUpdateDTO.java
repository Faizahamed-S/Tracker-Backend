package com.ApplyZap.Tracker.dto;

import com.ApplyZap.Tracker.model.GroupJobCellStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupJobStatusUpdateDTO {
    private GroupJobCellStatus status;
}
