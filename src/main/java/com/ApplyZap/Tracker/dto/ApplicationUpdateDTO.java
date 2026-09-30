package com.ApplyZap.Tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Request body for PUT/PATCH /board/applications/{id}.
 * Every field is nullable: null means "not sent, leave unchanged".
 * Server-owned fields (id, userJobId, createdAt, ...) are ignored if sent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationUpdateDTO {
    private String companyName;
    private String roleName;
    private Date dateOfApplication;
    private String jobLink;
    private String jobDescription;
    private String status;
    private Boolean tailored;
    private Boolean referral;
    /** When set, links this CRM contact and forces referral=true. */
    private Long referralContactId;
    private Map<String, Object> applicationMetadata;
    /** Optional: mirror job subset (link, company, role) to these collaborative groups. */
    private List<Long> groupIds;
}
