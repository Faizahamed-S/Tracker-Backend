package com.ApplyZap.Tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Many-to-One relationship with User - each application belongs to one user
    // @JsonIgnore: Exclude from JSON serialization to avoid Hibernate lazy loading
    // proxy issues
    // Frontend doesn't need user data - filtering happens on backend
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
