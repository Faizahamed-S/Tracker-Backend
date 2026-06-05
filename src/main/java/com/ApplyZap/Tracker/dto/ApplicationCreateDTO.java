package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateDTO {
    private String companyName;
    private String roleName;
    private Date dateOfApplication;
    private String jobLink;
    private boolean tailored;
    private String jobDescription;
    private boolean referral;
    private String status;
    private Map<String, Object> applicationMetadata;
    /** Optional: mirror job subset (link, company, role) to these collaborative groups. */
    private List<Long> groupIds;
}
