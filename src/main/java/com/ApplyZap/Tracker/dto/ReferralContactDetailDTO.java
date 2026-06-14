package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralContactDetailDTO {
    private Long id;
    private String name;
    private String companyName;
    private String mobile;
    private String email;
    private String linkedinUrl;
    private String notes;
    private Map<String, Object> customFields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AssociatedApplicationDTO> associatedApplications;
}
