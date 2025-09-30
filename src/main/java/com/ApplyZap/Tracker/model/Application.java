package com.ApplyZap.Tracker.model;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String companyName;
    private String roleName;
    private Date dateOfApplication;
    private String jobLink;
    private boolean tailored;
    private String jobDescription;
    private boolean referral;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
}
