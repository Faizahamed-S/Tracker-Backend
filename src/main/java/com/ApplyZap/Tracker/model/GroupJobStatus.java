package com.ApplyZap.Tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Per-member, per-job cell: Applied / Expired / N/A. */
@Entity
@Table(name = "group_job_status", uniqueConstraints = @UniqueConstraint(columnNames = { "group_job_id", "member_id" }))
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GroupJobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_job_id", nullable = false)
    private GroupJob groupJob;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private GroupMember member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupJobCellStatus status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
