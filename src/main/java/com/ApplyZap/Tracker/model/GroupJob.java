package com.ApplyZap.Tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_job", uniqueConstraints = @UniqueConstraint(columnNames = { "group_id", "normalized_url" }))
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GroupJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "normalized_url", nullable = false, length = 2048)
    private String normalizedUrl;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "date_added")
    private LocalDateTime dateAdded;

    /** User who added this job row (for star indicator and analytics). */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id", nullable = false)
    private User addedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (dateAdded == null) {
            dateAdded = LocalDateTime.now();
        }
    }
}
