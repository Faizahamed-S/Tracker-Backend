package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralContactListDTO {
    private Long id;
    private String name;
    private String companyName;
    private String email;
    private String mobile;
    private String linkedinUrl;
}
