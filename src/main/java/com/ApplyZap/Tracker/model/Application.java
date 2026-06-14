package com.ApplyZap.Tracker.model;

import com.ApplyZap.Tracker.dto.ReferralContactSummaryDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

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
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;
    private boolean referral;
    private String status;

    /** When the application was first saved to the tracker (server-side). */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** When status last changed (server-side); equals createdAt on create. */
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "application_metadata", columnDefinition = "jsonb")
    private Map<String, Object> applicationMetadata;

    // Many-to-One relationship with User - each application belongs to one user
    // @JsonIgnore: Exclude from JSON serialization to avoid Hibernate lazy loading
    // proxy issues
    // Frontend doesn't need user data - filtering happens on backend
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_contact_id")
    private ReferralContact referralContact;

    /** JSON input/output; not persisted directly. */
    @Transient
    private Long referralContactId;

    /** Populated on GET responses when a CRM contact is linked. */
    @Transient
    private ReferralContactSummaryDTO referralContactSummary;
}
