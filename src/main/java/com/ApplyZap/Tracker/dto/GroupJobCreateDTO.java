package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupJobCreateDTO {
    private String jobLink;
    private String companyName;
    private String roleName;
}
