package com.ApplyZap.Tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedApplicationDTO {
    private Long id;
    private String companyName;
    private String roleName;
    private Date dateOfApplication;
    private String status;
}
