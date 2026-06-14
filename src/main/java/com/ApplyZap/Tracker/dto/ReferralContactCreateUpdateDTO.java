package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralContactCreateUpdateDTO {
    private String name;
    private String companyName;
    private String mobile;
    private String email;
    private String linkedinUrl;
    private String notes;
    private Map<String, Object> customFields;
}
