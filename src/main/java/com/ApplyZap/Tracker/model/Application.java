package com.ApplyZap.Tracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Application {

    @Id
    private Long id;
    private String companyName;
    private String roleName;
    private Date dateOfApplication;
    private String jobLink;
    private boolean tailored;
    private String jobDescription;
    private boolean referral;
}
